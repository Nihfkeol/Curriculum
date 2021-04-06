package com.nihfkeol.curriculum.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class CurriculumWidget : AppWidgetProvider() {
    private val workName = "curriculumWork"

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        //每隔15分钟执行一次的工作
        val request = PeriodicWorkRequest.Builder(UpdateWork::class.java, 1, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context!!)
            .enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        WorkManager.getInstance(context!!).cancelAllWork()
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        WorkManager.getInstance(context!!).cancelUniqueWork(workName)
    }

}
