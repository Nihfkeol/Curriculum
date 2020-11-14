package com.nihfkeol.curriculum.utils

import android.util.Log
import okhttp3.*

class NetWorkUtils {
    private val _loginUrl = "http://210.36.80.160/jsxsd/xk/LoginToXk"
    private val _courseUrl = "http://210.36.80.160/jsxsd/xskb/xskb_list.do"
    private val _logoutUrl = "http://210.36.80.160/jsxsd/xk/LoginToXk?method=exit&tktime="
    private val _userAgent = "User-Agent"
    private val _userAgentValue =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36"
    private val _connection = "Connection"
    private val _connectionValue = "keep-alive"
    private var _client: OkHttpClient

    constructor() {
        _client = OkHttpClient.Builder().build()
    }

    constructor(cookieJar: CookieJar) {
        _client = OkHttpClient.Builder().cookieJar(cookieJar).build()
    }

    /**
     * 判断是否登录成功的网络连接方法
     * @param studentId 学号
     * @param password 密码
     * @param callback 回调方法
     */
    fun isLogin(studentId: String, password: String, callback: Callback) {
        val builder = FormBody.Builder()
        val formBody = builder
            .add("USERNAME", studentId)
            .add("PASSWORD", password)
            .build()
        val request: Request = Request.Builder()
            .url(_loginUrl)
            .post(formBody)
            .addHeader(_connection, _connectionValue)
            .addHeader(_userAgent, _userAgentValue)
            .build()
        _client.newCall(request).enqueue(callback)
    }

    /**
     * 获取课程表网络连接的方法
     * @param cookie cookie值
     * @param callback 回调方法
     */
    fun getCourseHTML(cookie: String, callback: Callback) {
        val request = Request.Builder()
            .url(_courseUrl)
            .addHeader(_connection, _connectionValue)
            .addHeader(_userAgent, _userAgentValue)
            .addHeader("Cookie", cookie)
            .build()
        _client.newCall(request).enqueue(callback)
    }

    fun getCourseHTML(cookie: String, callback: Callback, week: String) {
        val body = FormBody.Builder()
            .add("zc", week)
            .build()
        val request = Request.Builder()
            .url(_courseUrl)
            .addHeader(_connection, _connectionValue)
            .addHeader(_userAgent, _userAgentValue)
            .addHeader("Cookie", cookie)
            .post(body)
            .build()
        _client.newCall(request).enqueue(callback)
    }

    fun logout(time: Long, cookie: String, callback: Callback) {
        val url = _logoutUrl + time
        val request = Request.Builder()
            .url(url)
            .addHeader(_connection, _connectionValue)
            .addHeader("Cookie", cookie)
            .build()
        _client.newCall(request).enqueue(callback)
    }

}