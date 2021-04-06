package com.nihfkeol.curriculum.pojo

/**
 * 课表信息
 */
data class Course(
    //星期
    var Week: String,
    //当天的课程
    var courseList: ArrayList<CourseInfo>
){
    data class CourseInfo(
        var ClassTime: String?,
        var CourseInfoString: String?
    )
}