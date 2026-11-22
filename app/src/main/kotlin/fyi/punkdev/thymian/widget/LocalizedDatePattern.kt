package fyi.punkdev.thymian.widget

import android.text.format.DateFormat
import fyi.punkdev.thymian.domain.ClockFormatPolicy
import java.util.Locale

/** Asks Android for the locale's best date pattern for the domain's date skeleton (CWD13). */
object LocalizedDatePattern {
    fun forLocale(locale: Locale): String = DateFormat.getBestDateTimePattern(locale, ClockFormatPolicy.DATE_SKELETON)
}
