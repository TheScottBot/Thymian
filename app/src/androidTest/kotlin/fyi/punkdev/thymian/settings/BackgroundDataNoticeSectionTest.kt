package fyi.punkdev.thymian.settings

import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** The background data notice is in plain sight, not in a closed section, and one button fixes it. */
@RunWith(AndroidJUnit4::class)
class BackgroundDataNoticeSectionTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val themedContext = ContextThemeWrapper(context, android.R.style.Theme_DeviceDefault)

    private fun onMainThread(testBody: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(testBody)

    private fun View.descendants(): List<View> =
        if (this is ViewGroup) {
            listOf(this) +
                (0 until childCount).flatMap { childIndex ->
                    getChildAt(childIndex).descendants()
                }
        } else {
            listOf(this)
        }

    @Test
    fun `no notice when background data is allowed`() =
        onMainThread {
            assertNull(BackgroundDataNoticeSection.forState(themedContext, shouldShow = false) { })
        }

    @Test
    fun `the notice explains the problem and its button opens the setting`() =
        onMainThread {
            var opened = 0
            val section = checkNotNull(BackgroundDataNoticeSection.forState(themedContext, shouldShow = true) { opened += 1 })
            val views = section.view.descendants()

            assertTrue(views.all { view -> view.visibility == View.VISIBLE })
            assertTrue(views.filterIsInstance<TextView>().any { textView -> "mobile data" in textView.text.toString() })
            views
                .filterIsInstance<Button>()
                .single()
                .performClick()
            assertEquals(1, opened)
        }

    @Test
    fun `the setting is asked for this app`() {
        val intent = BackgroundDataNoticeSection.backgroundDataSettingsIntent(context)

        assertEquals(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS, intent.action)
        assertEquals(Uri.fromParts("package", context.packageName, null), intent.data)
    }
}
