package com.gymquest.app.data.seed

import com.gymquest.app.domain.model.enums.MartialTechniqueFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShitoRyuCatalogTest {
    @Test
    fun `recognises Shito-Ryu spelling variants regardless of case or accents`() {
        assertTrue(ShitoRyuCatalog.matchesStyle("Shito-Ryu"))
        assertTrue(ShitoRyuCatalog.matchesStyle("shito ryu"))
        assertTrue(ShitoRyuCatalog.matchesStyle("SHITŌ RYŪ"))
        assertFalse(ShitoRyuCatalog.matchesStyle("Shotokan"))
    }

    @Test
    fun `contains the complete reusable Shito-Ryu library`() {
        assertEquals(13, ShitoRyuCatalog.stances.size)
        assertEquals(23, ShitoRyuCatalog.techniques.size)
        assertEquals(13, ShitoRyuCatalog.stances.map { ShitoRyuCatalog.normalizedEntryName(it.name) }.toSet().size)
        assertEquals(23, ShitoRyuCatalog.techniques.map { ShitoRyuCatalog.normalizedEntryName(it.name) }.toSet().size)
        assertTrue(ShitoRyuCatalog.stances.all { it.translation.isNotBlank() && it.description.isNotBlank() })
        assertTrue(ShitoRyuCatalog.techniques.all { it.translation.isNotBlank() && it.description.isNotBlank() })
        assertEquals(MartialTechniqueFamily.PUNCH, ShitoRyuCatalog.techniques.first { it.name == "Oi Zuki" }.family)
        assertEquals(MartialTechniqueFamily.KICK, ShitoRyuCatalog.techniques.first { it.name == "Mae Geri" }.family)
        assertEquals(MartialTechniqueFamily.OPEN_HAND, ShitoRyuCatalog.techniques.first { it.name == "Shuto Uke" }.family)
    }
}
