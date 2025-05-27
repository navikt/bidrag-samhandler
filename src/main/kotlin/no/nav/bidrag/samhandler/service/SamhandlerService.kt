package no.nav.bidrag.samhandler.service

import jakarta.persistence.EntityManager
import no.nav.bidrag.commons.security.utils.TokenUtils
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.domene.land.Landkode3
import no.nav.bidrag.samhandler.SECURE_LOGGER
import no.nav.bidrag.samhandler.mapper.SamhandlerMapper
import no.nav.bidrag.samhandler.persistence.repository.SamhandlerRepository
import no.nav.bidrag.samhandler.persistence.repository.SamhandlerSøkSpec
import no.nav.bidrag.samhandler.util.KontonummerUtils
import no.nav.bidrag.transport.samhandler.AdresseDto
import no.nav.bidrag.transport.samhandler.KontonummerDto
import no.nav.bidrag.transport.samhandler.SamhandlerDto
import no.nav.bidrag.transport.samhandler.SamhandlerKafkaHendelsestype
import no.nav.bidrag.transport.samhandler.SamhandlerSøk
import no.nav.bidrag.transport.samhandler.SamhandlersøkeresultatDto
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class SamhandlerService(
    private val samhandlerRepository: SamhandlerRepository,
    private val kafkaService: KafkaService,
    private val entityManager: EntityManager,
) {
    @Transactional
    fun hentSamhandler(
        ident: Ident,
        inkluderAuditLog: Boolean,
    ): SamhandlerDto? {
        samhandlerRepository.findByIdent(ident.verdi)?.let { samhandler ->
            return SamhandlerMapper.mapTilSamhandlerDto(samhandler, inkluderAuditLog)
        }
        return null
    }

    fun samhandlerSøk(samhandlerSøk: SamhandlerSøk): SamhandlersøkeresultatDto {
        SECURE_LOGGER.info(
            "Samhandlersøk utført av {}. Input: {}",
            TokenUtils.hentSaksbehandlerIdent() ?: TokenUtils.hentApplikasjonsnavn() ?: "ukjent",
            samhandlerSøk,
        )
        val samhandlere = samhandlerRepository.findAll(SamhandlerSøkSpec.søkPåAlleParameter(samhandlerSøk))
        SECURE_LOGGER.info("Samhandlersøk returnerte følgende samhandlere: {}", samhandlere.map { it.ident }.toString())
        return SamhandlerMapper.mapTilSamhandlersøkeresultatDto(samhandlere)
    }

    @Transactional
    fun opprettSamhandler(samhandlerDto: SamhandlerDto): Int {
        val samhandler = SamhandlerMapper.mapTilSamhandler(samhandlerDto)
        SECURE_LOGGER.info(
            "OpprettSamhandler for {} utført av {} med følgende data: {}",
            samhandlerDto.navn,
            TokenUtils.hentSaksbehandlerIdent() ?: TokenUtils.hentApplikasjonsnavn() ?: "ukjent",
            samhandlerDto,
        )
        val opprettetSamhandler = samhandlerRepository.save(samhandler)
        entityManager.refresh(opprettetSamhandler)
        kafkaService.sendSamhandlerMelding(opprettetSamhandler, SamhandlerKafkaHendelsestype.OPPRETTET)

        return opprettetSamhandler.id!!
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun hentSamhandlerPåId(samhandlerId: Int) =
        samhandlerRepository.findById(samhandlerId).getOrNull()?.let {
            SamhandlerMapper.mapTilSamhandlerDto(it)
        }

    @Transactional
    fun oppdaterSamhandler(samhandlerDto: SamhandlerDto): ResponseEntity<*> {
        val samhandlerIdent =
            samhandlerDto.samhandlerId?.verdi ?: return ResponseEntity
                .badRequest()
                .body("Oppdatering av samhandler må ha angitt samhandlerId!")
        val samhandler =
            samhandlerRepository.findByIdent(samhandlerIdent) ?: return ResponseEntity.notFound().build<Any>()

        val oppdatertSamhandler =
            samhandler.copy(
                navn =
                    samhandlerDto.navn,
                offentligId = samhandlerDto.offentligId,
                offentligIdType = samhandlerDto.offentligIdType,
                områdekode = samhandlerDto.områdekode,
                språk = samhandlerDto.språk,
                norskkontonr = samhandlerDto.kontonummer?.norskKontonummer,
                iban = samhandlerDto.kontonummer?.iban,
                swift = samhandlerDto.kontonummer?.swift,
                banknavn = samhandlerDto.kontonummer?.banknavn,
                banklandkode = samhandlerDto.kontonummer?.landkodeBank?.verdi,
                valutakode = samhandlerDto.kontonummer?.valutakode,
                bankcode = samhandlerDto.kontonummer?.bankCode,
                adresselinje1 = samhandlerDto.adresse?.adresselinje1,
                adresselinje2 = samhandlerDto.adresse?.adresselinje2,
                adresselinje3 = samhandlerDto.adresse?.adresselinje3,
                postnr = samhandlerDto.adresse?.postnr,
                poststed = samhandlerDto.adresse?.poststed,
                land = samhandlerDto.adresse?.land?.verdi,
                kontaktperson = samhandlerDto.kontaktperson,
                kontaktEpost = samhandlerDto.kontaktEpost,
                kontaktTelefon = samhandlerDto.kontaktTelefon,
                notat = samhandlerDto.notat,
                erOpphørt = samhandlerDto.erOpphørt ?: false,
            )

        val lagretSamhandler = samhandlerRepository.save(oppdatertSamhandler)
        kafkaService.sendSamhandlerMelding(lagretSamhandler, SamhandlerKafkaHendelsestype.OPPDATERT)

        SECURE_LOGGER.info(
            "OppdaterSamhandler for {} utført av {} fra data: {}",
            samhandlerDto.samhandlerId,
            TokenUtils.hentSaksbehandlerIdent() ?: TokenUtils.hentApplikasjonsnavn() ?: "ukjent",
            samhandlerDto,
        )

        return ResponseEntity.ok(SamhandlerMapper.mapTilSamhandlerDto(oppdatertSamhandler))
    }

    fun validerInput(samhandlerDto: SamhandlerDto): ResponseEntity<*>? {
        if (samhandlerDto.adresse != null) {
            validerAdresse(samhandlerDto.adresse!!)?.let { return it }
        }
        if (samhandlerDto.kontonummer != null) {
            validerKontonummer(samhandlerDto.kontonummer!!)?.let { return it }
        }
        if (samhandlerDto.språk == null) {
            return ResponseEntity.badRequest().body("Språk må angis.")
        }

        return null
    }

    private fun validerKontonummer(kontonummer: KontonummerDto): ResponseEntity<*>? {
        if (kontonummer.norskKontonummer?.isNotEmpty() == true) {
            if (kontonummer.norskKontonummer?.length != 11) {
                return ResponseEntity.badRequest().body("Norsk kontonummer må være 11 tegn langt.")
            }
            if (!KontonummerUtils.erGyldigKontonummerMod11(kontonummer.norskKontonummer!!)) {
                return ResponseEntity.badRequest().body("Det er angitt et ugyldig norsk kontonummer.")
            }
        }
        if (kontonummer.valutakode == null) {
            return ResponseEntity.badRequest().body("Valutakode må angis.")
        }
        if (kontonummer.landkodeBank != null && kontonummer.landkodeBank?.gyldig() == false) {
            return ResponseEntity
                .badRequest()
                .body("Landkode for bank ${kontonummer.landkodeBank?.verdi} må ha 3 tegn.")
        }
        if (kontonummer.norskKontonummer.isNullOrBlank() &&
            kontonummer.iban.isNullOrBlank() &&
            kontonummer.swift.isNullOrBlank()
        ) {
            return ResponseEntity
                .badRequest()
                .body("Samhandleren må ha kontonummeropplysninger. Fyll inn enten norsk eller utenlandsk kontoinformasjon.")
        }
        return null
    }

    private fun validerAdresse(adresse: AdresseDto): ResponseEntity<*>? {
        if (adresse.adresselinje1.isNullOrBlank() &&
            (
                adresse.adresselinje2?.isNotEmpty() == true ||
                    adresse.adresselinje3?.isNotEmpty() == true
            )
        ) {
            return ResponseEntity
                .badRequest()
                .body("Adresselinje1 må fylles ut før adresselinje 2 eller adresselinje 3.")
        }
        if (adresse.adresselinje1?.isNotEmpty() == true) {
            if (adresse.land?.verdi.isNullOrBlank()) {
                return ResponseEntity.badRequest().body("Landkode med 3 tegn må angis for adresse.")
            }
            if (adresse.land?.gyldig() == false) {
                return ResponseEntity
                    .badRequest()
                    .body("Landkode ${adresse.land?.verdi} må ha 3 tegn, og kan ikke være blank.")
            }
            if (adresse.land?.equals(Landkode3("NOR")) == true && (adresse.postnr.isNullOrBlank() || adresse.poststed.isNullOrBlank())) {
                return ResponseEntity
                    .badRequest()
                    .body("Postnummer og poststed må angis for norske adresser.")
            }
        }
        return null
    }
}
