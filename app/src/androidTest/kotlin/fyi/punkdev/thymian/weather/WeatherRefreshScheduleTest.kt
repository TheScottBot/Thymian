package fyi.punkdev.thymian.weather

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * On 24 November 2026 the Pixel's stored schedule still required a network, from
 * before deviation D11 removed it, because an existing schedule was kept as it
 * was. With background data off on mobile, it then never ran away from Wi-Fi.
 */
@RunWith(AndroidJUnit4::class)
class WeatherRefreshScheduleTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val workManager = WorkManager.getInstance(context)

    @Test
    fun `scheduling again replaces a stored network requirement`() {
        val oldRequest =
            PeriodicWorkRequest
                .Builder(WeatherRefreshWorker::class.java, 30, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
        workManager
            .enqueueUniquePeriodicWork(WeatherRefreshWorker.PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, oldRequest)
            .result
            .get()

        WeatherRefreshWorker.ensurePeriodic(context)

        val scheduled =
            workManager
                .getWorkInfosForUniqueWork(WeatherRefreshWorker.PERIODIC_WORK_NAME)
                .get()
                .single { workInfo -> !workInfo.state.isFinished }
        assertEquals(NetworkType.NOT_REQUIRED, scheduled.constraints.requiredNetworkType)
    }
}
