package vyan.alwaysonwidget.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import vyan.alwaysonwidget.R
import vyan.alwaysonwidget.services.StayAwakeService
import vyan.alwaysonwidget.services.StayAwakeService.Companion.EXTRA_START_AWAKE_FLAG
import vyan.alwaysonwidget.services.StayAwakeService.Companion.EXTRA_STAY_AWAKE_STATE

/**
 * Provider for app widget that toggles the stay awake functionality.
 * App widget dimens are 1x1 and non-resizable.
 */
class ToggleAwakeAppWidget : AppWidgetProvider() {
    private var isToggledState = false


    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                isToggledState = intent.getBooleanExtra(EXTRA_STAY_AWAKE_STATE, false)
                buildRemoteViews(context)
            }
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Intent(context, StayAwakeService::class.java).also { intent ->
            context.startService(intent)
        }
        buildRemoteViews(context, appWidgetManager, appWidgetIds)
    }

    private fun buildRemoteViews(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(
                context,
                ToggleAwakeAppWidget::class.java
            )
        )
        buildRemoteViews(context, appWidgetManager, appWidgetIds)
    }

    private fun buildRemoteViews(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            // Construct the RemoteViews object
            val views = RemoteViews(
                context.packageName,
                R.layout.toggle_awake_app_widget
            ).apply {
                // Set up toggle button
                var toggleImageSrc = R.drawable.ic_always_on_toggle_off_24dp
                // Set up click intent
                var clickIntent = PendingIntent.getService(
                    context,
                    0,
                    Intent(context, StayAwakeService::class.java).apply {
                        putExtra(EXTRA_START_AWAKE_FLAG, true)
                    },
                    0
                )

                if (isToggledState) {
                    toggleImageSrc = R.drawable.ic_always_on_toggle_on_24dp
                    clickIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(StayAwakeService.ACTION_TOGGLE_STAY_AWAKE),
                        0
                    )
                }
                setImageViewResource(R.id.toggle_awake_app_widget_button, toggleImageSrc)
                setOnClickPendingIntent(R.id.toggle_awake_app_widget_button, clickIntent)
            }

            // Instruct the widget manager to update the widget(s)
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}

