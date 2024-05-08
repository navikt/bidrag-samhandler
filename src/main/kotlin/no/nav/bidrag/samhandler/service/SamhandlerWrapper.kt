package no.nav.bidrag.samhandler.service

import no.nav.bidrag.transport.samhandler.SamhandlerDto

data class SamhandlerWrapper(
    val samhandlerDto: SamhandlerDto,
    val erOpphørt: Boolean,
)
