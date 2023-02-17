package no.nav.bidrag.samhandler.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

data class SamhandlerDto(
    val tssId: String,
    @Schema(description = "Navn på samhandler")
    val samhandlernavn: String?,

    @Schema(description = "Offentlig id for samhandlere.")
    val offentligId: String? = null,

    @Schema(description = "Type offentlig id. F.eks ORG for norske organisasjonsnummere.")
    val offentligIdType: String? = null,

    @Schema(description = "Aktørens adresse. Angis ikke for personer.")
    val adresse: AdresseDto? = null,

    @Schema(description = "Aktørens kontonummer.")
    val kontonummer: KontonummerDto? = null
)
