package no.nav.bidrag.samhandler.service

import jakarta.persistence.EntityManager
import no.nav.bidrag.commons.security.utils.TokenUtils
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.samhandler.SECURE_LOGGER
import no.nav.bidrag.samhandler.mapper.SamhandlerMapper
import no.nav.bidrag.samhandler.persistence.repository.SamhandlerRepository
import no.nav.bidrag.samhandler.persistence.repository.SamhandlerSøkSpec
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
                    samhandlerDto.navn ?: return ResponseEntity
                        .badRequest()
                        .body("Navn kan ikke være tomt!"),
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
}
