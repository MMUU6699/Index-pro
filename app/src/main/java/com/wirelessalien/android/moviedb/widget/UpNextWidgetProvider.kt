/*
 *     This file is part of "ShowCase" formerly Movie DB. <https://github.com/WirelessAlien/MovieDB>
 *     forked from <https://notabug.org/nvb/MovieDB>
 *
 *     Copyright (C) 2024  WirelessAlien <https://github.com/WirelessAlien>
 *
 *     ShowCase is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ShowCase is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with "ShowCase".  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wirelessalien.android.moviedb.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.wirelessalien.android.moviedb.R
import com.wirelessalien.android.moviedb.activity.MainActivity
import com.wirelessalien.android.moviedb.helper.MovieDatabaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UpNextWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_MARK_AS_WATCHED) {
            val showId = intent.getIntExtra(EXTRA_SHOW_ID, -1)
            val seasonNumber = intent.getIntExtra(EXTRA_SEASON_NUMBER, -1)
            val episodeNumber = intent.getIntExtra(EXTRA_EPISODE_NUMBER, -1)

            if (showId != -1 && seasonNumber != -1 && episodeNumber != -1) {
                markEpisodeAsWatched(context, showId, seasonNumber, episodeNumber)

                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, UpNextWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_up_next)

        val intent = Intent(context, UpNextWidgetService::class.java)
        views.setRemoteAdapter(R.id.widget_list_view, intent)

        val appIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_headerRL, pendingIntent)

        val clickIntent = Intent(context, UpNextWidgetProvider::class.java).apply {
            action = ACTION_MARK_AS_WATCHED
        }
        val clickPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setPendingIntentTemplate(R.id.widget_list_view, clickPendingIntent)


        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun markEpisodeAsWatched(context: Context, showId: Int, seasonNumber: Int, episodeNumber: Int) {
        val dbHelper = MovieDatabaseHelper(context)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        dbHelper.addEpisodeNumber(showId, seasonNumber, listOf(episodeNumber), currentDate)
    }

    companion object {
        const val ACTION_MARK_AS_WATCHED = "com.wirelessalien.android.moviedb.widget.action.MARK_AS_WATCHED"
        const val EXTRA_SHOW_ID = "com.wirelessalien.android.moviedb.widget.extra.SHOW_ID"
        const val EXTRA_SEASON_NUMBER = "com.wirelessalien.android.moviedb.widget.extra.SEASON_NUMBER"
        const val EXTRA_EPISODE_NUMBER = "com.wirelessalien.android.moviedb.widget.extra.EPISODE_NUMBER"
    }
}
