package no.nav.bidrag.samhandler.exception

@Deprecated("TSS-integrasjon skal fjernes.")
class MQServiceException : Exception {
    constructor(message: String?) : super(message)
    constructor(message: String?, error: Throwable?) : super(message, error)
}
