package com.gymquest.app.core.ui.component

import org.junit.Assert.assertTrue
import org.junit.Test

class QuestActionTest {
    @Test fun `every action exposes an accessible description`() = assertTrue(QuestAction.entries.all { it.contentDescription.isNotBlank() })
    @Test fun `destructive actions require confirmation`() = assertTrue(QuestAction.entries.filter { it.role == QuestActionRole.Destructive }.all { it.requiresConfirmation })
}
