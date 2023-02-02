package no.nav.bidrag.samhandler.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Angir hvilken type identitetsnummer som benyttes for å identifisere aktøren.")
enum class IdenttypeDTO {
    @Schema(description = "PERSONNUMMER angir at identitetsnummeret som benyttes er enten et FNR eller et DNR.")
    PERSONNUMMER,

    @Schema(description = "AKTOERNUMMER angir at identitetsnummeret er en TSS-ident. A.k.a. en samhandler-id.")
    AKTOERNUMMER
}
