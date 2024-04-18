package no.nav.bidrag.samhandler.controller

import io.swagger.v3.oas.annotations.Operation
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
import org.springframework.web.bind.annotation.PutMapping
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
    fun hentSamhandler(
        @RequestBody ident: Ident,
    ): SamhandlerDto? {
        return samhandlerService.hentSamhandler(ident)
    }

    @GetMapping("/samhandler")
    @Operation(
        description = "Søker etter samhandlere basert på navn, område og postnummer",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @Deprecated(message = "Søk på samhandler mot TSS.", replaceWith = ReplaceWith("/samhandlersok"))
    fun søkSamhandler(søkSamhandlerQuery: SøkSamhandlerQuery): SamhandlersøkeresultatDto {
        return samhandlerService.søkSamhandler(søkSamhandlerQuery)
    }

    @PostMapping("/samhandlersok")
    @Operation(
        description = "Søker etter samhandlere.",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    fun samhandlerSøk(samhandlerSøk: SamhandlerSøk): SamhandlersøkeresultatDto {
        return samhandlerService.samhandlerSøk(samhandlerSøk)
    }

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

    @PutMapping("/samhandler")
    @Operation(
        description = "Oppdaterer samhandler.",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    fun oppdaterSamhandler(
        @RequestBody samhandlerDto: SamhandlerDto,
    ): ResponseEntity<*> {
        return samhandlerService.oppdaterSamhandler(samhandlerDto)
    }
}
