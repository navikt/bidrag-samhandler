package no.nav.bidrag.samhandler.controller

import no.nav.bidrag.domene.enums.samhandler.OffentligIdType
import no.nav.bidrag.domene.util.visningsnavn
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class Visningsnavnkontroller {
    @GetMapping("/visningsnavn")
    fun hentVisningsnavn(): Map<String, String> = OffentligIdType.entries.associate { it.name to it.visningsnavn.intern }
}
