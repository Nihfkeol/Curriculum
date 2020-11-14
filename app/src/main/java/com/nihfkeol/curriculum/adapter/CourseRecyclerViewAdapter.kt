package com.nihfkeol.curriculum.adapter

import android.content.Context
import android.graphics.Rect
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.nihfkeol.curriculum.R
import com.nihfkeol.curriculum.pojo.Course
import com.nihfkeol.curriculum.pojo.ItemColor
import kotlinx.android.synthetic.main.item_course.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.random.Random

class CourseRecyclerViewAdapter(
    private val context: Context,
    private val courses: List<Course>,
    private val textWidth: Int,
    private val week: String,
    private val countCourse: Set<String>
) : RecyclerView.Adapter<CourseRecyclerViewAdapter.MyViewHolder>() {
    private var itemColors: Array<ItemColor?> = emptyArray()
    private var courseArray: Array<String> = emptyArray()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.courseArray = countCourse.toTypedArray()
        val size = countCourse.size
        val textColors =
            context.resources.getIntArray(R.array.recyclerTextColors).toMutableList()
        val cardViewColors =
            context.resources.getIntArray(R.array.recyclerItemColors).toMutableList()
        itemColors = arrayOfNulls(size)
        for (i in itemColors.indices) {
            val j = (cardViewColors.indices).random()
            itemColors[i] = ItemColor(textColors[j], cardViewColors[j])
            textColors.removeAt(j)
            cardViewColors.removeAt(j)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_course, parent, false)
        val myViewHolder = MyViewHolder(view)
        myViewHolder.courseTextView.width = textWidth

        return myViewHolder
    }

    override fun getItemCount(): Int {
        val w = courses.size
        val h = courses.size * courses[0].courseList.size
        return w + h
    }

    /**
     * 总共有courses.size() + courses.size()*courses.get(0).getCourseList().size()个item，
     * 而courseInfoList小于总数，所以每七个差值就会+1，
     * 所以用整形difference记录差值
     */
    private var difference: Int = 0
    private lateinit var courseInfoList: List<Course.CourseInfo>
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //是否显示提示框
        var isShowDialog = false
        val alertDialogBuilder = AlertDialog.Builder(context)
        //星期+课，总共有7行 42%7判断是第几行的
        val countCourse = position % 7
        //第一行，星期
        if (countCourse == 0) {
            difference = position / 7 + 1
            val course = courses[position / 7]
            courseInfoList = course.courseList
            if (position == 0) {
                val countDay = week.toLong()
                val text = "第" + countDay + "周"
                holder.courseTextView.text = text
            } else {
                //显示星期几
                val weekDay: String = course.Week
                holder.courseTextView.text = weekDay
            }
        } else {
            val courseInfo = courseInfoList[position % 7 - 1]
            //差值为一代表第一列，第一列是每节课的时间
            if (difference == 1) {
                var classTime = "<big><big>" + courseInfo.ClassTime + "</big></big>"
                when (position) {
                    1 -> {
                        classTime += "\n8:30 - 9:10\n9:15 - 9:55"
                    }
                    2 -> {
                        classTime += "\n10:10 - 10:50\n10:55 - 11:35"
                    }
                    3 -> {
                        classTime += "\n11:40 - 12:20"
                    }
                    4 -> {
                        classTime += "\n14:20 - 15:00\n15:05 - 15:45"
                    }
                    5 -> {
                        classTime += "\n16:00 - 16:40\n16:45 - 17:25"
                    }
                    6 -> {
                        classTime += "\n19:10 - 19:50\n20:00 - 20:40\n20:50 - 21:30"
                    }
                }
                holder.courseTextView.textSize = 10f
                holder.courseTextView.text = Html.fromHtml(classTime, Html.FROM_HTML_MODE_COMPACT)
            } else {
                val courseInfoString = courseInfo.CourseInfoString
                holder.courseTextView.text = courseInfoString
                var split = courseInfoString?.split(" ---------------------- ")
                /**
                 * 长度为1，有可能是使用了另一个版本的课程表
                 * 试着换一种字符串分割
                 */
                if (split?.size == 1) {
                    split = courseInfoString?.split(" --------------------- ")
                }
                if (null != courseInfoString) {
                    isShowDialog = true
                    alertDialogBuilder.setAdapter(
                        ArrayAdapter(
                            context,
                            android.R.layout.simple_list_item_1,
                            split!!
                        ), null
                    )

                    //从课程信息中分解出课程名
                    val cname = courseInfoString.split(" ")[0]
                    for (i in courseArray.indices) {
                        if (cname == courseArray[i]) {
                            holder.cView.setCardBackgroundColor(itemColors[i]!!.getCardViewColor())
                            holder.courseTextView.setTextColor(itemColors[i]!!.getTextColor())
                            break
                        }
                    }
                }
            }
        }
        if (isShowDialog) {
            holder.cView.setOnClickListener {
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }
    }

    inner class MyViewHolder(
        view: View
    ) :
        RecyclerView.ViewHolder(view) {
        var courseTextView = view.findViewById<TextView>(R.id.courseTextView)!!
        var cView = view.findViewById<CardView>(R.id.cView)!!
    }

    class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            if (parent.getChildAdapterPosition(view) / 7 == 0) {
                outRect.left = 0
            } else {
                outRect.left = space
            }

            val spaceHalf = space / 2
            if (parent.getChildAdapterPosition(view) % 7 == 0) {
                outRect.top = 0
            } else {
                outRect.top = spaceHalf
            }
        }
    }
}