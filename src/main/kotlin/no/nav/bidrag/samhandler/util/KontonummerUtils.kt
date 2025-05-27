package no.nav.bidrag.samhandler.util

import no.nav.bidrag.transport.dokument.isNumeric

object KontonummerUtils {
    fun erGyldigKontonummerMod11(kontonummer: String): Boolean {
        if (kontonummer.isEmpty() || kontonummer.length != 11 || !kontonummer.isNumeric) {
            return false
        }
        val kontrollSiffer = kontonummer[kontonummer.length - 1].toString().toInt()
        val nummerUtenKontrollSiffer = kontonummer.substring(0, kontonummer.length - 1)
        val resultat =
            nummerUtenKontrollSiffer
                .reversed()
                .mapIndexed { i, v -> v.toString().toInt() * ((i % 6) + 2) }
                .sum()
        return (if (resultat == 0) 0 else 11 - (resultat % 11)) == kontrollSiffer
    }
}
