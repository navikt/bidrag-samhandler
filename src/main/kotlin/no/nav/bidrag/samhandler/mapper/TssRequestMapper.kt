package no.nav.bidrag.samhandler.mapper

import no.nav.bidrag.domain.ident.Ident
import no.nav.bidrag.domain.ident.Identtype
import no.nav.bidrag.samhandler.service.TssService
import no.nav.bidrag.transport.samhandler.SøkSamhandlerQuery
import no.rtv.namespacetss.SamhandlerIDataB910Type
import no.rtv.namespacetss.SamhandlerIDataB940Type
import no.rtv.namespacetss.TServicerutiner
import no.rtv.namespacetss.TidOFF1
import no.rtv.namespacetss.TssSamhandlerData

object TssRequestMapper {

    fun createTssSamhandlerRequest(samhandlerId: Ident): TssSamhandlerData {
        val samhandlerIData = createSamhandlerIDataB910Type(samhandlerId)
        val servicerutiner = TServicerutiner().apply { samhandlerIDataB910 = samhandlerIData }
        return lagTssSamhandlerData(servicerutiner)
    }

    fun createSamhandlersøkRequest(søkSamhandlerQuery: SøkSamhandlerQuery): TssSamhandlerData {
        val samhandlerIDataB940Data = getSamhandlerIDataB940Data(søkSamhandlerQuery)
        val servicerutiner = TServicerutiner().apply { samhandlerIDataB940 = samhandlerIDataB940Data }
        return lagTssSamhandlerData(servicerutiner)
    }

    private fun lagTssSamhandlerData(servicerutiner: TServicerutiner): TssSamhandlerData {
        val inputData = TssSamhandlerData.TssInputData().apply { tssServiceRutine = servicerutiner }
        return TssSamhandlerData().apply { tssInputData = inputData }
    }

    private fun getSamhandlerIDataB940Data(søkSamhandlerQuery: SøkSamhandlerQuery): SamhandlerIDataB940Type {
        return SamhandlerIDataB940Type().apply {
            brukerID = TssService.BRUKER_ID
            navnSamh = søkSamhandlerQuery.navn
            kodeSamhType = "KRED"
            postNr = søkSamhandlerQuery.postnummer
            omrade = søkSamhandlerQuery.område
            buffnr = søkSamhandlerQuery.side.toString().padStart(3, '0')
        }
    }

    private fun createSamhandlerIDataB910Type(ident: Ident) = SamhandlerIDataB910Type()
        .apply {
            brukerID = TssService.BRUKER_ID
            idOffTSS = if (ident.erSamhandlerId()) ident.verdi else null
            ofFid = createTidOFF1(ident)
            historikk = "N"
            brukerID = "RTV9999"
        }

    private fun createTidOFF1(ident: Ident): TidOFF1? =
        when (ident.type()) {
            Identtype.PersonIdent -> TidOFF1().apply {
                idOff = ident.verdi
                kodeIdType = "FNR"
            }
            Identtype.Organisasjonsnummer -> TidOFF1().apply {
                idOff = ident.verdi
                kodeIdType = "ORG"
                kodeSamhType = "INST"
            }
            Identtype.SamhandlerId -> null
            Identtype.Ukjent -> throw IllegalArgumentException("Ukjent identtype ${ident.type()}")
        }
}
