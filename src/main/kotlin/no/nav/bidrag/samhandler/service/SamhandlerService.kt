package no.nav.bidrag.samhandler.service

import no.nav.bidrag.commons.security.utils.TokenUtils
import no.nav.bidrag.commons.web.MdcConstants
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.samhandler.SECURE_LOGGER
import no.nav.bidrag.samhandler.kafka.SamhandlerProducer
import no.nav.bidrag.samhandler.mapper.SamhandlerMapper
import no.nav.bidrag.samhandler.persistence.entity.Samhandler
import no.nav.bidrag.samhandler.persistence.repository.SamhandlerRepository
import no.nav.bidrag.samhandler.persistence.repository.SamhandlerSøkSpec
import no.nav.bidrag.transport.samhandler.SamhandlerDto
import no.nav.bidrag.transport.samhandler.SamhandlerKafkaHendelsestype
import no.nav.bidrag.transport.samhandler.SamhandlerSøk
import no.nav.bidrag.transport.samhandler.Samhandlerhendelse
import no.nav.bidrag.transport.samhandler.SamhandlersøkeresultatDto
import no.nav.bidrag.transport.samhandler.SøkSamhandlerQuery
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class SamhandlerService(
    private val tssService: TssService,
    private val samhandlerRepository: SamhandlerRepository,
    private val samhandlerProducer: SamhandlerProducer,
) {
    fun hentSamhandler(
        ident: Ident,
        inkluderAuditLog: Boolean,
    ): SamhandlerDto? {
        val samhandler = samhandlerRepository.findByIdent(ident.verdi)
        if (samhandler != null) {
            return SamhandlerMapper.mapTilSamhandlerDto(samhandler, inkluderAuditLog)
        }

        val hentetSamhandler = tssService.hentSamhandler(ident)
        hentetSamhandler?.let {
            val mapTilSamhandler = SamhandlerMapper.mapTilSamhandler(hentetSamhandler, true, hentetSamhandler.erOpphørt)
            val opprettetSamhandler = samhandlerRepository.save(mapTilSamhandler)
            sendKafkamelding(opprettetSamhandler, SamhandlerKafkaHendelsestype.OPPRETTET)
        }
        return hentetSamhandler
    }

    @Deprecated(
        "Søker mot tss med gammel query.",
        replaceWith = ReplaceWith("samhandlerService.samhandlerSøk(samhandlerSøk)"),
    )
    fun søkSamhandler(søkSamhandlerQuery: SøkSamhandlerQuery): SamhandlersøkeresultatDto {
        val samhandlere =
            søkSamhandlerQuery.postnummer?.let {
                samhandlerRepository.findAllByNavnIgnoreCaseAndPostnr(
                    søkSamhandlerQuery.navn,
                    søkSamhandlerQuery.postnummer,
                )
            }
                ?: samhandlerRepository.findAllByNavnIgnoreCase(søkSamhandlerQuery.navn)

        if (samhandlere.isNotEmpty()) {
            return SamhandlerMapper.mapTilSamhandlersøkeresultatDto(samhandlere)
        }
        return tssService.søkSamhandler(søkSamhandlerQuery)
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun opprettSamhandler(samhandlerDto: SamhandlerDto): Int {
        val samhandler = SamhandlerMapper.mapTilSamhandler(samhandlerDto)
        SECURE_LOGGER.info(
            "OpprettSamhandler for {} utført av {} med følgende data: {}",
            samhandlerDto.navn,
            TokenUtils.hentSaksbehandlerIdent() ?: TokenUtils.hentApplikasjonsnavn() ?: "ukjent",
            samhandlerDto,
        )
        val opprettetSamhandler = samhandlerRepository.save(samhandler)

        sendKafkamelding(opprettetSamhandler, SamhandlerKafkaHendelsestype.OPPRETTET)

        return opprettetSamhandler.id
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
                        .body("Navn kan ikke være tomt! Mangler navn fra TSS må dette opprettes."),
                offentligId = samhandlerDto.offentligId,
                offentligIdType = samhandlerDto.offentligIdType,
                områdekode = samhandlerDto.områdekode?.name,
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
                erOpphørt = samhandlerDto.erOpphørt,
            )

        val oppdatertSamhander = samhandlerRepository.save(oppdatertSamhandler)
        sendKafkamelding(oppdatertSamhander, SamhandlerKafkaHendelsestype.OPPDATERT)

        SECURE_LOGGER.info(
            "OppdaterSamhandler for {} utført av {} fra data: {}",
            samhandlerDto.samhandlerId,
            TokenUtils.hentSaksbehandlerIdent() ?: TokenUtils.hentApplikasjonsnavn() ?: "ukjent",
            samhandlerDto,
        )

        return ResponseEntity.ok(SamhandlerMapper.mapTilSamhandlerDto(oppdatertSamhandler))
    }

    private fun sendKafkamelding(
        samhandler: Samhandler,
        hendelsestype: SamhandlerKafkaHendelsestype,
    ) {
        val samhandlerhendelse =
            Samhandlerhendelse(
                samhandler.ident!!,
                hendelsestype,
                MdcConstants.MDC_CALL_ID,
            )
        samhandlerProducer.sendSamhandlerMelding(samhandlerhendelse)
    }
}
