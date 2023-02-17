package no.nav.bidrag.samhandler.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "ibm.mq")
@ConstructorBinding
class MQProperties(val tssRequestQueue: String)
