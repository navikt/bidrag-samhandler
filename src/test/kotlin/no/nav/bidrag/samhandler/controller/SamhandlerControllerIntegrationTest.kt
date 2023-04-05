package no.nav.bidrag.samhandler.controller

import io.kotest.matchers.shouldBe
import no.nav.bidrag.domain.ident.Ident
import no.nav.bidrag.samhandler.SpringTestRunner
import no.nav.bidrag.transport.samhandler.AdresseDto
import no.nav.bidrag.transport.samhandler.KontonummerDto
import no.nav.bidrag.transport.samhandler.SamhandlerDto
import no.nav.bidrag.transport.samhandler.SamhandlersøkeresultatDto
import no.nav.bidrag.transport.samhandler.SøkSamhandlerQuery
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class SamhandlerControllerIntegrationTest : SpringTestRunner() {

    fun urlForPost() = rootUriComponentsBuilder().pathSegment("samhandler").build().toUri()

    fun urlForGet() = rootUriComponentsBuilder().pathSegment("samhandler")
        .queryParams(SøkSamhandlerQuery("navn", "postnummer", "område").toQueryParams())
        .build().toUri()

    @Test
    fun `post for Ident retunerer korrekt bygd SamhandlerDto`() {
        val forventetResultat = SamhandlerDto(
            tssId = "idOffTSS",
            navn = "navnSamh",
            offentligId = "idOff",
            offentligIdType = "kodeIdentType",
            adresse = AdresseDto(
                adresselinje1 = "adresseLinje1",
                adresselinje2 = "adresseLinje2",
                adresselinje3 = "adresseLinje3",
                postnr = "postNr",
                poststed = "poststed",
                land = "kodeLand"
            ),
            kontonummer = KontonummerDto(
                norskKontonummer = "gironrInnland",
                iban = "gironrUtland",
                swift = "swiftKode",
                banknavn = "bankNavn",
                landkodeBank = "kodeLand",
                bankCode = "bankKode",
                valutakode = "kodeValuta"
            )
        )

        val responseEntity = httpHeaderTestRestTemplate.postForEntity<SamhandlerDto>(urlForPost(), Ident("80000000003"))

        responseEntity.statusCode shouldBe HttpStatus.OK
        responseEntity.body shouldBe forventetResultat
    }

    @Test
    fun `get for søk retunerer korrekt bygd SamhandlersøkeresultatDto`() {
        val forventetSøkeresultat = SamhandlersøkeresultatDto(
            listOf(
                SamhandlerDto(
                    tssId = "idOffTSS",
                    navn = "navnSamh",
                    offentligId = "idOff",
                    offentligIdType = "kodeIdentType",
                    adresse = AdresseDto(
                        adresselinje1 = "adresseLinje1",
                        adresselinje2 = "adresseLinje2",
                        adresselinje3 = "adresseLinje3",
                        postnr = "postNr",
                        poststed = "poststed",
                        land = "kodeLand"
                    ),
                    kontonummer = null
                )
            ),
            flereForekomster = false
        )

        val responseEntity =
            httpHeaderTestRestTemplate.getForEntity<SamhandlersøkeresultatDto>(urlForGet(), null)

        responseEntity.statusCode shouldBe HttpStatus.OK
        responseEntity.body shouldBe forventetSøkeresultat
    }
}
