package fyi.punkdev.thymian.background

import android.app.ApplicationExitInfo
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fyi.punkdev.thymian.domain.background.BackgroundNames
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/** What the phone says about background work, read without any permission. */
@RunWith(AndroidJUnit4::class)
class PlatformBackgroundObservationsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val observations = PlatformBackgroundObservations(context)

    @Test
    fun `the domain bucket numbers match those of Android`() {
        assertEquals("active", BackgroundNames.standbyBucket(UsageStatsManager.STANDBY_BUCKET_ACTIVE))
        assertEquals("working_set", BackgroundNames.standbyBucket(UsageStatsManager.STANDBY_BUCKET_WORKING_SET))
        assertEquals("frequent", BackgroundNames.standbyBucket(UsageStatsManager.STANDBY_BUCKET_FREQUENT))
        assertEquals("rare", BackgroundNames.standbyBucket(UsageStatsManager.STANDBY_BUCKET_RARE))
        assertEquals("restricted", BackgroundNames.standbyBucket(UsageStatsManager.STANDBY_BUCKET_RESTRICTED))
    }

    @Test
    fun `the domain exit reason numbers match those of Android`() {
        assertEquals("low_memory", BackgroundNames.exitReason(ApplicationExitInfo.REASON_LOW_MEMORY))
        assertEquals("user_requested", BackgroundNames.exitReason(ApplicationExitInfo.REASON_USER_REQUESTED))
        assertEquals("user_stopped", BackgroundNames.exitReason(ApplicationExitInfo.REASON_USER_STOPPED))
        assertEquals("freezer", BackgroundNames.exitReason(ApplicationExitInfo.REASON_FREEZER))
        assertEquals("package_updated", BackgroundNames.exitReason(ApplicationExitInfo.REASON_PACKAGE_UPDATED))
    }

    @Test
    fun `the current state names a known bucket`() {
        assertFalse(observations.currentState().standbyBucket.startsWith("bucket_"))
    }

    @Test
    fun `history can be read for the past day without failing`() {
        val dayAgo = Instant.now().minusSeconds(24 * 60 * 60)

        assertTrue(observations.standbyBucketsSince(dayAgo).none { bucket -> bucket.isEmpty() })
        assertTrue(observations.processExitsSince(dayAgo).all { exit -> exit.exitedAt.isAfter(dayAgo) })
    }
}
