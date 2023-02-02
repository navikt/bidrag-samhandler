package no.nav.bidrag.samhandler.controller.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
class SamhandlerDto(
    @Schema(description = "Id for aktøren")
    val samhandlerId: String?,

    @Schema(description = "Offentlig id for samhandlere. Angis ikke for personer.")
    val offentligId: String? = null,

    @Schema(description = "Type offentlig id. F.eks ORG for norske organisasjonsnummere.")
    val offentligIdType: String? = null,

    @Schema(description = "Aktørens adresse. Angis ikke for personer.")
    val adresse: AdresseDTO? = null,

    @Schema(description = "Aktørens kontonummer.")
    val kontonummer: KontonummerDTO? = null
)
