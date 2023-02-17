package no.nav.bidrag.samhandler.exception

class MQServiceException : Exception {
    constructor(message: String?) : super(message)
    constructor(message: String?, error: Throwable?) : super(message, error)
}
