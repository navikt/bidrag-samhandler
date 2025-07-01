package no.nav.bidrag.samhandler.service

import jakarta.persistence.EntityManager
import no.nav.bidrag.commons.security.utils.TokenUtils
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.domene.land.Landkode3
import no.nav.bidrag.samhandler.SECURE_LOGGER
import no.nav.bidrag.samhandler.mapper.SamhandlerMapper
import no.nav.bidrag.samhandler.model.DuplikatSamhandler
import no.nav.bidrag.samhandler.model.SamhandlerValideringsfeil
import no.nav.bidrag.samhandler.persistence.repository.SamhandlerRepository
import no.nav.bidrag.samhandler.persistence.repository.SamhandlerSøkSpec
import no.nav.bidrag.samhandler.util.DuplikatSamhandlerMap
import no.nav.bidrag.samhandler.util.KontonummerUtils
import no.nav.bidrag.samhandler.util.add
import no.nav.bidrag.samhandler.util.getPath
import no.nav.bidrag.samhandler.util.nullIfEmpty
import no.nav.bidrag.transport.felles.commonObjectmapper
import no.nav.bidrag.transport.samhandler.AdresseDto
import no.nav.bidrag.transport.samhandler.KontonummerDto
import no.nav.bidrag.transport.samhandler.SamhandlerDto
import no.nav.bidrag.transport.samhandler.SamhandlerKafkaHendelsestype
import no.nav.bidrag.transport.samhandler.SamhandlerSøk
import no.nav.bidrag.transport.samhandler.SamhandlersøkeresultatDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
import java.nio.charset.Charset
import kotlin.collections.component1
import kotlin.collections.component2
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
        validerOpprettSamhandlerIkkeFinnesFraFør(samhandlerDto)
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

    private fun validerOpprettSamhandlerIkkeFinnesFraFør(samhandlerDto: SamhandlerDto) {
        val samhandlerMedSammeOffentligId = samhandlerRepository.findAllByOffentligId(samhandlerDto.offentligId)
        if (samhandlerMedSammeOffentligId.isEmpty()) return
        val identtiskeSamhandlere: DuplikatSamhandlerMap = mutableMapOf()
        samhandlerMedSammeOffentligId.forEach {
            if (it.norskkontonr != null && it.norskkontonr == samhandlerDto.kontonummer?.norskKontonummer.nullIfEmpty()) {
                identtiskeSamhandlere.add(it.ident!!, "kontonummer.norskKontonummer")
            }
            if (it.iban != null && it.iban == samhandlerDto.kontonummer?.iban.nullIfEmpty()) {
                identtiskeSamhandlere.add(it.ident!!, "kontonummer.iban")
            }
        }

        if (identtiskeSamhandlere.isNotEmpty()) {
            throw HttpClientErrorException(
                HttpStatus.CONFLICT,
                "Feil ved opprettelse av samhandler: Samhandler med samme offentlig ID og kontonummer finnes fra før.",
                commonObjectmapper.writeValueAsBytes(
                    SamhandlerValideringsfeil(
                        identtiskeSamhandlere.entries.first().let { (samhandlerId, felter) ->
                            DuplikatSamhandler(
                                "Samhandler med samme offentlig ID og kontonummer finnes fra før",
                                samhandlerId,
                                felter,
                            )
                        },
                    ),
                ),
                Charset.defaultCharset(),
            )
        }
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
                norskkontonr = samhandlerDto.kontonummer?.norskKontonummer.nullIfEmpty(),
                iban = samhandlerDto.kontonummer?.iban.nullIfEmpty(),
                swift = samhandlerDto.kontonummer?.swift.nullIfEmpty(),
                banknavn = samhandlerDto.kontonummer?.banknavn.nullIfEmpty(),
                banklandkode =
                    samhandlerDto.kontonummer
                        ?.landkodeBank
                        ?.verdi
                        .nullIfEmpty(),
                valutakode = samhandlerDto.kontonummer?.valutakode,
                bankcode = samhandlerDto.kontonummer?.bankCode.nullIfEmpty(),
                adresselinje1 = samhandlerDto.adresse?.adresselinje1.nullIfEmpty(),
                adresselinje2 = samhandlerDto.adresse?.adresselinje2.nullIfEmpty(),
                adresselinje3 = samhandlerDto.adresse?.adresselinje3.nullIfEmpty(),
                postnr = samhandlerDto.adresse?.postnr.nullIfEmpty(),
                poststed = samhandlerDto.adresse?.poststed.nullIfEmpty(),
                land =
                    samhandlerDto.adresse
                        ?.land
                        ?.verdi
                        .nullIfEmpty(),
                kontaktperson = samhandlerDto.kontaktperson.nullIfEmpty(),
                kontaktEpost = samhandlerDto.kontaktEpost.nullIfEmpty(),
                kontaktTelefon = samhandlerDto.kontaktTelefon.nullIfEmpty(),
                notat = samhandlerDto.notat.nullIfEmpty(),
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

    fun validerInput(
        samhandlerDto: SamhandlerDto,
        opprettSamhandler: Boolean = false,
    ) {
        val valideringsfeil: MutableMap<String, String> = mutableMapOf()
        if (samhandlerDto.adresse != null) {
            validerAdresse(samhandlerDto.adresse!!, valideringsfeil)
        }
        if (samhandlerDto.kontonummer != null) {
            validerKontonummer(samhandlerDto.kontonummer!!, valideringsfeil)
        }
        if (samhandlerDto.språk == null) {
            valideringsfeil.put(
                getPath(SamhandlerDto::språk),
                "Språk må angis.",
            )
        }
        val samhandlerIdInput = samhandlerDto.samhandlerId
        if (opprettSamhandler && samhandlerIdInput != null && samhandlerIdInput.verdi.isNotEmpty()) {
            valideringsfeil.put(
                getPath(SamhandlerDto::samhandlerId),
                "Kan ikke sette samhandlerId ved opprettelse av samhandler.",
            )
        }
        if (valideringsfeil.isNotEmpty()) {
            throw HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Feil ved ${if (opprettSamhandler) "opprettelse" else "oppdatering"} av samhandler ${samhandlerDto.samhandlerId ?: ""}",
                commonObjectmapper.writeValueAsBytes(
                    SamhandlerValideringsfeil(
                        ugyldigInput = valideringsfeil,
                    ),
                ),
                Charset.defaultCharset(),
            )
        }
    }

    private fun validerKontonummer(
        kontonummer: KontonummerDto,
        valideringsfeil: MutableMap<String, String>,
    ) {
        if (kontonummer.norskKontonummer?.isNotEmpty() == true) {
            if (kontonummer.norskKontonummer?.length != 11) {
                valideringsfeil.put(
                    getPath(SamhandlerDto::kontonummer, KontonummerDto::norskKontonummer),
                    "Norsk kontonummer må være 11 tegn langt.",
                )
            }
            if (!KontonummerUtils.erGyldigKontonummerMod11(kontonummer.norskKontonummer!!)) {
                valideringsfeil.put(
                    getPath(SamhandlerDto::kontonummer, KontonummerDto::norskKontonummer),
                    "Det er angitt et ugyldig norsk kontonummer.",
                )
            }
        }
        if (kontonummer.valutakode == null) {
            valideringsfeil.put(
                getPath(SamhandlerDto::kontonummer, KontonummerDto::valutakode),
                "Valutakode må angis.",
            )
        }
        if (kontonummer.landkodeBank != null && kontonummer.landkodeBank?.gyldig() == false) {
            valideringsfeil.put(
                getPath(SamhandlerDto::kontonummer, KontonummerDto::landkodeBank),
                "Landkode for bank ${kontonummer.landkodeBank?.verdi} må ha 3 tegn.",
            )
        }
        if (kontonummer.norskKontonummer.isNullOrBlank() &&
            kontonummer.iban.isNullOrBlank() &&
            kontonummer.swift.isNullOrBlank()
        ) {
            valideringsfeil.put(
                getPath(SamhandlerDto::kontonummer, KontonummerDto::norskKontonummer),
                "Samhandleren må ha kontonummeropplysninger. Fyll inn enten norsk eller utenlandsk kontoinformasjon.",
            )
            valideringsfeil.put(
                getPath(SamhandlerDto::kontonummer, KontonummerDto::iban),
                "Samhandleren må ha kontonummeropplysninger. Fyll inn enten norsk eller utenlandsk kontoinformasjon.",
            )
        }
    }

    private fun validerAdresse(
        adresse: AdresseDto,
        valideringsfeil: MutableMap<String, String>,
    ) {
        if (adresse.adresselinje1.isNullOrBlank() &&
            (
                adresse.adresselinje2?.isNotEmpty() == true ||
                    adresse.adresselinje3?.isNotEmpty() == true
            )
        ) {
            valideringsfeil.put(
                getPath(SamhandlerDto::adresse, AdresseDto::adresselinje1),
                "Adresselinje1 må fylles ut før adresselinje 2 eller adresselinje 3.",
            )
        }
        if (adresse.adresselinje1?.isNotEmpty() == true) {
            if (adresse.land?.verdi.isNullOrBlank()) {
                valideringsfeil.put(
                    getPath(SamhandlerDto::adresse, AdresseDto::land),
                    "Landkode med 3 tegn må angis for adresse.",
                )
            }
            if (adresse.land?.gyldig() == false) {
                valideringsfeil.put(
                    getPath(SamhandlerDto::adresse, AdresseDto::land),
                    "Landkode ${adresse.land?.verdi} må ha 3 tegn, og kan ikke være blank.",
                )
            }
            if (adresse.land?.equals(Landkode3("NOR")) == true && (adresse.postnr.isNullOrBlank() || adresse.poststed.isNullOrBlank())) {
                valideringsfeil.put(
                    getPath(SamhandlerDto::adresse, AdresseDto::postnr),
                    "Postnummer og poststed må angis for norske adresser.",
                )
                valideringsfeil.put(
                    getPath(SamhandlerDto::adresse, AdresseDto::poststed),
                    "Postnummer og poststed må angis for norske adresser.",
                )
            }
        }
    }
}
