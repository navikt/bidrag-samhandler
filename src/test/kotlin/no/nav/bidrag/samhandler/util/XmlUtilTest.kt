package no.nav.bidrag.samhandler.util

import no.nav.bidrag.samhandler.SpringTestRunner
import no.nav.bidrag.samhandler.controller.dto.SamhandlerDto
import no.nav.bidrag.samhandler.exception.MQServiceException
import no.rtv.namespacetss.SamhandlerIDataB910Type
import no.rtv.namespacetss.TServicerutiner
import no.rtv.namespacetss.TssSamhandlerData
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.converter.MarshallingMessageConverter
import org.springframework.jms.support.converter.MessageType
import org.springframework.oxm.jaxb.Jaxb2Marshaller

internal class XmlUtilTest : SpringTestRunner() {

    @Autowired
    lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var jaxb2Marshaller: Jaxb2Marshaller

    @Test
    fun getMessageConverter() {
        val samhandlerDto = SamhandlerDto("32541654987")

        val samhandlerIData = SamhandlerIDataB910Type().apply {
            idOffTSS = "32541654987"
            historikk = "N"
            brukerID = "RTV9999"
        }
        val servicerutiner = TServicerutiner().apply { samhandlerIDataB910 = samhandlerIData }
        val inputData = TssSamhandlerData.TssInputData().apply { tssServiceRutine = servicerutiner }
        val tssSamhandlerData = TssSamhandlerData().apply { tssInputData = inputData }

        val messageConverter = MarshallingMessageConverter(jaxb2Marshaller).apply { setTargetType(MessageType.TEXT) }
        jmsTemplate.messageConverter = messageConverter
        jmsTemplate.convertAndSend("DEV.QUEUE.1", tssSamhandlerData)
        val message = jmsTemplate.receiveAndConvert("DEV.QUEUE.1")
            ?: throw MQServiceException("Konsument timet ut uten å ha mottatt noen respons fra MQ")

        println("success : $message")
    }
}
