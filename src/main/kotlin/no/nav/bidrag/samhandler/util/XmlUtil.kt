package no.nav.bidrag.samhandler.util

import org.springframework.jms.support.converter.MarshallingMessageConverter
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.oxm.jaxb.Jaxb2Marshaller

object XmlUtil {
//    fun <T> createXMLString(instance: T, objectType: Class<T>): String {
//        val jaxbContext = JAXBContext.newInstance(objectType)
//        val marshaller = jaxbContext.createMarshaller()
//        val stringWriter = StringWriter()
//        marshaller.marshal(instance, stringWriter)
//        return stringWriter.toString()
//    }
//
//    fun <T> getObjectFromXMLMessage(xmlMessage: String, objectType: Class<T>): T {
//        val jaxbContext = JAXBContext.newInstance(objectType)
//        val unmarshaller = jaxbContext.createUnmarshaller()
//        val streamSource = StreamSource(StringReader(xmlMessage))
//        val jaxbElement = unmarshaller.unmarshal(streamSource, objectType)
//        return jaxbElement.value
//    }

    fun <Request, Response : Any> getMessageConverter(request: Class<Request>, response: Class<Response>): MessageConverter {
        val jaxbContext = Jaxb2Marshaller().apply { setClassesToBeBound(request, response) }
        return MarshallingMessageConverter(jaxbContext)
    }
}
