package no.nav.bidrag.samhandler.mapper

import no.nav.bidrag.commons.util.trimToNull
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
import no.nav.bidrag.domain.string.Postnummer
import no.nav.bidrag.domain.string.Poststed
import no.nav.bidrag.domain.string.Swift
import no.nav.bidrag.domain.string.Valutakode
import no.nav.bidrag.transport.samhandler.AdresseDto
import no.nav.bidrag.transport.samhandler.KontonummerDto
import no.nav.bidrag.transport.samhandler.SamhandlerDto
import no.rtv.namespacetss.Samhandler
import no.rtv.namespacetss.TssSamhandlerData
import no.rtv.namespacetss.TypeKomp940
import no.rtv.namespacetss.TypeSamhAdr
import no.rtv.namespacetss.TypeSamhAvd
import no.rtv.namespacetss.TypeSamhandler

object SamhandlerMapper {

    fun mapTilSamhandler(tssSamhandlerData: TssSamhandlerData): SamhandlerDto? {
        val samhandler =
            tssSamhandlerData.tssOutputData.samhandlerODataB910?.enkeltSamhandler?.firstOrNull()

        return samhandler?.let {
            val samhandlerType = gyldigSamhandler(it.samhandler110)
            SamhandlerDto(
                tssId = mapTilTssEksternId(it.samhandlerAvd125),
                navn = samhandlerType?.navnSamh.trimToNull()?.let { s -> FulltNavn(s) },
                offentligId = samhandlerType?.idOff.trimToNull()?.let { s -> OffentligId(s) },
                offentligIdType = samhandlerType?.kodeIdentType.trimToNull()?.let { s -> OffentligIdtype(s) },
                adresse = mapTilAdresse(it.adresse130),
                kontonummer = mapToKontonummer(it)
            )
        }
    }

    fun mapTilSamhandlersøkeresultat(tssSamhandlerData: TssSamhandlerData): List<SamhandlerDto> {
        return tssSamhandlerData.tssOutputData.samhandlerODataB940.enkeltSamhandler
            .map { mapSamhandler(it) }
    }

    private fun mapSamhandler(enkeltSamhandler: TypeKomp940): SamhandlerDto {
        val samhandlerType = gyldigSamhandler(enkeltSamhandler.samhandler110)
        return SamhandlerDto(
            tssId = mapTilTssEksternId(enkeltSamhandler.samhandlerAvd125),
            navn = samhandlerType?.navnSamh.trimToNull()?.let { FulltNavn(it) },
            offentligId = samhandlerType?.idOff.trimToNull()?.let { s -> OffentligId(s) },
            offentligIdType = samhandlerType?.kodeIdentType.trimToNull()?.let { s -> OffentligIdtype(s) },
            adresse = mapTilAdresse(enkeltSamhandler.adresse130)
        )
    }

    private fun mapTilTssEksternId(samhandlerAvd125: TypeSamhAvd) =
        SamhandlerId(samhandlerAvd125.samhAvd.first().idOffTSS)

    private fun gyldigSamhandler(samhandler110: TypeSamhandler?) =
        samhandler110?.samhandler?.firstOrNull { it.kodeStatus == "GYLD" }

    private fun mapTilAdresse(adresse130: TypeSamhAdr?) =
        adresse130?.adresseSamh?.firstOrNull()?.let {
            AdresseDto(
                land = it.kodeLand?.trimToNull()?.let { s -> Landkode3(s) },
                poststed = it.poststed?.trimToNull()?.let { s -> Poststed(s) },
                postnr = it.postNr?.trimToNull()?.let { s -> Postnummer(s) },
                adresselinje1 = it.adrLinjeInfo?.adresseLinje?.firstOrNull()?.trimToNull()?.let { s -> Adresselinje1(s) },
                adresselinje2 = it.adrLinjeInfo?.adresseLinje?.getOrNull(1)?.trimToNull()?.let { s -> Adresselinje2(s) },
                adresselinje3 = it.adrLinjeInfo?.adresseLinje?.getOrNull(2)?.trimToNull()?.let { s -> Adresselinje3(s) }
            )
        }

    private fun mapToKontonummer(samhandler: Samhandler): KontonummerDto? {
        val kontoTypeInnland = samhandler.konto140?.konto?.firstOrNull { it.gironrInnland != null }
        val kontoTypeUtland = samhandler.konto140?.konto?.firstOrNull { it.gironrUtland != null }

        return (kontoTypeInnland ?: kontoTypeUtland)?.let {
            KontonummerDto(
                landkodeBank = it.kodeLand?.trimToNull()?.let { s -> Landkode3(s) },
                banknavn = it.bankNavn?.trimToNull()?.let { s -> Banknavn(s) },
                norskKontonummer = it.gironrInnland?.trimToNull()?.let { s -> NorskKontonummer(s) },
                swift = it.swiftKode?.trimToNull()?.let { s -> Swift(s) },
                valutakode = it.kodeValuta?.trimToNull()?.let { s -> Valutakode(s) },
                bankCode = it.bankKode?.trimToNull()?.let { s -> Bankkode(s) },
                iban = it.gironrUtland?.trimToNull()?.let { s -> Iban(s) }
            )
        }
    }
}
