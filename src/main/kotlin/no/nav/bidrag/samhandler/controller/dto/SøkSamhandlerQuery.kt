package no.nav.bidrag.samhandler.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Queryfelter for søk etter samhandlere.")
data class SøkSamhandlerQuery(
    val navn: String,
    val postnummer: String? = null,
    val område: String? = null,
    @Schema(description = "Sidenummer med resultater man ønsker, hvis det finnes og man ønsker påfølgende resultater.")
    val side: Int = 0
)
