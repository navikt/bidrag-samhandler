package no.nav.bidrag.samhandler

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [BidragSamhandlerLocal::class])
@SpringBootTest(classes = [BidragSamhandlerLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@EnableMockOAuth2Server
class SpringTestRunner {

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @AfterEach
    fun reset() {
        resetWiremockServers()
    }

    private fun resetWiremockServers() {
        applicationContext.getBeansOfType(WireMockServer::class.java)
            .values
            .forEach(WireMockServer::resetRequests)
    }

    fun rootUri(): String {
        return LOCALHOST + port
    }

    protected fun getPort(): String {
        return port.toString()
    }

    companion object {

        private const val LOCALHOST = "http://localhost:"
    }
}
