package com.nihfkeol.curriculum.utils

import com.nihfkeol.curriculum.pojo.Course
import com.nihfkeol.curriculum.pojo.MaxCourse
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import kotlin.collections.ArrayList

class ParseUtils(html: String) {
    private lateinit var trElements: Elements
    private val document: Document = Jsoup.parse(html)

    /**
     * 判断是否登录成功
     * @return true成功，则失败
     */
    fun parseIsLogin(): Boolean {
        val font = document.select("font")
        if (!font.isEmpty()) {
            val text = font.text()
            return "请先登录系统" != text && "用户名或密码错误" != text
        }
        return true
    }

    /**
     * 课程表版本，
     * 是否显示任课老师信息
     * @return 返回字符串
     */
    fun parseVersion(i: Int): String {
        trElements = document.select("tbody").last().getElementsByTag("tr")
        val input: Elements = trElements.select("input")
        return input[i].attr("name")
    }

    /**
     * 解析课程
     */
    fun parseCourse(version: String): List<Course> {
        //先记录课程信息
        val courseInfoMap = HashMap<Int, Course.CourseInfo>()
        //记录周几
        var weeks: Array<String?>? = null
        //记录map的Integer的值
        var countMapKey = -1
        //记录网页的列数，最后一列为空所以忽略
        val forIMax = trElements.size - 1
        for (i in 0 until forIMax) {
            val elementI = trElements[i]
            if (i == 0) {
                //存放星期，并确定courseList的长度
                val thElements = elementI.getElementsByTag("th")
                weeks = arrayOfNulls(thElements.size - 2)
                for (j in weeks.indices) {
                    weeks[j] = thElements[j].text()
                }
            } else {
                for (j in weeks!!.indices) {
                    val info = Course.CourseInfo(null, null)
                    //当j=0的时候显示的是上课时间
                    if (j == 0) {
                        val e = elementI.getElementsByTag("th").first()
                        info.ClassTime = e.text()
                    } else {
                        val e = elementI.getElementsByTag("td")[j - 1]
                        val input1 = e.getElementsByTag("input")
                        var courseVersion: String? = null
                        for (elementInputs in input1) {
                            if (elementInputs.attr("name") == version) {
                                courseVersion = elementInputs.attr("value")
                                break
                            }
                        }
                        val selectDIV = e.select("div#$courseVersion")
                        info.CourseInfoString = selectDIV.text()
                    }
                    countMapKey++
                    courseInfoMap[countMapKey] = info
                }
            }
        }

        //用于储存周几的所有课程
        val courseList = ArrayList<Course>()
        var infoList: ArrayList<Course.CourseInfo>
        for (i in weeks!!.indices) {
            infoList = ArrayList()
            for (key in courseInfoMap.keys) {
                if (key % 6 == i) {
                    infoList.add(courseInfoMap[key]!!)
                }
            }
            val course = Course(weeks[i]!!, infoList)
            courseList.add(course)
        }
        return courseList
    }

    /**
     * 最大周数
     * 课程列表
     */
    fun parseMaxCourse(courseList: List<Course>): MaxCourse {
        //最大周数（第几周没课）
        var maxWeek = 0
        //多少种课
        val set = HashSet<String>()
        for (i in 1 until courseList.size) {
            val courseInfoList = courseList[i].courseList
            for (courseInfo in courseInfoList) {
                val ciStr = courseInfo.CourseInfoString
                if ("" == ciStr) continue
                var infoList = ciStr!!.split(" ---------------------- ")
                if (infoList.size == 1) {
                    infoList = ciStr.split(" --------------------- ")
                }
                for (info in infoList) {
                    val ciStrArr = info.split(" ")
                    set.add(ciStrArr[0])
                    var weekArr = ciStrArr[ciStrArr.size - 2].split("-")
                    var weekStr: String
                    if (weekArr.size != 2) {
                        weekArr = ciStrArr[ciStrArr.size - 2].split(",")
                        weekStr = if (weekArr.size != 2) {
                            weekArr[0]
                        } else {
                            weekArr[1]
                        }
                    } else {
                        weekStr = weekArr[1]
                    }
                    if (weekStr.length != 5) continue
                    val weekNum = weekStr.substring(0, 2).toInt()
                    if (maxWeek < weekNum) {
                        maxWeek = weekNum
                    }
                }
            }
        }
        return MaxCourse(maxWeek, set)
    }

    /**
     * 获取学年列表
     */
    fun parseSchoolYear(): List<String> {
        trElements = document.getElementById("kksj").select("option")
        val list = ArrayList<String>()
        //只显示最近5年
        for (i in 0 until 6) {
            list.add(trElements[i].attr("value"))
        }
        return list
    }

    fun parseTranscript(): List<Map<String, String>> {
        trElements = document.getElementById("dataList").select("tr")
        val transcripts = ArrayList<Map<String, String>>()
        for (element in trElements) {
            val elements = element.allElements
            val map = HashMap<String, String>().also {
                it["courseTitle"] = elements[4].text()
                it["score"] = elements[5].text()
            }
            transcripts.add(map)
        }
        return transcripts
    }
}