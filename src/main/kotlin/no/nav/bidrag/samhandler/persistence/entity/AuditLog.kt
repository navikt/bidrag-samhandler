package no.nav.bidrag.samhandler.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity(name = "audit_log")
data class AuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val tabellNavn: String = "",
    val tabellId: Int = 0,
    val operasjon: String = "",
    val endretTidspunkt: LocalDateTime = LocalDateTime.now(),
    val endretAv: String = "",
    val gamleVerdier: String = "",
    val nyeVerdier: String = "",
)
