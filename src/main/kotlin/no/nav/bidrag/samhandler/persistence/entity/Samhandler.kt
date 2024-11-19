package no.nav.bidrag.samhandler.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Version
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.DynamicInsert
import java.time.LocalDateTime

@Entity(name = "samhandlere")
@DynamicInsert
data class Samhandler(
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @Column(name = "ident")
    @ColumnDefault("nextval('ident_seq')")
    val ident: String? = null,
    @Column(name = "navn")
    val navn: String = "",
    @Column(name = "offentlig_id")
    val offentligId: String? = null,
    @Column(name = "offentlig_id_type")
    val offentligIdType: String? = null,
    @Column(name = "sprak")
    val språk: String? = null,
    @Column(name = "omradekode")
    val områdekode: String? = null,
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
    @Column(name = "kontaktperson")
    val kontaktperson: String? = null,
    @Column(name = "kontakt_epost")
    val kontaktEpost: String? = null,
    @Column(name = "kontakt_telefon")
    val kontaktTelefon: String? = null,
    @Column(name = "notat")
    val notat: String? = null,
    @Column(name = "er_opphort")
    val erOpphørt: Boolean = false,
    @Version
    @Column(name = "endret_tidspunkt")
    var endretTidspunkt: LocalDateTime? = null,
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "tabellId", referencedColumnName = "id")
    var auditLog: MutableList<AuditLog> = mutableListOf(),
)
