package fyi.punkdev.thymian.domain.frontdoor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FrontDoorDestinationTest {
    @Test
    fun `with no widget placed the front door offers to add one`() {
        assertEquals(FrontDoorDestination.AddWidget, FrontDoorDestination.choose(placedWidgetIds = emptyList()))
    }

    @Test
    fun `with one widget placed the front door opens its settings`() {
        assertEquals(FrontDoorDestination.WidgetSettings(12), FrontDoorDestination.choose(placedWidgetIds = listOf(12)))
    }

    @Test
    fun `with several widgets placed the front door opens the most recently placed`() {
        // Launchers hand out widget ids in increasing order, so the highest id is the newest widget.
        assertEquals(FrontDoorDestination.WidgetSettings(40), FrontDoorDestination.choose(placedWidgetIds = listOf(7, 40, 23)))
    }

    @Test
    fun `a widget added while the front door waited is found by its new id`() {
        assertEquals(31, FrontDoorDestination.newlyPlacedWidget(placedBefore = listOf(7, 23), placedNow = listOf(7, 23, 31)))
    }

    @Test
    fun `no new widget is found when nothing was added`() {
        assertNull(FrontDoorDestination.newlyPlacedWidget(placedBefore = listOf(7, 23), placedNow = listOf(7, 23)))
    }

    @Test
    fun `a removed widget is not mistaken for a new one`() {
        assertNull(FrontDoorDestination.newlyPlacedWidget(placedBefore = listOf(7, 23), placedNow = listOf(7)))
    }
}
