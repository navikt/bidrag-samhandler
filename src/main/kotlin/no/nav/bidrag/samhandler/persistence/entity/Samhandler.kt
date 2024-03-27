package no.nav.bidrag.samhandler.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Version
import java.time.LocalDateTime

@Entity(name = "samhandlere")
data class Samhandler(
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @Column(name = "ident")
    val ident: String = "",
    @Column(name = "navn")
    val navn: String = "",
    @Column(name = "offentlig_id")
    val offentligId: String? = null,
    @Column(name = "offentlig_id_type")
    val offentligIdType: String? = null,
    @Column(name = "norskkontonr")
    val norskkontonr: String? = null,
    @Column(name = "iban")
    val iban: String? = null,
    @Column(name = "swift")
    val swift: String? = null,
    @Column(name = "banknavn")
    val banknavn: String? = null,
    @Column(name = "banklandkode")
    val banklandkode: String? = null,
    @Column(name = "valutakode")
    val valutakode: String? = null,
    @Column(name = "bankcode")
    val bankcode: String? = null,
    @Column(name = "adresselinje1")
    val adresselinje1: String? = null,
    @Column(name = "adresselinje2")
    val adresselinje2: String? = null,
    @Column(name = "adresselinje3")
    val adresselinje3: String? = null,
    @Column(name = "postnr")
    val postnr: String? = null,
    @Column(name = "poststed")
    val poststed: String? = null,
    @Column(name = "land")
    val land: String? = null,
    @Version
    @Column(name = "endret_tidspunkt")
    var endretTidspunkt: LocalDateTime? = null,
)
