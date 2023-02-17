package no.nav.bidrag.samhandler.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Representerer kontonummer for en bidragsaktør. For norske kontonummer " +
        "er det kun norskKontornr som er utfyllt, ellers benyttes de andre feltene for utlandske kontonummer."
)
data class KontonummerDto(
    @Schema(description = "Norsk kontonummer, 11 siffer.")
    val norskKontonummer: String? = null,
    @Schema(description = "IBAN angir kontonummeret på et internasjonalt format.")
    val iban: String? = null,
    @Schema(description = "SWIFT angir banken på et internasjonalt format.")
    val swift: String? = null,
    @Schema(description = "Bankens navn.")
    val banknavn: String? = null,
    @Schema(description = "Bankens landkode. TODO: Bestemme representasjon av land. 3-sifret land-kode?")
    val landkodeBank: String? = null,
    @Schema(description = "BankCode. Format varierer.")
    val bankCode: String? = null,
    @Schema(description = "Kontoens valuta.")
    val valutakode: String? = null
)
