package no.nav.bidrag.samhandler.util

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KontonummerUtilsTest {
    @Test
    fun `skal returnere true for gyldige kontonumre`() {
        val gyldigeKontonumre =
            listOf(
                "15768474386", // Fiktivt, men gyldig mod11
                "20027240179", // Fiktivt, men gyldig mod11
                "38718549237", // Fiktivt, men gyldig mod11
            )
        gyldigeKontonumre.forEach {
            assertTrue(KontonummerUtils.erGyldigKontonummerMod11(it), "Forventet gyldig: $it")
        }
    }

    @Test
    fun `skal returnere false for ugyldige kontonumre`() {
        val ugyldigeKontonumre =
            listOf(
                "51736524478", // Endret kontrollsiffer
                "48949816700", // Endret kontrollsiffer
                "abcdefghijk", // Ikke tall
                "123", // For kort
                "", // Tom streng
            )
        ugyldigeKontonumre.forEach {
            assertFalse(KontonummerUtils.erGyldigKontonummerMod11(it), "Forventet ugyldig: $it")
        }
    }
}
