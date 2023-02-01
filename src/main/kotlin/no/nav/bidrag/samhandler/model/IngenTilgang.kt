package no.nav.bidrag.samhandler.model

import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
fun ingenTilgang(message: String): Nothing = throw HttpClientErrorException(HttpStatus.FORBIDDEN, message)
