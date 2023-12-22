package no.nav.bidrag.samhandler.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.support.converter.MarshallingMessageConverter
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.jms.support.converter.MessageType
import org.springframework.oxm.jaxb.Jaxb2Marshaller

@Configuration
@EnableJms
class JMSConfiguration {
    @Bean
    fun jaxb2Marshaller() = Jaxb2Marshaller().apply { setPackagesToScan("no.rtv") }

    @Bean
    fun marshallingMessageConverter(jaxb2Marshaller: Jaxb2Marshaller): MessageConverter =
        MarshallingMessageConverter(jaxb2Marshaller).apply { setTargetType(MessageType.TEXT) }
}
