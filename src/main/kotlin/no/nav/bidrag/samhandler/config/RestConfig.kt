package no.nav.bidrag.samhandler.config

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.transport.felles.objectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RestConfig {

    @Bean
    fun jacksonObjectMapper(): ObjectMapper {
        return objectMapper
    }
}
