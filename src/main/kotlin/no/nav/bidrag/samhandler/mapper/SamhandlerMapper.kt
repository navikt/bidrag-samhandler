package no.nav.bidrag.samhandler.mapper

import no.nav.bidrag.commons.util.trimToNull
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.domene.ident.SamhandlerId
import no.nav.bidrag.domene.land.Landkode3
import no.nav.bidrag.transport.samhandler.AdresseDto
import no.nav.bidrag.transport.samhandler.KontonummerDto
import no.nav.bidrag.transport.samhandler.Områdekode
import no.nav.bidrag.transport.samhandler.SamhandlerDto
import no.nav.bidrag.transport.samhandler.SamhandlersøkeresultatDto
import no.rtv.namespacetss.Samhandler
import no.rtv.namespacetss.SamhandlerType
import no.rtv.namespacetss.TssSamhandlerData
import no.rtv.namespacetss.TypeKomp940
import no.rtv.namespacetss.TypeSamhAdr
import no.rtv.namespacetss.TypeSamhAvd
import no.rtv.namespacetss.TypeSamhandler
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger(SamhandlerMapper::class.java)

object SamhandlerMapper {
    fun mapTilSamhandlerDto(samhandler: no.nav.bidrag.samhandler.persistence.entity.Samhandler): SamhandlerDto {
        return samhandler.let {
            SamhandlerDto(
                samhandlerId = it.ident?.let { ident -> SamhandlerId(ident) },
                navn = it.navn,
                offentligId = samhandler.offentligId,
                offentligIdType = samhandler.offentligIdType,
                språk = samhandler.språk,
                områdekode = samhandler.områdekode?.let { områdekode -> Områdekode.valueOf(områdekode) },
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
                kontaktperson = it.kontaktperson,
                kontaktEpost = it.kontaktEpost,
                kontaktTelefon = it.kontaktTelefon,
                notat = it.notat,
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

    fun mapTilSamhandler(
        samhandlerDto: SamhandlerDto,
        fraTss: Boolean = false,
    ): no.nav.bidrag.samhandler.persistence.entity.Samhandler {
        return no.nav.bidrag.samhandler.persistence.entity.Samhandler(
            ident = if (fraTss) samhandlerDto.samhandlerId?.verdi else null,
            navn = samhandlerDto.navn ?: if (fraTss) "" else error("Samhandler kan ikke opprettes uten navn."),
            offentligId = samhandlerDto.offentligId,
            offentligIdType = samhandlerDto.offentligIdType,
            språk = samhandlerDto.språk,
            områdekode = samhandlerDto.områdekode?.name,
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
            kontaktperson = samhandlerDto.kontaktperson,
            kontaktEpost = samhandlerDto.kontaktEpost,
            kontaktTelefon = samhandlerDto.kontaktTelefon,
            notat = samhandlerDto.notat,
        )
    }

    fun mapTilSamhandler(
        tssSamhandlerData: TssSamhandlerData,
        ident: Ident,
    ): SamhandlerDto? {
        val samhandler = tssSamhandlerData.tssOutputData.samhandlerODataB910?.enkeltSamhandler?.firstOrNull()

        return samhandler?.let {
            val samhandlerType = gyldigSamhandler(it.samhandler110, ident)
            val avdeling = it.samhandlerAvd125.samhAvd.firstOrNull { avdeling -> avdeling.idOffTSS == ident.verdi }?.avdNr
            SamhandlerDto(
                samhandlerId = mapTilTssEksternId(it.samhandlerAvd125, avdeling),
                navn = samhandlerType?.navnSamh.trimToNull(),
                offentligId = samhandlerType?.idOff.trimToNull(),
                offentligIdType = samhandlerType?.kodeIdentType.trimToNull(),
                språk = samhandlerType?.kodeSpraak,
                områdekode = mapOmrådekode(samhandler),
                adresse = mapTilAdresse(it.adresse130, avdeling),
                kontonummer = mapToKontonummer(it, avdeling),
                kontaktperson =
                    samhandler.kontakter150?.enKontakt?.find {
                            kontakt ->
                        kontakt.kodeKontaktType == "KNTB" && avdeling == kontakt.avdNr
                    }?.kontakt,
                kontaktEpost =
                    samhandler.kontakter150?.enKontakt?.find {
                            kontakt ->
                        kontakt.kodeKontaktType == "EPOS" && avdeling == kontakt.avdNr
                    }?.kontakt,
                kontaktTelefon =
                    samhandler.kontakter150?.enKontakt?.find {
                            kontakt ->
                        kontakt.kodeKontaktType == "TLF" && avdeling == kontakt.avdNr
                    }?.kontakt,
            )
        }
    }

    private fun mapOmrådekode(samhandler: Samhandler) =
        Områdekode.entries.find { områdekode ->
            områdekode.tssOmrådekode ==
                samhandler.samhandlerFag120?.samhFag?.find { samfag ->
                    samfag.gyldigFag == "J"
                }?.kodeFagOmrade
        }

    fun mapTilSamhandlersøkeresultat(tssSamhandlerData: TssSamhandlerData): List<SamhandlerDto> {
        return tssSamhandlerData.tssOutputData.samhandlerODataB940.enkeltSamhandler
            .map {
                mapSamhandler(
                    it,
                    tssSamhandlerData.tssInputData?.tssServiceRutine?.samhandlerIDataB910?.idOffTSS?.let {
                            ident ->
                        Ident(ident)
                    },
                )
            }
    }

    private fun mapSamhandler(
        enkeltSamhandler: TypeKomp940,
        ident: Ident?,
    ): SamhandlerDto {
        val samhandlerType = gyldigSamhandler(enkeltSamhandler.samhandler110, ident)
        val avdeling =
            if (ident == null) {
                enkeltSamhandler.samhandlerAvd125.samhAvd.firstOrNull()?.avdNr
            } else {
                enkeltSamhandler.samhandlerAvd125.samhAvd.firstOrNull { avdeling -> avdeling.idOffTSS == ident.verdi }?.avdNr
            }
        return SamhandlerDto(
            samhandlerId = mapTilTssEksternId(enkeltSamhandler.samhandlerAvd125, avdeling),
            navn = samhandlerType?.navnSamh.trimToNull(),
            offentligId = samhandlerType?.idOff.trimToNull(),
            offentligIdType = samhandlerType?.kodeIdentType.trimToNull(),
            adresse = mapTilAdresse(enkeltSamhandler.adresse130, avdeling),
        )
    }

    private fun mapTilTssEksternId(
        samhandlerAvd125: TypeSamhAvd,
        avdeling: String?,
    ) = SamhandlerId(
        samhandlerAvd125.samhAvd.first {
            it.avdNr == avdeling
        }.idOffTSS,
    )

    private fun gyldigSamhandler(
        samhandler110: TypeSamhandler?,
        ident: Ident?,
    ): SamhandlerType? {
        var samhandlerType = samhandler110?.samhandler?.firstOrNull { it.kodeStatus == "GYLD" }
        if (samhandlerType == null) {
            LOGGER.warn(
                "OPPHØRT SAMHANDLER: Samhandler med ident: {} har ingen gyldig statuskode! " +
                    "Benytter en ikke gyldig samhandler for data.",
                ident?.verdi,
            )
            samhandlerType = samhandler110?.samhandler?.firstOrNull()
        }
        return samhandlerType
    }

    private fun mapTilAdresse(
        adresse130: TypeSamhAdr?,
        avdeling: String?,
    ) = adresse130?.adresseSamh?.firstOrNull { it.gyldigAdresse == "J" && it.avdNr == avdeling }?.let {
        AdresseDto(
            land = it.kodeLand?.trimToNull()?.let { s -> Landkode3(s) },
            poststed = it.poststed?.trimToNull(),
            postnr = it.postNr?.trimToNull(),
            adresselinje1 = it.adrLinjeInfo?.adresseLinje?.firstOrNull()?.trimToNull(),
            adresselinje2 = it.adrLinjeInfo?.adresseLinje?.getOrNull(1)?.trimToNull(),
            adresselinje3 = it.adrLinjeInfo?.adresseLinje?.getOrNull(2)?.trimToNull(),
        )
    }

    private fun mapToKontonummer(
        samhandler: Samhandler,
        avdeling: String?,
    ): KontonummerDto? {
        val kontoTypeInnland = samhandler.konto140?.konto?.firstOrNull { it.gironrInnland != null && avdeling == it.avdNr }
        val kontoTypeUtland = samhandler.konto140?.konto?.firstOrNull { it.gironrUtland != null && avdeling == it.avdNr }

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
