package no.nav.bidrag.samhandler.util

fun String?.nullIfEmpty() = if (this?.trim().isNullOrEmpty()) null else this
