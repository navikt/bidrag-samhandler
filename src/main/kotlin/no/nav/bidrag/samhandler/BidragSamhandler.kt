package no.nav.bidrag.samhandler

import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class, ManagementWebSecurityAutoConfiguration::class])
class BidragSamhandlerSpring

fun main(args: Array<String>) {
    SpringApplication.run(BidragSamhandlerSpring::class.java, *args)
}
