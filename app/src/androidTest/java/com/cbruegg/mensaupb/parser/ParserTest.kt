package com.cbruegg.mensaupb.parser

import androidx.test.runner.AndroidJUnit4
import okio.Okio
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class ParserTest {

    @Test
    fun testRestaurantParsing() {
        @Language("JSON")
        val json = """{
          "mensa-academica-paderborn": {
            "name": "Mensa Academica",
            "location": "Paderborn, Campus",
            "active": true
          },
          "campus-doener": {
            "name": "Campus Döner",
            "location": "Paderborn, Campus",
            "active": false
          }
        }"""

        val restaurants = parseRestaurantsFromApi(json.bufferedSource())

        assertEquals("mensa-academica-paderborn", restaurants[0].id)
        assertEquals("Mensa Academica", restaurants[0].name, "Name should match")
        assertEquals("Paderborn, Campus", restaurants[0].location, "Location should match")
        assertTrue(restaurants[0].isActive, "isActive should match")

        assertEquals("campus-doener", restaurants[1].id)
        assertEquals("Campus Döner", restaurants[1].name, "Name should match")
        assertEquals("Paderborn, Campus", restaurants[1].location, "Location should match")
        assertFalse(restaurants[1].isActive, "isActive should match")
    }

    // No dishes parsing test needed, default Moshi behavior is used

    private fun String.bufferedSource() = Okio.buffer(Okio.source(byteInputStream()))
}