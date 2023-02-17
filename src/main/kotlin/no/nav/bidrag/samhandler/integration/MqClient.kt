package no.nav.bidrag.samhandler.integration

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.samhandler.exception.MQServiceException
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.stereotype.Service
import java.util.*

@Service
class MqClient(
    private val jmsTemplate: JmsTemplate,
    private val messageConverter: MessageConverter,
    private val objectMapper: ObjectMapper
) {

    init {
        jmsTemplate.receiveTimeout = 15000L
    }

    fun <R : Any> performRequestResponseSpring(queue: String, request: Any): R {
        val uuid = UUID.randomUUID().toString()
        return try {
            val message = jmsTemplate.sendAndReceive(queue) { session ->
                messageConverter.toMessage(request, session).apply {
                    jmsCorrelationID = uuid
                }
            } ?: throw MQServiceException("Konsument timet ut uten å ha mottatt noen respons fra MQ")
            messageConverter.fromMessage(message) as R
        } catch (e: Exception) {
            throw MQServiceException("Feil mot MQ med uuid=$uuid mot kø: $queue med request: ${objectMapper.writeValueAsString(request)}", e)
        }
    }
}
