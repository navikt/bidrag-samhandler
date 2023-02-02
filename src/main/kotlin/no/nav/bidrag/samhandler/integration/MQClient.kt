package no.nav.bidrag.samhandler.integration

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.samhandler.exception.MQServiceException
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.stereotype.Service
import javax.jms.JMSException
import javax.xml.bind.JAXBException

@Service
class MQClient(
    private val jmsTemplate: JmsTemplate,
    private val messageConverter: MessageConverter,
    private val objectMapper: ObjectMapper
) {

    init {
        jmsTemplate.receiveTimeout = 15000L
    }

    fun <R : Any> performRequestResponseSpring(queue: String, request: Any): R {
        return try {
            val message = jmsTemplate.sendAndReceive(queue) { session -> messageConverter.toMessage(request, session) }
                ?: throw MQServiceException("Konsument timet ut uten å ha mottatt noen respons fra MQ")
            messageConverter.fromMessage(message) as R
        } catch (e: JMSException) {
            throw MQServiceException("MQ Request-Response feilet mot kø: $queue med request: ${objectMapper.writeValueAsString(request)}", e)
        } catch (e: JAXBException) {
            throw MQServiceException("MQ Request-Response feilet mot kø: $queue med request: ${objectMapper.writeValueAsString(request)}", e)
        } catch (e: MQServiceException) {
            throw MQServiceException("MQ Request-Response feilet mot kø: $queue med request: ${objectMapper.writeValueAsString(request)}", e)
        }
    }
}
