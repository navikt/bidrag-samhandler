package no.nav.bidrag.samhandler.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.samhandler.service.TssService
import no.nav.bidrag.transport.samhandler.SamhandlerDto
import no.nav.bidrag.transport.samhandler.SamhandlersøkeresultatDto
import no.nav.bidrag.transport.samhandler.SøkSamhandlerQuery
import no.nav.security.token.support.core.api.Protected
import no.rtv.namespacetss.TssSamhandlerData
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class SamhandlerController(val tssService: TssService) {
    @PostMapping("/samhandler")
    @Operation(
        description = "Henter samhandler for ident",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    fun hentSamhandler(
        @RequestBody ident: Ident,
    ): SamhandlerDto? {
        return tssService.hentSamhandler(ident)
    }

    @PostMapping("/samhandlerData")
    @Operation(
        description = "Henter samhandler for ident og lever ut all data på identen slik det hentes fra TSS",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    fun hentSamhandlerData(
        @RequestBody ident: Ident,
    ): TssSamhandlerData {
        return tssService.hentSamhandlerData(ident)
    }

    @GetMapping("/samhandler")
    @Operation(
        description = "Søker etter samhandlere basert på navn, område og postnummer",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    fun søkSamhandler(søkSamhandlerQuery: SøkSamhandlerQuery): SamhandlersøkeresultatDto {
        return tssService.søkSamhandler(søkSamhandlerQuery)
    }
}
