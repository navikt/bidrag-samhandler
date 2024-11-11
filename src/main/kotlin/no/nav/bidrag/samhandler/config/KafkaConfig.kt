package no.nav.bidrag.samhandler.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig(
    @Value("\${TOPIC_SAMHANDLER}") val topicSamhandler: String,
)
