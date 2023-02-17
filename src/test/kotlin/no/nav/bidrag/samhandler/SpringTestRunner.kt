package no.nav.bidrag.samhandler

import com.github.tomakehurst.wiremock.WireMockServer
import com.nimbusds.jwt.SignedJWT
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.util.UriComponentsBuilder

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

    @Autowired
    protected lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

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

    fun rootUriComponentsBuilder(): UriComponentsBuilder {
        return UriComponentsBuilder.fromHttpUrl(rootUri())
    }

    protected fun getPort(): String {
        return port.toString()
    }

    val httpHeaderTestRestTemplate
        get() = HttpHeaderTestRestTemplate(testRestTemplate).apply {
            add(HttpHeaders.AUTHORIZATION) { generateTestToken() }
        }

    private fun generateTestToken(): String {
        val token: SignedJWT = mockOAuth2Server.issueToken("aad", "aud-localhost", "aud-localhost")
        return "Bearer " + token.serialize()
    }

    companion object {

        private const val LOCALHOST = "http://localhost:"
    }
}
