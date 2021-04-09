package com.nihfkeol.curriculum.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.nihfkeol.curriculum.R

class CourseOfDayListViewService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return ListRemoteViewsFactory(this.applicationContext)
    }

    inner class ListRemoteViewsFactory(
        private val context: Context
    ) : RemoteViewsFactory {
        private lateinit var courseList : List<String>
        private var textColor : Int = 0

        private val shp = context.getSharedPreferences(
            context.getString(R.string.SHP_NAME),
            Context.MODE_PRIVATE
        )

        override fun onCreate() {
        }

        override fun onDataSetChanged() {
            val courseOfDayStr = shp.getString("courseListKEY","")
            courseList = courseOfDayStr!!.split("#")
            textColor = Color.parseColor(shp.getString(context.resources.getString(R.string.WIDGET_TEXT_COLOR_KEY), "#000000"))
        }

        override fun onDestroy() {
            courseList = emptyList()
        }

        override fun getCount(): Int {
            return courseList.size
        }

        override fun getViewAt(position: Int): RemoteViews {
            val str = courseList[position]
            val strArr = str.split(",")
            return RemoteViews(context.packageName, R.layout.item_course_of_day).apply {
                setTextColor(R.id.countTextView,textColor)
                setTextViewText(R.id.countTextView, strArr[0])
                setTextColor(R.id.timeTextView,textColor)
                setTextViewText(R.id.timeTextView,splitStr(strArr[1]))
                setTextColor(R.id.courseInfoTextView,textColor)
                setTextViewText(R.id.courseInfoTextView,splitStr(strArr[2]))
            }
        }

        private fun splitStr(s: String): String {
            var strTmp = ""
            val strArr = s.split(" ")
            for (i in strArr.indices){
                strTmp += if (i != strArr.size-1){
                    "${strArr[i]}\n"
                }else{
                    strArr[i]
                }
            }
            return strTmp
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }

    }

}