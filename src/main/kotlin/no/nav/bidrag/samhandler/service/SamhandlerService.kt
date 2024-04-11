package no.nav.bidrag.samhandler.service

import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.samhandler.mapper.SamhandlerMapper
import no.nav.bidrag.samhandler.persistence.repository.SamhandlerRepository
import no.nav.bidrag.samhandler.persistence.repository.SamhandlerSøkSpec
import no.nav.bidrag.transport.samhandler.SamhandlerDto
import no.nav.bidrag.transport.samhandler.SamhandlerSøk
import no.nav.bidrag.transport.samhandler.SamhandlersøkeresultatDto
import no.nav.bidrag.transport.samhandler.SøkSamhandlerQuery
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SamhandlerService(
    private val tssService: TssService,
    private val samhandlerRepository: SamhandlerRepository,
) {
    fun hentSamhandler(ident: Ident): SamhandlerDto? {
        val samhandler = samhandlerRepository.findByIdent(ident.verdi)
        if (samhandler != null) {
            return SamhandlerMapper.mapTilSamhandlerDto(samhandler)
        }

        val hentSamhandler = tssService.hentSamhandler(ident)
        hentSamhandler?.let { samhandlerRepository.save(SamhandlerMapper.mapTilSamhandler(hentSamhandler, true)) }
        return hentSamhandler
    }

    @Deprecated("Søker mot tss med gammel query.", replaceWith = ReplaceWith("samhandlerService.samhandlerSøk(samhandlerSøk)"))
    fun søkSamhandler(søkSamhandlerQuery: SøkSamhandlerQuery): SamhandlersøkeresultatDto {
        val samhandlere =
            søkSamhandlerQuery.postnummer?.let {
                samhandlerRepository.findAllByNavnIgnoreCaseAndPostnr(søkSamhandlerQuery.navn, søkSamhandlerQuery.postnummer)
            }
                ?: samhandlerRepository.findAllByNavnIgnoreCase(søkSamhandlerQuery.navn)

        if (samhandlere.isNotEmpty()) {
            return SamhandlerMapper.mapTilSamhandlersøkeresultatDto(samhandlere)
        }

        return tssService.søkSamhandler(søkSamhandlerQuery)
    }

    fun samhandlerSøk(samhandlerSøk: SamhandlerSøk): SamhandlersøkeresultatDto {
        val samhandlere = samhandlerRepository.findAll(SamhandlerSøkSpec.søkPåAlleParameter(samhandlerSøk))

        return SamhandlerMapper.mapTilSamhandlersøkeresultatDto(samhandlere)
    }

    @Transactional
    fun opprettSamhandler(samhandlerDto: SamhandlerDto) {
        val samhandler = SamhandlerMapper.mapTilSamhandler(samhandlerDto)
        samhandlerRepository.save(samhandler)
    }

    @Transactional
    fun oppdaterSamhandler(samhandlerDto: SamhandlerDto): ResponseEntity<*> {
        val samhandlerIdent =
            samhandlerDto.tssId?.verdi ?: return ResponseEntity.badRequest()
                .body("Oppdatering av samhandler må ha angitt samhandlerId!")
        val samhandler = samhandlerRepository.findByIdent(samhandlerIdent) ?: return ResponseEntity.notFound().build<Any>()

        val oppdatertSamhandler =
            samhandler.copy(
                navn =
                    samhandlerDto.navn ?: return ResponseEntity.badRequest()
                        .body("Navn kan ikke være tomt! Mangler navn fra TSS må dette opprettes."),
                offentligId = samhandlerDto.offentligId,
                offentligIdType = samhandlerDto.offentligIdType,
                områdekode = samhandlerDto.områdekode?.name,
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
            )
        samhandlerRepository.save(oppdatertSamhandler)
        return ResponseEntity.ok().build<Any>()
    }
}
