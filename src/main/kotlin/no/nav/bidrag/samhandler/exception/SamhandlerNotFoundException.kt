package no.nav.bidrag.samhandler.exception

import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpStatusCodeException

class SamhandlerNotFoundException(message: String) : HttpStatusCodeException(HttpStatus.NOT_FOUND, message)
