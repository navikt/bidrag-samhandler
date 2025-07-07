package no.nav.bidrag.samhandler.aop

import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.transport.felles.ifTrue
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpStatusCodeException

@RestControllerAdvice
class DefaultRestControllerAdvice {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ResponseBody
    @ExceptionHandler(HttpStatusCodeException::class)
    fun handleHttpClientErrorException(exception: HttpStatusCodeException): ResponseEntity<*> {
        val errorMessage = getErrorMessage(exception)
        logger.warn(errorMessage, exception)
        val hasBody = exception.responseBodyAsString.isNotEmpty()
        val payloadFeilmelding =
            exception.responseBodyAsString.isEmpty().ifTrue { exception.message }
                ?: exception.responseBodyAsString
        secureLogger.warn { "Det skjedde en feil: $payloadFeilmelding" }
        return ResponseEntity
            .status(exception.statusCode)
            .header(HttpHeaders.WARNING, errorMessage)
            .header(HttpHeaders.CONTENT_TYPE, if (hasBody) MediaType.APPLICATION_JSON_VALUE else MediaType.TEXT_PLAIN_VALUE)
            .body(payloadFeilmelding)
    }

    private fun getErrorMessage(exception: HttpStatusCodeException): String {
        val errorMessage = StringBuilder()
        exception.responseHeaders
            ?.get(HttpHeaders.WARNING)
            ?.firstOrNull()
            ?.let { errorMessage.append(it) }
        if (exception.statusText.isNotEmpty()) {
            errorMessage.append(exception.statusText)
        }
        return errorMessage.toString()
    }

    @ResponseBody
    @ExceptionHandler(Exception::class)
    fun handleOtherExceptions(exception: Exception): ResponseEntity<*> {
        logger.warn("Det skjedde en ukjent feil", exception)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(HttpHeaders.WARNING, "Det skjedde en ukjent feil: ${exception.message}")
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler(JwtTokenUnauthorizedException::class)
    fun handleUnauthorizedException(exception: JwtTokenUnauthorizedException): ResponseEntity<*> {
        logger.warn("Ugyldig eller manglende sikkerhetstoken", exception)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.WARNING, "Ugyldig eller manglende sikkerhetstoken")
            .build<Any>()
    }
}
