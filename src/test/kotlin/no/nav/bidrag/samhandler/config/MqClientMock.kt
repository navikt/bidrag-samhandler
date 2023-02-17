package no.nav.bidrag.samhandler.config

import io.mockk.mockk
import no.nav.bidrag.samhandler.integration.MqClient
import no.nav.bidrag.samhandler.service.TssService
import no.rtv.namespacetss.AdrLinjeType
import no.rtv.namespacetss.AdresseSamhType
import no.rtv.namespacetss.KontoType
import no.rtv.namespacetss.SamhAvdPraType
import no.rtv.namespacetss.Samhandler
import no.rtv.namespacetss.SamhandlerType
import no.rtv.namespacetss.SvarStatusType
import no.rtv.namespacetss.TOutputElementer
import no.rtv.namespacetss.TssSamhandlerData
import no.rtv.namespacetss.TypeKomp940
import no.rtv.namespacetss.TypeOD910
import no.rtv.namespacetss.TypeSamhAdr
import no.rtv.namespacetss.TypeSamhAvd
import no.rtv.namespacetss.TypeSamhKonto
import no.rtv.namespacetss.TypeSamhandler
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class MqClientMock : MqClient(mockk(relaxed = true), mockk(), mockk()) {

    override fun <R : Any> performRequestResponseSpring(queue: String, request: Any): R {
        val samhandler110 = TypeSamhandler().apply {
            samhandler.add(
                SamhandlerType().apply {
                    idOff = "idOff"
                    kodeIdentType = "kodeIdentType"
                    navnSamh = "navnSamh"
                    kodeStatus = "GYLD"
                }
            )
        }
        val samhandlerAvd125 = TypeSamhAvd().apply {
            samhAvd.add(
                SamhAvdPraType().apply {
                    avdNr = "00"
                    idOffTSS = "idOffTSS"
                }
            )
        }
        val adresse130 = TypeSamhAdr().apply {
            adresseSamh.add(
                AdresseSamhType().apply {
                    kodeLand = "kodeLand"
                    poststed = "poststed"
                    postNr = "postNr"
                    adrLinjeInfo = AdrLinjeType().apply {
                        adresseLinje.add("adresseLinje1")
                        adresseLinje.add("adresseLinje2")
                        adresseLinje.add("adresseLinje3")
                    }
                }
            )
        }
        val samhandler = Samhandler().apply {
            this.samhandler110 = samhandler110
            this.samhandlerAvd125 = samhandlerAvd125
            this.adresse130 = adresse130
            konto140 = TypeSamhKonto().apply {
                konto.add(
                    KontoType().apply {
                        kodeLand = "kodeLand"
                        bankNavn = "bankNavn"
                        gironrInnland = "gironrInnland"
                        swiftKode = "swiftKode"
                        kodeValuta = "kodeValuta"
                        bankKode = "bankKode"
                        gironrUtland = "gironrUtland"
                    }
                )
            }
        }
        return TssSamhandlerData().apply {
            tssOutputData = TOutputElementer().apply {
                svarStatus = SvarStatusType().apply {
                    alvorligGrad = TssService.TSS_STATUS_OK
                }
                samhandlerODataB910 = TypeOD910().apply {
                    enkeltSamhandler.add(samhandler)
                }
                samhandlerODataB940 = TOutputElementer.SamhandlerODataB940().apply {
                    enkeltSamhandler.add(
                        TypeKomp940().apply {
                            this.samhandler110 = samhandler110
                            this.samhandlerAvd125 = samhandlerAvd125
                            this.adresse130 = adresse130
                        }
                    )
                }
            }
        } as R
    }
}
