package no.nav.bidrag.samhandler.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.nav.bidrag.commons.web.DefaultCorsFilter
import no.nav.bidrag.commons.web.MdcFilter
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import org.springframework.jms.annotation.EnableJms

@EnableAspectJAutoProxy
@OpenAPIDefinition(info = Info(title = "bidrag-samhandler", version = "v1"), security = [SecurityRequirement(name = "bearer-key")])
@SecurityScheme(bearerFormat = "JWT", name = "bearer-key", scheme = "bearer", type = SecuritySchemeType.HTTP)
@Configuration
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
@Import(DefaultCorsFilter::class, MdcFilter::class)
@EnableConfigurationProperties(MQProperties::class)
@EnableJms
class BidragSamhandlerConfiguration
