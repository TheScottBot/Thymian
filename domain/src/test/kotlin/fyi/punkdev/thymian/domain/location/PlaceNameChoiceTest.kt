package fyi.punkdev.thymian.domain.location

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Geocoder results can lack a locality (London did in the Plan spike, section 7.2). */
class PlaceNameChoiceTest {
    @Test
    fun `the locality is preferred`() {
        assertEquals("München", PlaceNameChoice.choose(locality = "München", subAdminArea = "Oberbayern", adminArea = "Bayern"))
    }

    @Test
    fun `without a locality the sub administrative area is used, then the administrative area`() {
        assertEquals("Greater London", PlaceNameChoice.choose(locality = null, subAdminArea = "Greater London", adminArea = "England"))
        assertEquals("Berlin", PlaceNameChoice.choose(locality = " ", subAdminArea = null, adminArea = "Berlin"))
    }

    @Test
    fun `nothing usable gives no name`() {
        assertEquals(null, PlaceNameChoice.choose(locality = null, subAdminArea = "", adminArea = null))
    }

    @Test
    fun `an unreasonably long name is cut to a widget sized length`() {
        val longName = "Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch".repeat(3)

        assertEquals(
            PlaceNameChoice.MAXIMUM_LENGTH,
            PlaceNameChoice.choose(locality = longName, subAdminArea = null, adminArea = null)!!.length,
        )
    }
}
