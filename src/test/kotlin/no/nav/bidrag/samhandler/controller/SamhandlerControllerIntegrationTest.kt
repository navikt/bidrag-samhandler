package no.nav.bidrag.samhandler.controller

import io.kotest.matchers.shouldBe
import no.nav.bidrag.domain.bool.FlereForekomster
import no.nav.bidrag.domain.ident.Ident
import no.nav.bidrag.domain.ident.OffentligId
import no.nav.bidrag.domain.ident.SamhandlerId
import no.nav.bidrag.domain.string.Adresselinje1
import no.nav.bidrag.domain.string.Adresselinje2
import no.nav.bidrag.domain.string.Adresselinje3
import no.nav.bidrag.domain.string.Bankkode
import no.nav.bidrag.domain.string.Banknavn
import no.nav.bidrag.domain.string.FulltNavn
import no.nav.bidrag.domain.string.Iban
import no.nav.bidrag.domain.string.Landkode3
import no.nav.bidrag.domain.string.NorskKontonummer
import no.nav.bidrag.domain.string.OffentligIdtype
import no.nav.bidrag.domain.string.Område
import no.nav.bidrag.domain.string.Postnummer
import no.nav.bidrag.domain.string.Poststed
import no.nav.bidrag.domain.string.Swift
import no.nav.bidrag.domain.string.Valutakode
import no.nav.bidrag.samhandler.SpringTestRunner
import no.nav.bidrag.transport.samhandler.AdresseDto
import no.nav.bidrag.transport.samhandler.KontonummerDto
import no.nav.bidrag.transport.samhandler.SamhandlerDto
import no.nav.bidrag.transport.samhandler.SamhandlersøkeresultatDto
import no.nav.bidrag.transport.samhandler.SøkSamhandlerQuery
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus

class SamhandlerControllerIntegrationTest : SpringTestRunner() {

    fun urlForPost() = rootUriComponentsBuilder().pathSegment("samhandler").build().toUri()

    fun urlForGet() = rootUriComponentsBuilder().pathSegment("samhandler")
        .queryParams(SøkSamhandlerQuery(FulltNavn("navn"), Postnummer("postnummer"), Område("område")).toQueryParams())
        .build().toUri()

    @Test
    fun `post for Ident retunerer korrekt bygd SamhandlerDto`() {
        val forventetResultat = SamhandlerDto(
            tssId = SamhandlerId("idOffTSS"),
            navn = FulltNavn("navnSamh"),
            offentligId = OffentligId("idOff"),
            offentligIdType = OffentligIdtype("kodeIdentType"),
            adresse = AdresseDto(
                adresselinje1 = Adresselinje1("adresseLinje1"),
                adresselinje2 = Adresselinje2("adresseLinje2"),
                adresselinje3 = Adresselinje3("adresseLinje3"),
                postnr = Postnummer("postNr"),
                poststed = Poststed("poststed"),
                land = Landkode3("kodeLand")
            ),
            kontonummer = KontonummerDto(
                norskKontonummer = NorskKontonummer("gironrInnland"),
                iban = Iban("gironrUtland"),
                swift = Swift("swiftKode"),
                banknavn = Banknavn("bankNavn"),
                landkodeBank = Landkode3("kodeLand"),
                bankCode = Bankkode("bankKode"),
                valutakode = Valutakode("kodeValuta")
            )
        )

        val responseEntity =
            httpHeaderTestRestTemplate.postForEntity<SamhandlerDto>(urlForPost(), Ident("80000000003"))

        responseEntity.statusCode shouldBe HttpStatus.OK
        responseEntity.body shouldBe forventetResultat
    }

    @Test
    fun `get for søk retunerer korrekt bygd SamhandlersøkeresultatDto`() {
        val forventetSøkeresultat = SamhandlersøkeresultatDto(
            listOf(
                SamhandlerDto(
                    tssId = SamhandlerId("idOffTSS"),
                    navn = FulltNavn("navnSamh"),
                    offentligId = OffentligId("idOff"),
                    offentligIdType = OffentligIdtype("kodeIdentType"),
                    adresse = AdresseDto(
                        adresselinje1 = Adresselinje1("adresseLinje1"),
                        adresselinje2 = Adresselinje2("adresseLinje2"),
                        adresselinje3 = Adresselinje3("adresseLinje3"),
                        postnr = Postnummer("postNr"),
                        poststed = Poststed("poststed"),
                        land = Landkode3("kodeLand")
                    ),
                    kontonummer = null
                )
            ),
            flereForekomster = FlereForekomster(false)
        )
        val responseEntity =
            httpHeaderTestRestTemplate.getForEntity<SamhandlersøkeresultatDto>(urlForGet().toString(), null)

        responseEntity.statusCode shouldBe HttpStatus.OK
        responseEntity.body shouldBe forventetSøkeresultat
    }
}
