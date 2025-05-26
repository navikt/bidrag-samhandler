package no.nav.bidrag.samhandler.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.samhandler.service.TssService
import no.nav.security.token.support.core.api.Protected
import no.rtv.namespacetss.TssSamhandlerData
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
@Deprecated("TSS-integrasjon skal fjernes.")
class TssController(
    private val tssService: TssService,
) {
    @PostMapping("/samhandlerData")
    @Operation(
        description =
            "Henter samhandler for ident og lever ut all data på identen slik det hentes fra TSS." +
                "Denne tjenesten lagrer IKKE i databasen.",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @Deprecated(message = "Bruk hentSamhandler i stedet", replaceWith = ReplaceWith("/samhandler"))
    fun hentSamhandlerData(
        @RequestBody ident: Ident,
    ): TssSamhandlerData = tssService.hentSamhandlerData(ident)
}
