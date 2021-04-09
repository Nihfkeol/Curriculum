package com.nihfkeol.curriculum.model

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.nihfkeol.curriculum.R

class AccountViewModel(application: Application, private val handle: SavedStateHandle) :
    AndroidViewModel(application) {

    //储存在SharedPreferences文件中获取学号的key
    private val _studentIDKey = application.getString(R.string.STUDENT_ID)

    //储存在SharedPreferences文件中获取密码的key
    private val _passwordKey = application.getString(R.string.PASSWORD)

    //储存在SharedPreferences文件中获取点击状态的key
    private val _isAutoKey = application.getString(R.string.IS_AUTO)
    private val _isSaveKey = application.getString(R.string.IS_SAVE)

    //SharedPreferences的文件名
    private val _shpName = application.getString(R.string.SHP_NAME)
    private val shp = application.getSharedPreferences(_shpName, Context.MODE_PRIVATE)

    private val _isSaveCourseInfoKey = application.resources.getString(R.string.IS_SAVE_COURSE_KEY)

    private val _isNotShowHelpInfoKey =
        application.resources.getString(R.string.IS_NOT_SHOW_HELP_INFO_KEY)

    init {
        val defString = application.getString(R.string.defStringValue)
        //判断是否有学号的key,没有就加载
        if (!handle.contains(_studentIDKey)) {
            handle.set(_studentIDKey, shp.getString(_studentIDKey, defString))
        }
        if (!handle.contains(_passwordKey)) {
            handle.set(_passwordKey, shp.getString(_passwordKey, defString))
        }

        val defBool = application.resources.getBoolean(R.bool.defBool)
        if (!handle.contains(_isAutoKey)) {
            handle.set(_isAutoKey, shp.getBoolean(_isAutoKey, defBool))
        }
        if (!handle.contains(_isSaveKey)) {
            handle.set(_isSaveKey, shp.getBoolean(_isSaveKey, defBool))
        }
        if (!handle.contains(_isSaveCourseInfoKey)) {
            handle.set(_isSaveCourseInfoKey, shp.getBoolean(_isSaveCourseInfoKey, defBool))
        }
        if (!handle.contains(_isNotShowHelpInfoKey)) {
            handle.set(_isNotShowHelpInfoKey, shp.getBoolean(_isNotShowHelpInfoKey, defBool))
        }
    }

    fun saveCheck() {
        val editor = shp.edit()
        editor.putBoolean(_isAutoKey, getIsAuto().value!!)
        editor.putBoolean(_isSaveKey, getIsSave().value!!)
        editor.apply()
    }

    fun saveAccount() {
        val editor = shp.edit()
        editor.putString(_studentIDKey, getStudentId().value!!)
        editor.putString(_passwordKey, getPassword().value!!)
        editor.apply()
    }

    fun clearAccount() {
        val edit = shp.edit()
        edit.putString(_studentIDKey, "")
        edit.putString(_passwordKey, "")
        edit.apply()
    }

    fun getStudentId(): MutableLiveData<String> {
        return handle.getLiveData(_studentIDKey)
    }

    fun getPassword(): MutableLiveData<String> {
        return handle.getLiveData(_passwordKey)
    }

    fun setStudentId(studentId: String?) {
        handle.set(_studentIDKey, studentId)
    }

    fun setPassword(password: String?) {
        handle.set(_passwordKey, password)
    }

    fun getIsAuto(): MutableLiveData<Boolean> {
        return handle.getLiveData(_isAutoKey)
    }

    fun getIsSave(): MutableLiveData<Boolean> {
        return handle.getLiveData(_isSaveKey)
    }

    fun setIsAuto(isAuto: Boolean) {
        handle.set(_isAutoKey, isAuto)
    }

    fun setIsSave(isSave: Boolean) {
        handle.set(_isSaveKey, isSave)
    }

    fun getIsSaveCourseInfo(): MutableLiveData<Boolean> {
        return handle.getLiveData(_isSaveCourseInfoKey)
    }

    fun setIsNotShowHelpInfo(isShow: Boolean) {
        handle.set(_isNotShowHelpInfoKey, isShow)
        val edit = shp.edit()
        edit.putBoolean(_isNotShowHelpInfoKey, isShow)
        edit.apply()
    }

    fun getIsNotShowHelpInfo(): MutableLiveData<Boolean> {
        return handle.getLiveData(_isNotShowHelpInfoKey)
    }

}