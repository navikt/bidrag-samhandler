package no.nav.bidrag.samhandler.persistence.repository

import no.nav.bidrag.samhandler.persistence.entity.Samhandler
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface SamhandlerRepository : JpaRepository<Samhandler, Int>, JpaSpecificationExecutor<Samhandler> {
    fun findByIdent(ident: String): Samhandler?

    fun findAllByNavnIgnoreCaseAndPostnr(
        navn: String,
        postnr: String?,
    ): List<Samhandler>

    fun findAllByNavnIgnoreCase(navn: String): List<Samhandler>
}
