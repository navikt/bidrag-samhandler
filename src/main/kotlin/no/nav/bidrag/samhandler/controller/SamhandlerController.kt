package no.nav.bidrag.samhandler.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.samhandler.service.SamhandlerService
import no.nav.bidrag.transport.samhandler.SamhandlerDto
import no.nav.bidrag.transport.samhandler.SamhandlerSøk
import no.nav.bidrag.transport.samhandler.SamhandlersøkeresultatDto
import no.nav.bidrag.transport.samhandler.SøkSamhandlerQuery
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class SamhandlerController(
    private val samhandlerService: SamhandlerService,
) {
    @PostMapping("/samhandler")
    @Operation(
        description = "Henter samhandler for ident.",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Returnerer samhandler.",
                content = [
                    ((Content(schema = Schema(implementation = SamhandlerDto::class)))),
                ],
            ),
            ApiResponse(
                responseCode = "204",
                description = "Samhandler finnes ikke.",
            ),
        ],
    )
    fun hentSamhandler(
        @RequestBody ident: Ident,
    ): ResponseEntity<*> {
        val samhandler = samhandlerService.hentSamhandler(ident) ?: return ResponseEntity.notFound().build<Any>()
        return ResponseEntity.ok(samhandler)
    }

    @GetMapping("/samhandler")
    @Operation(
        description = "Søker etter samhandlere basert på navn, område og postnummer",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @Deprecated(message = "Søk på samhandler mot TSS.", replaceWith = ReplaceWith("/samhandlersok"))
    fun søkSamhandler(søkSamhandlerQuery: SøkSamhandlerQuery): SamhandlersøkeresultatDto =
        samhandlerService.søkSamhandler(søkSamhandlerQuery)

    @PostMapping("/samhandlersok")
    @Operation(
        description = "Søker etter samhandlere.",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    fun samhandlerSøk(
        @RequestBody samhandlerSøk: SamhandlerSøk,
    ): SamhandlersøkeresultatDto = samhandlerService.samhandlerSøk(samhandlerSøk)

    @PostMapping("/opprettSamhandler")
    @Operation(
        description = "Oppretter samhandler.",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    fun opprettSamhandler(
        @RequestBody samhandlerDto: SamhandlerDto,
    ): ResponseEntity<*> {
        val samhandlerId = samhandlerService.opprettSamhandler(samhandlerDto)
        return ResponseEntity.ok(samhandlerService.hentSamhandlerPåId(samhandlerId))
    }

    @PostMapping("/oppdaterSamhandler")
    @Operation(
        description = "Oppdaterer samhandler.",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    fun oppdaterSamhandler(
        @RequestBody samhandlerDto: SamhandlerDto,
    ): ResponseEntity<*> = samhandlerService.oppdaterSamhandler(samhandlerDto)
}
