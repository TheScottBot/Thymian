package fyi.punkdev.thymian.widget

import android.appwidget.AppWidgetProvider

/**
 * Receives the system's widget callbacks. Deliberately empty in CWE1: the
 * initial layout from the provider info is all the placeholder needs. It stays
 * thin when rendering arrives, delegating every callback (specification 2.2).
 */
class ThymianWidgetProvider : AppWidgetProvider()
