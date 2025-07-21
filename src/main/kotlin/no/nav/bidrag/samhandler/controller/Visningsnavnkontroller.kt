package no.nav.bidrag.samhandler.controller

import no.nav.bidrag.domene.enums.samhandler.OffentligIdType
import no.nav.bidrag.domene.util.visningsnavn
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class Visningsnavnkontroller {
    @GetMapping("/visningsnavn")
    fun hentVisningsnavn(): Map<String, String> = OffentligIdType.entries.associate { it.name to it.visningsnavn.intern }
}
