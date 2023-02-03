// package no.nav.bidrag.samhandler.controller
//
// import io.swagger.v3.oas.annotations.Operation
// import io.swagger.v3.oas.annotations.security.SecurityRequirement
// import no.nav.bidrag.samhandler.controller.dto.SamhandlerDto
// import no.nav.bidrag.samhandler.service.TssServiceImpl
// import no.nav.domain.ident.SamhandlerId
// import no.nav.security.token.support.core.api.Protected
// import org.springframework.web.bind.annotation.GetMapping
// import org.springframework.web.bind.annotation.PathVariable
// import org.springframework.web.bind.annotation.RestController
//
// @RestController
// @Protected
// class ExampleController(val tssServiceImpl: TssServiceImpl) {
//
//    @GetMapping("/samhandler/{samhandlerId}")
//    @Operation(
//        description = "Henter samhandler for samhandlerId",
//        security = [SecurityRequirement(name = "bearer-key")]
//    )
//    fun hentDialog(@PathVariable samhandlerId: SamhandlerId): SamhandlerDto? {
//        return tssServiceImpl.hentSamhandler(samhandlerId)
//    }
//
// }
