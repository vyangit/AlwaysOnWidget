package vyan.alwaysonwidget.widgets

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import vyan.alwaysonwidget.R
import vyan.alwaysonwidget.services.StayAwakeService
import vyan.alwaysonwidget.services.StayAwakeService.Companion.EXTRA_START_AWAKE_FLAG
import vyan.alwaysonwidget.services.StayAwakeService.Companion.EXTRA_STAY_AWAKE_STATE

private const val ACTION_SELF_STARTER =
    "vyan.alwaysonwidget.widgets.ToggleAwakeAppWidget.ACTION_SELF_STARTER"
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
            ACTION_SELF_STARTER -> {
                isToggledState = true
                startForegroundStayAwakeService(context)
            }
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
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
                var clickIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent().apply {
                        action = ACTION_SELF_STARTER
                        component = ComponentName(context, ToggleAwakeAppWidget::class.java)
                    },
                    FLAG_CANCEL_CURRENT
                )

                if (isToggledState) {
                    toggleImageSrc = R.drawable.ic_always_on_toggle_on_24dp
                    clickIntent = PendingIntent.getBroadcast(
                        context,
                        1,
                        Intent(StayAwakeService.ACTION_TOGGLE_STAY_AWAKE),
                        FLAG_CANCEL_CURRENT
                    )
                }
                setImageViewResource(R.id.toggle_awake_app_widget_button, toggleImageSrc)
                setOnClickPendingIntent(R.id.toggle_awake_app_widget_button, clickIntent)
            }

            // Instruct the widget manager to update the widget(s)
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    private fun startForegroundStayAwakeService(context: Context) {
        Intent(context, StayAwakeService::class.java).apply {
            putExtra(EXTRA_START_AWAKE_FLAG, isToggledState)
        }.also { intent ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}

