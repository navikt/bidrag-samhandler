package no.nav.bidrag.samhandler.service

import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.samhandler.config.MQProperties
import no.nav.bidrag.samhandler.exception.SamhandlerNotFoundException
import no.nav.bidrag.samhandler.exception.TSSServiceException
import no.nav.bidrag.samhandler.integration.MqClient
import no.nav.bidrag.samhandler.mapper.SamhandlerMapper
import no.nav.bidrag.samhandler.mapper.TssRequestMapper.createSamhandlersøkRequest
import no.nav.bidrag.samhandler.mapper.TssRequestMapper.createTssSamhandlerRequest
import no.nav.bidrag.transport.samhandler.SamhandlerDto
import no.nav.bidrag.transport.samhandler.SamhandlersøkeresultatDto
import no.nav.bidrag.transport.samhandler.SøkSamhandlerQuery
import no.rtv.namespacetss.TssSamhandlerData
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class TssService(
    private val mqService: MqClient,
    private val mqProperties: MQProperties,
) {
    @Cacheable("Samhandler")
    fun hentSamhandler(ident: Ident): SamhandlerDto? {
        val request = createTssSamhandlerRequest(ident)
        val response: TssSamhandlerData = mqService.performRequestResponseSpring(mqProperties.tssRequestQueue, request)
        validateResponse(response, ident)

        return SamhandlerMapper.mapTilSamhandler(response, ident)
    }

    fun hentSamhandlerData(ident: Ident): TssSamhandlerData {
        val request = createTssSamhandlerRequest(ident)
        val response: TssSamhandlerData = mqService.performRequestResponseSpring(mqProperties.tssRequestQueue, request)
        validateSøkResponse(response, ident)

        return response
    }

    fun søkSamhandler(søkSamhandlerQuery: SøkSamhandlerQuery): SamhandlersøkeresultatDto {
        val request = createSamhandlersøkRequest(søkSamhandlerQuery)
        val response: TssSamhandlerData = mqService.performRequestResponseSpring(mqProperties.tssRequestQueue, request)
        val flereForekomster = validateSøkResponse(response, søkSamhandlerQuery.toString())
        return SamhandlersøkeresultatDto(SamhandlerMapper.mapTilSamhandlersøkeresultat(response), flereForekomster)
    }

    private fun validateResponse(
        tssSamhandlerData: TssSamhandlerData,
        verdi: Any,
    ) {
        val svarstatus = tssSamhandlerData.tssOutputData.svarStatus
        if (svarstatus.alvorligGrad != TSS_STATUS_OK) {
            if (svarstatus.kodeMelding == KODEMELDING_INGEN_FUNNET) {
                throw SamhandlerNotFoundException("Ingen treff med med inputData=$verdi")
            }
            throw TSSServiceException("${svarstatus.beskrMelding} - ${svarstatus.alvorligGrad} - ${svarstatus.kodeMelding}")
        }
        if (tssSamhandlerData.tssOutputData.ingenReturData != null) {
            throw SamhandlerNotFoundException("Ingen returdata for TSS request med inputData=$verdi")
        }
    }

    private fun validateSøkResponse(
        tssSamhandlerData: TssSamhandlerData,
        verdi: Any,
    ): Boolean {
        val svarstatus = tssSamhandlerData.tssOutputData.svarStatus
        if (svarstatus.alvorligGrad != TSS_STATUS_OK) {
            if (svarstatus.kodeMelding == KODEMELDING_INGEN_FUNNET) {
                throw SamhandlerNotFoundException("Ingen treff med med inputData=$verdi")
            }
            if (svarstatus.kodeMelding == KODEMELDING_MER_INFO) return true
            if (svarstatus.kodeMelding == KODEMELDING_INGEN_FLERE_FOREKOMSTER) return false
            throw TSSServiceException("${svarstatus.beskrMelding} - ${svarstatus.alvorligGrad} - ${svarstatus.kodeMelding}")
        }
        if (tssSamhandlerData.tssOutputData.ingenReturData != null) {
            throw SamhandlerNotFoundException("Ingen returdata for TSS request med inputData=$verdi")
        }
        return false
    }

    companion object {
        const val KODEMELDING_INGEN_FUNNET = "B9XX008F"
        const val KODEMELDING_MER_INFO = "B9XX018I"
        const val KODEMELDING_INGEN_FLERE_FOREKOMSTER = "B9XX021I"
        const val TSS_STATUS_OK = "00"
        const val BRUKER_ID = "bidrag-samhandler"
    }
}
