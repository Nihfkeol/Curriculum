package com.nihfkeol.curriculum.model

import android.app.Application
import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.nihfkeol.curriculum.R
import java.util.*

class UtilsModel(
    application: Application,
    private val handle: SavedStateHandle
) : AndroidViewModel(application) {

    //cookie的key
    private val _cookie = application.resources.getString(R.string.COOKIE_KEY)

    //是否保存课表信息到本地的key
    private val _isSaveCourseInfoKey = application.resources.getString(R.string.IS_SAVE_COURSE_KEY)

    //屏幕像素宽度的key
    private val _widthPixelsKey = application.resources.getString(R.string.WIDTH_PIXELS_KEY)

    //屏幕像素高度的key
    private val _heightPixelsKey = application.resources.getString(R.string.HEIGHT_PIXELS_KEY)

    //储存开学时间的key
    private val _startDateKey = application.resources.getString(R.string.START_DATE_KEY)

    //shp文件的名字
    private val _shpName = application.resources.getString(R.string.SHP_NAME)
    private val shp = application
        .getSharedPreferences(_shpName, Context.MODE_PRIVATE)

    init {
        if (!handle.contains(_isSaveCourseInfoKey)) {
            handle.set(_isSaveCourseInfoKey, shp.getBoolean(_isSaveCourseInfoKey, false))
        }
        val wm = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        if (!handle.contains(_cookie)) {
            handle.set(_cookie, application.resources.getString(R.string.defStringValue))
        }
        if (!handle.contains(_widthPixelsKey)) {
            handle.set(_widthPixelsKey, (dm.widthPixels / 8) * 6)
        }
        if (!handle.contains(_heightPixelsKey)) {
            handle.set(_heightPixelsKey, (dm.heightPixels / 6) * 5)
        }
        if (!handle.contains(_startDateKey)) {
            val calendar = Calendar.getInstance()
            with(calendar){
                handle.set(
                    _startDateKey,
                    shp.getString(
                        _startDateKey,
                        "${get(Calendar.YEAR)}-${get(Calendar.MONTH)+1}-${get(Calendar.DAY_OF_MONTH)}"
                    )
                )
            }
        }

    }

    fun setCookie(cookie: String) {
        handle.set(_cookie, cookie)
    }

    fun getCookie(): MutableLiveData<String> {
        return handle.getLiveData(_cookie)
    }

    fun setIsSaveCourseInfo(isSave: Boolean) {
        handle.set(_isSaveCourseInfoKey, isSave)
        val edit = shp.edit()
        edit.putBoolean(_isSaveCourseInfoKey, isSave)
        edit.apply()
    }

    fun getIsSaveCourseInfo(): MutableLiveData<Boolean> {
        return handle.getLiveData(_isSaveCourseInfoKey)
    }

    fun getWidthPixels(): MutableLiveData<Int> {
        return handle.getLiveData(_widthPixelsKey)
    }

    fun getHeightPixels(): MutableLiveData<Int> {
        return handle.getLiveData(_heightPixelsKey)
    }

    fun setStartDate(dateStr: String) {
        handle.set(_startDateKey, dateStr)
        val edit = shp.edit()
        edit.putString(_startDateKey, dateStr)
        edit.apply()
    }

    fun getStartDate(): MutableLiveData<String> {
        return handle.getLiveData(_startDateKey)
    }
}