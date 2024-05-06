package no.nav.bidrag.samhandler.persistence.repository

import no.nav.bidrag.samhandler.persistence.entity.Samhandler
import no.nav.bidrag.transport.samhandler.SamhandlerSøk
import org.springframework.data.jpa.domain.Specification

object SamhandlerSøkSpec {
    fun søkPåAlleParameter(samhandlerSøk: SamhandlerSøk): Specification<Samhandler> {
        return Specification
            .where(navnLike(samhandlerSøk))
            .and(identEquals(samhandlerSøk))
            .and(offentligIdEquals(samhandlerSøk))
            .and(postnummerEquals(samhandlerSøk))
            .and(poststedEquals(samhandlerSøk))
            .and(kontonummerEquals(samhandlerSøk))
            .and(ibanEquals(samhandlerSøk))
            .and(swiftEquals(samhandlerSøk))
            .and(banknavnEquals(samhandlerSøk))
            .and(banklandkodeEquals(samhandlerSøk))
            .and(bankcodeEquals(samhandlerSøk))
    }

    private fun navnLike(samhandlerSøk: SamhandlerSøk): Specification<Samhandler> {
        return Specification<Samhandler> { root, _, criteriaBuilder ->
            if (samhandlerSøk.navn == null) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.like(
                    criteriaBuilder.upper(root.get("navn")),
                    "%" + samhandlerSøk.navn!!.uppercase() + "%",
                )
            }
        }
    }

    private fun identEquals(samhandlerSøk: SamhandlerSøk): Specification<Samhandler> {
        return Specification<Samhandler> { root, _, criteriaBuilder ->
            if (samhandlerSøk.ident == null) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("ident")),
                    samhandlerSøk.ident!!.uppercase(),
                )
            }
        }
    }

    private fun offentligIdEquals(samhandlerSøk: SamhandlerSøk): Specification<Samhandler> {
        return Specification<Samhandler> { root, _, criteriaBuilder ->
            if (samhandlerSøk.offentligId == null) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("offentligId")),
                    samhandlerSøk.offentligId!!.uppercase(),
                )
            }
        }
    }

    private fun postnummerEquals(samhandlerSøk: SamhandlerSøk): Specification<Samhandler> {
        return Specification<Samhandler> { root, _, criteriaBuilder ->
            if (samhandlerSøk.postnummer == null) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("postnr")),
                    samhandlerSøk.postnummer!!.uppercase(),
                )
            }
        }
    }

    private fun poststedEquals(samhandlerSøk: SamhandlerSøk): Specification<Samhandler> {
        return Specification<Samhandler> { root, _, criteriaBuilder ->
            if (samhandlerSøk.poststed == null) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("poststed")),
                    samhandlerSøk.poststed!!.uppercase(),
                )
            }
        }
    }

    private fun kontonummerEquals(samhandlerSøk: SamhandlerSøk): Specification<Samhandler> {
        return Specification<Samhandler> { root, _, criteriaBuilder ->
            if (samhandlerSøk.norskkontonr == null) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("norskkontonr")),
                    samhandlerSøk.norskkontonr!!.uppercase(),
                )
            }
        }
    }

    private fun ibanEquals(samhandlerSøk: SamhandlerSøk): Specification<Samhandler> {
        return Specification<Samhandler> { root, _, criteriaBuilder ->
            if (samhandlerSøk.iban == null) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("iban")),
                    samhandlerSøk.iban!!.uppercase(),
                )
            }
        }
    }

    private fun swiftEquals(samhandlerSøk: SamhandlerSøk): Specification<Samhandler> {
        return Specification<Samhandler> { root, _, criteriaBuilder ->
            if (samhandlerSøk.swift == null) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("swift")),
                    samhandlerSøk.swift!!.uppercase(),
                )
            }
        }
    }

    private fun banknavnEquals(samhandlerSøk: SamhandlerSøk): Specification<Samhandler> {
        return Specification<Samhandler> { root, _, criteriaBuilder ->
            if (samhandlerSøk.banknavn == null) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("banknavn")),
                    samhandlerSøk.banknavn!!.uppercase(),
                )
            }
        }
    }

    private fun banklandkodeEquals(samhandlerSøk: SamhandlerSøk): Specification<Samhandler> {
        return Specification<Samhandler> { root, _, criteriaBuilder ->
            if (samhandlerSøk.banklandkode == null) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("banklandkode")),
                    samhandlerSøk.banklandkode!!.uppercase(),
                )
            }
        }
    }

    private fun bankcodeEquals(samhandlerSøk: SamhandlerSøk): Specification<Samhandler> {
        return Specification<Samhandler> { root, _, criteriaBuilder ->
            if (samhandlerSøk.bankcode == null) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("bankcode")),
                    samhandlerSøk.bankcode!!.uppercase(),
                )
            }
        }
    }
}
