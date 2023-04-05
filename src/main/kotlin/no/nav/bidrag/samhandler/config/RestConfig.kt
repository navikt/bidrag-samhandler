package no.nav.bidrag.samhandler.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.YearMonthDeserializer
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Configuration
class RestConfig {

    @Bean
    fun jacksonObjectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerKotlinModule()
            .registerModule(
                JavaTimeModule()
                    .addDeserializer(
                        YearMonth::class.java,
                        YearMonthDeserializer(DateTimeFormatter.ofPattern("u-MM"))
                    )
            )
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}
