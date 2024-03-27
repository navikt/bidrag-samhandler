package no.nav.bidrag.samhandler.mapper

import no.nav.bidrag.commons.util.trimToNull
import no.nav.bidrag.domene.ident.SamhandlerId
import no.nav.bidrag.domene.land.Landkode3
import no.nav.bidrag.transport.samhandler.AdresseDto
import no.nav.bidrag.transport.samhandler.KontonummerDto
import no.nav.bidrag.transport.samhandler.SamhandlerDto
import no.nav.bidrag.transport.samhandler.SamhandlersøkeresultatDto
import no.rtv.namespacetss.Samhandler
import no.rtv.namespacetss.TssSamhandlerData
import no.rtv.namespacetss.TypeKomp940
import no.rtv.namespacetss.TypeSamhAdr
import no.rtv.namespacetss.TypeSamhAvd
import no.rtv.namespacetss.TypeSamhandler

object SamhandlerMapper {
    fun mapTilSamhandlerDto(samhandler: no.nav.bidrag.samhandler.persistence.entity.Samhandler): SamhandlerDto {
        return samhandler.let {
            SamhandlerDto(
                tssId = SamhandlerId(it.ident),
                navn = it.navn,
                offentligId = samhandler.offentligId,
                offentligIdType = samhandler.offentligIdType,
                adresse =
                    AdresseDto(
                        it.adresselinje1,
                        adresselinje2 = it.adresselinje2,
                        adresselinje3 = it.adresselinje3,
                        postnr = it.postnr,
                        poststed = it.poststed,
                        land = it.land?.let { land -> Landkode3(land) },
                    ),
                kontonummer =
                    KontonummerDto(
                        norskKontonummer = it.norskkontonr,
                        iban = it.iban,
                        swift = it.swift,
                        banknavn = it.banknavn,
                        landkodeBank = it.banklandkode?.let { banklandkode -> Landkode3(banklandkode) },
                        bankCode = it.bankcode,
                        valutakode = it.valutakode,
                    ),
            )
        }
    }

    fun mapTilSamhandlersøkeresultatDto(
        samhandlere: List<no.nav.bidrag.samhandler.persistence.entity.Samhandler>,
    ): SamhandlersøkeresultatDto {
        return SamhandlersøkeresultatDto(
            samhandlere =
                samhandlere.map {
                    mapTilSamhandlerDto(it)
                },
            flereForekomster = samhandlere.size >= 2,
        )
    }

    fun mapTilSamhandler(samhandlerDto: SamhandlerDto): no.nav.bidrag.samhandler.persistence.entity.Samhandler {
        return no.nav.bidrag.samhandler.persistence.entity.Samhandler(
            ident = samhandlerDto.tssId.verdi,
            navn = samhandlerDto.navn ?: error("Samhandler kan ikke opprettes uten navn."),
            offentligId = samhandlerDto.offentligId,
            offentligIdType = samhandlerDto.offentligIdType,
            norskkontonr = samhandlerDto.kontonummer?.norskKontonummer,
            iban = samhandlerDto.kontonummer?.iban,
            swift = samhandlerDto.kontonummer?.swift,
            banknavn = samhandlerDto.kontonummer?.banknavn,
            banklandkode = samhandlerDto.kontonummer?.landkodeBank?.verdi,
            valutakode = samhandlerDto.kontonummer?.valutakode,
            bankcode = samhandlerDto.kontonummer?.bankCode,
            adresselinje1 = samhandlerDto.adresse?.adresselinje1,
            adresselinje2 = samhandlerDto.adresse?.adresselinje2,
            adresselinje3 = samhandlerDto.adresse?.adresselinje3,
            postnr = samhandlerDto.adresse?.postnr,
            poststed = samhandlerDto.adresse?.poststed,
            land = samhandlerDto.adresse?.land?.verdi,
        )
    }

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
