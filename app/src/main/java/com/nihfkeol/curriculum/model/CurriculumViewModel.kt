package com.nihfkeol.curriculum.model

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.nihfkeol.curriculum.R

class CurriculumViewModel(
    application: Application,
    private val handle: SavedStateHandle
) : AndroidViewModel(application) {

    //课程表版本是否显示老师，1：显示，0：不显示
    private val _versionKey = application.resources.getString(R.string.VERSION_KEY)

    //RecyclerView中每个TextView的宽度
    private val _widthKey = "decoration"

    //RecyclerView中每个item的间隔
    private val _marginLength = 10

    //cookie的key
    private val _cookie = application.resources.getString(R.string.COOKIE_KEY)

    //shp文件的名字
    private val _shpName = application.resources.getString(R.string.SHP_NAME)
    private val shp = application
        .getSharedPreferences(_shpName, Context.MODE_PRIVATE)

    private val _isAutoLoginKey = "isAuto"

    //最大周数
    private val _maxWeek = application.resources.getString(R.string.MAX_WEEK_KEY)

    //课程名列表
    private val _countCourse = application.resources.getString(R.string.COUNT_COURSE_KEY)

    //是否保存课表信息到本地的key
    private val _isSaveCourseInfoKey = application.resources.getString(R.string.IS_SAVE_COURSE_KEY)


    init {
        if (!handle.contains(_versionKey)) {
            handle.set(_versionKey, shp.getInt(_versionKey, 1))
        }
        if (!handle.contains(_widthKey)) {
            handle.set(_widthKey, 0)
        }
        if (!handle.contains(_cookie)) {
            handle.set(_cookie, application.resources.getString(R.string.defStringValue))
        }
        if (!handle.contains(_isAutoLoginKey)) {
            handle.set(_isAutoLoginKey, false)
        }
        if (!handle.contains(_maxWeek)) {
            handle.set(_maxWeek, shp.getInt(_maxWeek, 0))
        }
        if (!handle.contains(_countCourse)) {
            handle.set(_countCourse, shp.getStringSet(_countCourse, null))
        }
        if (!handle.contains(_isSaveCourseInfoKey)){
            handle.set(_isSaveCourseInfoKey,shp.getBoolean(_isSaveCourseInfoKey, false))
        }
    }

    fun setVersion(version: Int) {
        handle.set(_versionKey, version)
        val editor = shp.edit()
        editor.putInt(_versionKey, version)
        editor.apply()
    }

    fun getVersion(): MutableLiveData<Int> {
        return handle.getLiveData(_versionKey)
    }

    fun setWidth(width: Int) {
        handle.set(_widthKey, width)
    }

    fun getWidth(): MutableLiveData<Int> {
        return handle.getLiveData(_widthKey)
    }

    fun getMarginLength(): Int {
        return _marginLength
    }

    fun setCookie(cookie: String) {
        handle.set(_cookie, cookie)
    }

    fun getCookie(): MutableLiveData<String> {
        return handle.getLiveData(_cookie)
    }

    fun setIsAutoLogin(isAuto: Boolean) {
        handle.set(_isAutoLoginKey, isAuto)
    }

    fun getIsAutoLogin(): MutableLiveData<Boolean> {
        return handle.getLiveData(_isAutoLoginKey)
    }

    fun setMaxWeek(num: Int) {
        handle.set(_maxWeek, num)
        val edit = shp.edit()
        edit.putInt(_maxWeek, num)
        edit.apply()
    }

    fun getMaxWeek(): MutableLiveData<Int> {
        return handle.getLiveData(_maxWeek)
    }

    fun setCountCourse(courseList: Set<String>) {
        handle.set(_countCourse, courseList)
        val edit = shp.edit()
        edit.putStringSet(_countCourse,courseList)
        edit.apply()
    }

    fun getCountCourse(): MutableLiveData<Set<String>> {
        return handle.getLiveData(_countCourse)
    }

    fun setIsSaveCourseInfo(isSave: Boolean) {
        handle.set(_isSaveCourseInfoKey, isSave)
        val edit = shp.edit()
        edit.putBoolean(_isSaveCourseInfoKey,isSave)
        edit.apply()
    }

    fun getIsSaveCourseInfo(): MutableLiveData<Boolean> {
        return handle.getLiveData(_isSaveCourseInfoKey)
    }
}