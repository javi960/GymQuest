package com.gymquest.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterCategoryRulesTest {
    @Test
    fun `first category lasts through level ten`() {
        assertEquals(1, CharacterCategoryRules.categoryFor(1))
        assertEquals(1, CharacterCategoryRules.categoryFor(10))
        assertEquals(2, CharacterCategoryRules.categoryFor(11))
    }

    @Test
    fun `category thresholds grow by ten levels`() {
        assertEquals(2, CharacterCategoryRules.categoryFor(30))
        assertEquals(3, CharacterCategoryRules.categoryFor(31))
        assertEquals(3, CharacterCategoryRules.categoryFor(60))
        assertEquals(4, CharacterCategoryRules.categoryFor(61))
        assertEquals(5, CharacterCategoryRules.categoryFor(101))
    }

    @Test
    fun `definitions expose stable names and png filenames`() {
        assertEquals(51, CharacterCategoryRules.definitions.size)
        assertEquals("Recluta", CharacterCategoryRules.definitionForCategory(1).name)
        assertEquals("recluta.png", CharacterCategoryRules.definitionForCategory(1).imageFileName)
        assertEquals("guerrero_de_acero.png", CharacterCategoryRules.definitionForCategory(15).imageFileName)
        assertEquals("leyenda_suprema.png", CharacterCategoryRules.definitionForCategory(50).imageFileName)
        assertEquals(50, CharacterCategoryRules.definitionFor(12_251).number)
    }

    @Test
    fun `eternal category has no upper level limit`() {
        assertEquals(51, CharacterCategoryRules.categoryFor(12_751))
        assertEquals(51, CharacterCategoryRules.categoryFor(Int.MAX_VALUE))
        assertEquals("Eterno", CharacterCategoryRules.definitionFor(12_751).name)
        assertEquals("eterno.png", CharacterCategoryRules.definitionFor(Int.MAX_VALUE).imageFileName)
    }

    @Test
    fun `category boundaries follow the published avatar table`() {
        assertEquals(10, CharacterCategoryRules.categoryFor(550))
        assertEquals(11, CharacterCategoryRules.categoryFor(551))
        assertEquals(50, CharacterCategoryRules.categoryFor(12_750))
        assertEquals(51, CharacterCategoryRules.categoryFor(12_751))
        assertEquals(451, CharacterCategoryRules.firstLevelFor(10))
        assertEquals(12_251, CharacterCategoryRules.firstLevelFor(50))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `category rejects non positive levels`() {
        CharacterCategoryRules.categoryFor(0)
    }
}
