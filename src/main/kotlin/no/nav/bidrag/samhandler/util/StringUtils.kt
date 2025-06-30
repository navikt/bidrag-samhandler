package no.nav.bidrag.samhandler.util

fun String?.nullIfEmpty() = if (this.isNullOrEmpty()) null else this
