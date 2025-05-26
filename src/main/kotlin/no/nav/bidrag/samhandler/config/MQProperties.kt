package no.nav.bidrag.samhandler.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@Deprecated("TSS-integrasjon skal fjernes.")
@ConfigurationProperties(prefix = "ibm.mq")
class MQProperties
    @ConstructorBinding
    constructor(
        val tssRequestQueue: String,
    )
