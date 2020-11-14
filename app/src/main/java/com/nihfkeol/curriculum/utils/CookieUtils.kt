package com.nihfkeol.curriculum.utils

import android.content.Context
import com.nihfkeol.curriculum.R
import okhttp3.Cookie

class CookieUtils {



    /**
     * 保存cookie到shp文件
     * @param context 上下文
     * @param cookie cookie
     */
    fun saveCookieToSHP(context: Context, cookie: Cookie) {
        val sp = context.getSharedPreferences(
            context.resources.getString(R.string.COOKIE_SHP_NAME),
            Context.MODE_PRIVATE
        )
        val editor = sp.edit()
        val split = cookie.toString().split(";")
        editor.putString(context.resources.getString(R.string.COOKIE_KEY), split[0])
        editor.apply()
    }

    fun saveCookieToSHP(context: Context, cookie: String) {
        val sp = context.getSharedPreferences(
            context.resources.getString(R.string.COOKIE_SHP_NAME),
            Context.MODE_PRIVATE
        )
        val editor = sp.edit()
        editor.putString(context.resources.getString(R.string.COOKIE_KEY), cookie)
        editor.apply()
    }

    /**
     * 从shp文件中获取cookie
     * @param context 上下文
     * @return 返回cookie值
     */
    fun getCookieToSHP(context: Context): String? {
        val sp = context.getSharedPreferences(
            context.resources.getString(R.string.COOKIE_SHP_NAME),
            Context.MODE_PRIVATE
        )
        val defValue = context.resources.getString(R.string.defStringValue)
        return sp.getString(context.resources.getString(R.string.COOKIE_KEY), defValue)
    }

}