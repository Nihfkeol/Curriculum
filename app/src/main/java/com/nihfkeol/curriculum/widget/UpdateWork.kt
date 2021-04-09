package com.nihfkeol.curriculum.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.view.View
import android.widget.RemoteViews
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.nihfkeol.curriculum.R
import com.nihfkeol.curriculum.utils.FileUtils
import com.nihfkeol.curriculum.utils.ParseUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class UpdateWork(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        //创建视图
        val views = RemoteViews(context.packageName, R.layout.widget_curriculum)
        val shp = context.getSharedPreferences(
            context.getString(R.string.SHP_NAME),
            Context.MODE_PRIVATE
        )
        //获取是否保存课表
        val getSaveCourse = shp.getBoolean(
            context.getString(R.string.IS_SAVE_COURSE_KEY),
            false
        )
        if (getSaveCourse) {
            //获取开学时间
            val startDate = shp.getString(
                context.getString(R.string.START_DATE_KEY),
                ""
            )
            //现在的时间
            val nowDate = Calendar.getInstance()
            //格式化时间
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            val firstDate = format.parse(startDate!!)
            val nd = 1_000 * 24 * 60 * 60
            //时间差
            val diff = nowDate.time.time - firstDate!!.time
            //现在第几周
            val nowWeek = (diff / nd / 7).toInt() + 1
            //现在星期几
            val nowDay = nowDate.get(Calendar.DAY_OF_WEEK).let {
                val tmp = it - 1
                if (tmp == 0) 7 else tmp
            }
            //文件名
            val fileName = context.getString(R.string.FILE_NAME) + "_" + nowWeek
            //文件路径
            val filePath =
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            //判断文件是否存在
            if (filePath.exists()) {
                val fileUtils = FileUtils().getInstance()
                val courseHtml = fileUtils!!.readHtml(filePath)!!
                val parseUtils = ParseUtils(courseHtml)
                val versionStr = parseUtils.parseVersion(0)
                //这周的课表
                val courseList = parseUtils.parseCourse(versionStr)
                //获取当天课表
//                        val courseOfDayList = ArrayList<String>()
                var courseOfDayStr = ""
                //获取当天的课程放入到courseOfDayList中
                for (i in courseList[nowDay].courseList.indices) {
                    val str = courseList[nowDay].courseList[i].CourseInfoString!!
                    var classTime = ""
                    when (i) {
                        0 -> {
                            classTime = "${i + 1},8:30-9:10 9:15-9:55,"
                        }
                        1 -> {
                            classTime = "${i + 1},10:10-10:50 10:55-11:35,"
                        }
                        2 -> {
                            classTime = "${i + 1},11:40-12:20,"
                        }
                        3 -> {
                            classTime = "${i + 1},14:20-15:00 15:05-15:45,"
                        }
                        4 -> {
                            classTime = "${i + 1},16:00-16:40 16:45-17:25,"
                        }
                        5 -> {
                            classTime = "${i + 1},19:10-19:50 20:00-20:40 20:50-21:30,"
                        }
                    }
                    //拼接字符串，并添加到list中strArr[0]课程名，strArr[strArr.size - 1]上课教室
//                            courseOfDayList.add(classTime.let {
//                                val strArr = str.split(" ")
//                                val s = "${strArr[0]} ${strArr[strArr.size - 1]}"
//                                it + s
//                            })
                    courseOfDayStr += classTime.let {
                        val strArr = str.split(" ")
                        val s = "${strArr[0]} ${strArr[strArr.size - 1]}"
                        if (i != 5)
                            "$it$s#"
                        else
                            "$it$s"
                    }
                }
                //设置adapter
                val intent = Intent(context, CourseOfDayListViewService::class.java)
                shp.edit().apply {
                    putString("courseListKEY", courseOfDayStr)
                    apply()
                }
                views.apply {
                    setRemoteAdapter(R.id.courseOfDayListView, intent)
                    //提示文隐藏
                    setViewVisibility(R.id.appwidget_text, View.GONE)
                    //课程列表显示
                    setViewVisibility(R.id.courseOfDayListView, View.VISIBLE)
                }
            } else {
                views.apply {
                    setTextViewText(R.id.appwidget_text, "文件不存在")
                    setViewVisibility(R.id.appwidget_text, View.VISIBLE)
                    setViewVisibility(R.id.courseOfDayListView, View.GONE)
                }
            }
        } else {
            views.apply {
                setViewVisibility(R.id.appwidget_text, View.VISIBLE)
                setViewVisibility(R.id.courseOfDayListView, View.GONE)
            }
        }
        val manager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, CurriculumWidget::class.java)
        manager.notifyAppWidgetViewDataChanged(
            manager.getAppWidgetIds(componentName),
            R.id.courseOfDayListView
        )
        manager.updateAppWidget(componentName, views)
        return Result.success()
    }
}