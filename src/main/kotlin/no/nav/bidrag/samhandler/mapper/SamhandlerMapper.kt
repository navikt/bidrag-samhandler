package no.nav.bidrag.samhandler.mapper

import no.nav.bidrag.samhandler.controller.dto.AdresseDto
import no.nav.bidrag.samhandler.controller.dto.KontonummerDto
import no.nav.bidrag.samhandler.controller.dto.SamhandlerDto
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
                samhandlernavn = samhandlerType?.navnSamh,
                offentligId = samhandlerType?.idOff,
                offentligIdType = samhandlerType?.kodeIdentType,
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
            samhandlernavn = samhandlerType?.navnSamh,
            offentligId = samhandlerType?.idOff,
            offentligIdType = samhandlerType?.kodeIdentType,
            adresse = mapTilAdresse(enkeltSamhandler.adresse130)
        )
    }

    private fun mapTilTssEksternId(samhandlerAvd125: TypeSamhAvd) =
        samhandlerAvd125.samhAvd.first().idOffTSS

    private fun gyldigSamhandler(samhandler110: TypeSamhandler?) =
        samhandler110?.samhandler?.firstOrNull { it.kodeStatus == "GYLD" }

    private fun mapTilAdresse(adresse130: TypeSamhAdr?) =
        adresse130?.adresseSamh?.firstOrNull()?.let {
            AdresseDto(
                land = it.kodeLand.trim(),
                poststed = it.poststed.trim(),
                postnr = it.postNr.trim(),
                adresselinje1 = it.adrLinjeInfo?.adresseLinje?.firstOrNull()?.trim(),
                adresselinje2 = it.adrLinjeInfo?.adresseLinje?.getOrNull(1)?.trim(),
                adresselinje3 = it.adrLinjeInfo?.adresseLinje?.getOrNull(2)?.trim()
            )
        }

    private fun mapToKontonummer(samhandler: Samhandler): KontonummerDto? {
        val kontoTypeInnland = samhandler.konto140?.konto?.firstOrNull { it.gironrInnland != null }
        val kontoTypeUtland = samhandler.konto140?.konto?.firstOrNull { it.gironrUtland != null }

        return (kontoTypeInnland ?: kontoTypeUtland)?.let {
            KontonummerDto(
                landkodeBank = it.kodeLand,
                banknavn = it.bankNavn,
                norskKontonummer = it.gironrInnland,
                swift = it.swiftKode,
                valutakode = it.kodeValuta,
                bankCode = it.bankKode,
                iban = it.gironrUtland
            )
        }
    }
}
