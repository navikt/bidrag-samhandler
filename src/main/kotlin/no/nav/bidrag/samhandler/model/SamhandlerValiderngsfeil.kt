@file:Suppress("ktlint:standard:filename")

package no.nav.bidrag.samhandler.model

data class SamhandlerValideringsfeil(
    val duplikatSamhandler: DuplikatSamhandler? = null,
    val ugyldigInput: Map<String, String>? = null,
)

data class DuplikatSamhandler(
    val feilmelding: String,
    val eksisterendeSamhandlerId: String,
    val felter: List<String> = emptyList(),
)
