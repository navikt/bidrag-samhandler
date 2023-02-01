package no.nav.bidrag.samhandler.service

import no.nav.bidrag.samhandler.consumer.BidragPersonConsumer
import no.nav.bidrag.samhandler.model.HentPersonResponse
import no.nav.domain.ident.PersonIdent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ExampleService(val bidragPersonConsumer: BidragPersonConsumer) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun hentDialogerForPerson(personIdent: PersonIdent): HentPersonResponse {
        logger.info("Henter samtalereferat for person")
        return bidragPersonConsumer.hentPerson(personIdent)
    }
}
