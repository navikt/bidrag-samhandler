package no.nav.bidrag.samhandler.service

import no.nav.bidrag.samhandler.SpringTestRunner
import no.nav.domain.ident.SamhandlerId
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class TssServiceImplTest : SpringTestRunner() {

    @Autowired
    private lateinit var tssServiceImpl: TssServiceImpl

    @Test
    fun hentSamhandler() {

            tssServiceImpl.hentSamhandler(SamhandlerId("98765432112"))


    }

    @Test
    fun testHentSamhandler() {
    }
}