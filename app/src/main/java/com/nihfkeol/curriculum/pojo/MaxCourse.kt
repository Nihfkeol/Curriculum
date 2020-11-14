package com.nihfkeol.curriculum.pojo

class MaxCourse() {
    private var maxWeek = 0
    private var setCourse = HashSet<String>()

    constructor(maxWeek:Int,setCourse:HashSet<String>):this(){
        this.maxWeek = maxWeek
        this.setCourse = setCourse
    }

    fun getMaxWeek(): Int {
        return maxWeek
    }

    fun getSetCourse(): HashSet<String> {
        return setCourse
    }
}