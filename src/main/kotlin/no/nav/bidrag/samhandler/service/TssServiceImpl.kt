package no.nav.bidrag.samhandler.service

import no.nav.bidrag.samhandler.controller.dto.AdresseDTO
import no.nav.bidrag.samhandler.controller.dto.KontonummerDTO
import no.nav.bidrag.samhandler.controller.dto.SamhandlerDto
import no.nav.bidrag.samhandler.exception.AktoerNotFoundException
import no.nav.bidrag.samhandler.exception.TSSServiceException
import no.nav.bidrag.samhandler.integration.MQClient
import no.nav.bidrag.samhandler.config.MQProperties
import no.nav.domain.felles.Verdiobjekt
import no.nav.domain.ident.PersonIdent
import no.nav.domain.ident.SamhandlerId
import no.rtv.namespacetss.Samhandler
import no.rtv.namespacetss.SamhandlerIDataB910Type
import no.rtv.namespacetss.SvarStatusType
import no.rtv.namespacetss.TServicerutiner
import no.rtv.namespacetss.TidOFF1
import no.rtv.namespacetss.TssSamhandlerData
import org.springframework.stereotype.Service

@Service
class TssServiceImpl(
    private val mqService: MQClient,
    private val mqProperties: MQProperties
) {

    fun hentSamhandler(samhandlerId: SamhandlerId): SamhandlerDto? {
        val request: TssSamhandlerData = createTssSamhandlerRequest(samhandlerId = samhandlerId)
        val response: TssSamhandlerData =
            mqService.performRequestResponseSpring(mqProperties.tssRequestQueue, request)
        validateResponse(response, samhandlerId)
        return mapToSamhandler(response)
    }

    fun hentSamhandler(personIdent: PersonIdent): SamhandlerDto? {
        val request: TssSamhandlerData = createTssSamhandlerRequest(personIdent = personIdent)
        val response: TssSamhandlerData =
            mqService.performRequestResponseSpring(mqProperties.tssRequestQueue, request)
        validateResponse(response, personIdent)
        return mapToSamhandler(response)
    }

    private fun createTssSamhandlerRequest(samhandlerId: SamhandlerId? = null, personIdent: PersonIdent? = null): TssSamhandlerData {
        val tidOFF1 = personIdent?.let {
            TidOFF1().apply {
                idOff = personIdent.verdi
                kodeIdType = "FNR"
            }
        }
        val samhandlerIData = SamhandlerIDataB910Type().apply {
            idOffTSS = samhandlerId?.verdi
            ofFid = tidOFF1
            historikk = "N"
            brukerID = "RTV9999"
        }
        val servicerutiner = TServicerutiner().apply { samhandlerIDataB910 = samhandlerIData }
        val inputData = TssSamhandlerData.TssInputData().apply { tssServiceRutine = servicerutiner }
        return TssSamhandlerData().apply { tssInputData = inputData }
    }

    private fun mapToSamhandler(tssSamhandlerData: TssSamhandlerData): SamhandlerDto? {
        val samhandler =
            tssSamhandlerData.tssOutputData.samhandlerODataB910?.enkeltSamhandler?.firstOrNull()
        return samhandler?.let {
            SamhandlerDto(
                samhandlerId = samhandler.samhandler110.samhandler.firstOrNull()?.brukerId,
                offentligId = getOffentligId(it),
                offentligIdType = getOffentligIdType(it),
                adresse = mapToAdresse(it),
                kontonummer = mapToKontonummer(it)
            )
        }
    }

    private fun getOffentligId(samhandler: Samhandler): String? {
        return samhandler.samhandler110.samhandler.firstOrNull()?.idOff
    }

    private fun getOffentligIdType(samhandler: Samhandler): String? {
        return samhandler.samhandler110.samhandler.firstOrNull()?.kodeIdentType
    }

    private fun mapToAdresse(samhandler: Samhandler): AdresseDTO? {
        val typeSamhAdr = samhandler.adresse130
        if (typeSamhAdr != null) {
            val navn = samhandler.samhandler110.samhandler.firstOrNull()?.navnSamh
            typeSamhAdr.adresseSamh.forEach {
                return AdresseDTO(
                    navn = navn,
                    land = it.kodeLand.trim(),
                    poststed = it.poststed.trim(),
                    postnr = it.postNr.trim(),
                    adresselinje1 = it.adrLinjeInfo?.adresseLinje?.firstOrNull()?.trim(),
                    adresselinje2 = it.adrLinjeInfo?.adresseLinje?.get(1)?.trim(),
                    adresselinje3 = it.adrLinjeInfo?.adresseLinje?.get(2)?.trim()
                )
            }
        }
        return null
    }

    private fun mapToKontonummer(samhandler: Samhandler): KontonummerDTO? {
        val kontoTypeInnland = samhandler.konto140.konto.firstOrNull { it.gironrInnland != null }
        val kontoTypeUtland = samhandler.konto140.konto.firstOrNull { it.gironrUtland != null }

        return (kontoTypeInnland ?: kontoTypeUtland)?.let {
            KontonummerDTO(
                bankLandkode = it.kodeLand,
                bankNavn = it.bankNavn,
                norskKontonr = it.gironrInnland,
                swift = it.swiftKode,
                valutaKode = it.kodeValuta,
                bankCode = it.bankKode,
                iban = it.gironrUtland
            )
        }
    }

    private fun validateResponse(tssSamhandlerData: TssSamhandlerData, verdiobjekt: Verdiobjekt<*>) {
        val svarStatusType: SvarStatusType = tssSamhandlerData.tssOutputData.svarStatus
        if (svarStatusType.alvorligGrad.equals("00")) {
            return
        } else if (svarStatusType.alvorligGrad.equals("04") && svarStatusType.kodeMelding.equals("B9XX008F")) {
            throw AktoerNotFoundException("Samhandler for $verdiobjekt ikke funnet.")
        }
        throw TSSServiceException(svarStatusType.beskrMelding + " " + svarStatusType.kodeMelding)
    }
}
