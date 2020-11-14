package com.nihfkeol.curriculum.pojo

/**
 * 课表信息
 */
data class Course(
    var Week: String,
    var courseList: ArrayList<CourseInfo>
){
    data class CourseInfo(
        var ClassTime: String?,
        var CourseInfoString: String?
    )
//    {
//        override fun toString(): String {
//            return "{\"CourseInfo\":{" +
//                    "\"ClassTime\":\"$ClassTime\"" +
//                    ",\"CourseInfoString\":\"$CourseInfoString\"}}"
//        }
//    }

//    override fun toString(): String {
//        return "{\"Course\":{" +
//                "\"Week\":\"$Week\", " +
//                "\"courseList\":$courseList" +
//                "}}"
//    }

}