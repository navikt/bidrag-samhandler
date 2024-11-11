package no.nav.bidrag.samhandler.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.samhandler.config.KafkaConfig
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class SamhandlerProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val kafkaConfig: KafkaConfig,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Retryable(
        value = [Exception::class],
        maxAttempts = 10,
        backoff = Backoff(delay = 1000, maxDelay = 12000, multiplier = 2.0),
    )
    fun sendSamhandlerMelding(samhandlerMelding: SamhandlerMelding) {
        sendKafkamelding(kafkaConfig.topicSamhandler, samhandlerMelding)
    }

    private fun sendKafkamelding(
        topic: String,
        samhandlerMelding: SamhandlerMelding,
    ) {
        val melding = objectMapper.writeValueAsString(samhandlerMelding)
        kafkaTemplate
            .send(topic, samhandlerMelding.samhandlerId.toString(), melding)
            .thenAccept {
                logger.info(
                    "Melding på topic $topic for samhandlerId ${samhandlerMelding.samhandlerId} er sendt. Fikk offset ${it?.recordMetadata?.offset()}",
                )
            }.exceptionally {
                val feilmelding =
                    "Melding på topic $topic kan ikke sendes for samhandlerId ${samhandlerMelding.samhandlerId}. Feiler med ${it.message}"
                logger.warn(feilmelding)
                error(feilmelding)
            }
    }
}

data class SamhandlerMelding(
    val samhandlerId: String,
    val hendelsestype: SamhandlerKafkaHendelsestype,
    val sporingId: String,
)

enum class SamhandlerKafkaHendelsestype {
    OPPRETTET,
    OPPDATERT,
    OPPHØRT,
}
