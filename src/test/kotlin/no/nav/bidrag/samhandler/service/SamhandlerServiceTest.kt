package no.nav.bidrag.samhandler.service

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import jakarta.persistence.EntityManager
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.samhandler.mapper.SamhandlerMapper
import no.nav.bidrag.samhandler.persistence.entity.Samhandler
import no.nav.bidrag.samhandler.persistence.repository.SamhandlerRepository
import no.nav.bidrag.transport.samhandler.SamhandlerDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.ResponseEntity
import java.util.Optional

@ExtendWith(MockKExtension::class)
class SamhandlerServiceTest {
    @RelaxedMockK
    private lateinit var tssService: TssService

    @RelaxedMockK
    private lateinit var samhandlerRepository: SamhandlerRepository

    @RelaxedMockK
    private lateinit var kafkaService: KafkaService

    @RelaxedMockK
    private lateinit var entityManager: EntityManager

    @InjectMockKs
    private lateinit var samhandlerService: SamhandlerService

    private lateinit var ident: Ident
    private lateinit var samhandler: Samhandler
    private lateinit var samhandlerDto: SamhandlerDto

    @BeforeEach
    fun setUp() {
        ident = Ident("12345678901")
        samhandler = Samhandler(id = 0, ident = "12345678901")
        samhandlerDto = SamhandlerMapper.mapTilSamhandlerDto(samhandler)
    }

    @Test
    fun `hentSamhandler skal returnere SamhandlerDto når samhandler finnes`() {
        every { samhandlerRepository.findByIdent(ident.verdi) } returns samhandler
        val result = samhandlerService.hentSamhandler(ident, true)
        result shouldNotBe null
        verify { samhandlerRepository.findByIdent(ident.verdi) }
    }

    @Test
    fun `hentSamhandler skal returnere null når samhandler ikke finnes`() {
        every { samhandlerRepository.findByIdent(ident.verdi) } returns null
        every { tssService.hentSamhandler(ident) } returns null

        val result = samhandlerService.hentSamhandler(ident, true)

        result shouldBe null
        verify { samhandlerRepository.findByIdent(ident.verdi) }
        verify { tssService.hentSamhandler(ident) }
    }

    @Test
    fun `opprettSamhandler skal lagre og returnere id`() {
        every { samhandlerRepository.save(any()) } returns samhandler
        val result = samhandlerService.opprettSamhandler(samhandlerDto)
        result shouldBe samhandler.id
        verify { samhandlerRepository.save(any()) }
    }

    @Test
    fun `hentSamhandlerPåId skal returnere SamhandlerDto når samhandler finnes`() {
        every { samhandlerRepository.findById(samhandler.id!!) } returns Optional.of(samhandler)
        val result = samhandlerService.hentSamhandlerPåId(samhandler.id!!)
        result shouldNotBe null
        verify { samhandlerRepository.findById(samhandler.id!!) }
    }

    @Test
    fun `hentSamhandlerPåId skal returnere null når samhandler ikke finnes`() {
        every { samhandlerRepository.findById(samhandler.id!!) } returns Optional.empty()
        val result = samhandlerService.hentSamhandlerPåId(samhandler.id!!)
        result shouldBe null
        verify { samhandlerRepository.findById(samhandler.id!!) }
    }

    @Test
    fun `oppdaterSamhandler skal returnere ResponseEntity med bad request når samhandlerId er null`() {
        val invalidSamhandlerDto = samhandlerDto.copy(samhandlerId = null)
        val result = samhandlerService.oppdaterSamhandler(invalidSamhandlerDto)
        result shouldBe ResponseEntity.badRequest().body("Oppdatering av samhandler må ha angitt samhandlerId!")
    }

    @Test
    fun `oppdaterSamhandler skal returnere ResponseEntity med not found når samhandler ikke finnes`() {
        every { samhandlerRepository.findByIdent(samhandlerDto.samhandlerId!!.verdi) } returns null
        val result = samhandlerService.oppdaterSamhandler(samhandlerDto)
        result shouldBe ResponseEntity.notFound().build<Any>()
    }

    @Test
    fun `oppdaterSamhandler skal returnere ResponseEntity med bad request when navn ikke er satt`() {
        val invalidSamhandlerDto = samhandlerDto.copy(navn = null)
        every { samhandlerRepository.save(any()) } returns samhandler
        val result = samhandlerService.oppdaterSamhandler(invalidSamhandlerDto)
        result shouldBe ResponseEntity.badRequest().body("Navn kan ikke være tomt! Mangler navn fra TSS må dette opprettes.")
    }

    @Test
    fun `oppdaterSamhandler skal oppdatere og returnere ResponseEntity med SamhandlerDto`() {
        every { samhandlerRepository.save(any()) } returns samhandler
        every { samhandlerRepository.findByIdent(samhandlerDto.samhandlerId!!.verdi) } returns samhandler
        val result = samhandlerService.oppdaterSamhandler(samhandlerDto)
        result shouldBe ResponseEntity.ok(SamhandlerMapper.mapTilSamhandlerDto(samhandler))
    }
}
