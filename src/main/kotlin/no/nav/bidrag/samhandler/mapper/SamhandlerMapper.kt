package no.nav.bidrag.samhandler.mapper

import no.nav.bidrag.commons.util.trimToNull
import no.nav.bidrag.domene.ident.SamhandlerId
import no.nav.bidrag.domene.land.Landkode3
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
                samhandlerId = mapTilTssEksternId(it.samhandlerAvd125),
                navn = samhandlerType?.navnSamh.trimToNull(),
                offentligId = samhandlerType?.idOff.trimToNull(),
                offentligIdType = samhandlerType?.kodeIdentType.trimToNull(),
                adresse = mapTilAdresse(it.adresse130),
                kontonummer = mapToKontonummer(it),
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
            samhandlerId = mapTilTssEksternId(enkeltSamhandler.samhandlerAvd125),
            navn = samhandlerType?.navnSamh.trimToNull(),
            offentligId = samhandlerType?.idOff.trimToNull(),
            offentligIdType = samhandlerType?.kodeIdentType.trimToNull(),
            adresse = mapTilAdresse(enkeltSamhandler.adresse130),
        )
    }

    private fun mapTilTssEksternId(samhandlerAvd125: TypeSamhAvd) = SamhandlerId(samhandlerAvd125.samhAvd.first().idOffTSS)

    private fun gyldigSamhandler(samhandler110: TypeSamhandler?) = samhandler110?.samhandler?.firstOrNull { it.kodeStatus == "GYLD" }

    private fun mapTilAdresse(adresse130: TypeSamhAdr?) =
        adresse130?.adresseSamh?.firstOrNull()?.let {
            AdresseDto(
                land = it.kodeLand?.trimToNull()?.let { s -> Landkode3(s) },
                poststed = it.poststed?.trimToNull(),
                postnr = it.postNr?.trimToNull(),
                adresselinje1 = it.adrLinjeInfo?.adresseLinje?.firstOrNull()?.trimToNull(),
                adresselinje2 = it.adrLinjeInfo?.adresseLinje?.getOrNull(1)?.trimToNull(),
                adresselinje3 = it.adrLinjeInfo?.adresseLinje?.getOrNull(2)?.trimToNull(),
            )
        }

    private fun mapToKontonummer(samhandler: Samhandler): KontonummerDto? {
        val kontoTypeInnland = samhandler.konto140?.konto?.firstOrNull { it.gironrInnland != null }
        val kontoTypeUtland = samhandler.konto140?.konto?.firstOrNull { it.gironrUtland != null }

        return (kontoTypeInnland ?: kontoTypeUtland)?.let {
            KontonummerDto(
                landkodeBank = it.kodeLand?.trimToNull()?.let { s -> Landkode3(s) },
                banknavn = it.bankNavn?.trimToNull(),
                norskKontonummer = it.gironrInnland?.trimToNull(),
                swift = it.swiftKode?.trimToNull(),
                valutakode = it.kodeValuta?.trimToNull(),
                bankCode = it.bankKode?.trimToNull(),
                iban = it.gironrUtland?.trimToNull(),
            )
        }
    }
}
