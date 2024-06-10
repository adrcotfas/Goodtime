package com.apps.adrcotfas.goodtime.utils

import kotlin.test.Test
import kotlin.test.assertTrue

class NumberSuffixTest {

    @Test
    fun `Generate Unique Name For Duplicate`() {
        val existingNames = listOf("Default", "Label1", "Label2", "Label3", "Label666")
        var result = generateUniqueNameForDuplicate("Label", existingNames)
        assertTrue(result == "Label4")
        result = generateUniqueNameForDuplicate("Default", existingNames)
        assertTrue(result == "Default2")
        result = generateUniqueNameForDuplicate("Label666", existingNames)
        assertTrue(result == "Label667")
    }
}