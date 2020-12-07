package com.nihfkeol.curriculum.utils

import okhttp3.*
import java.util.concurrent.TimeUnit

class NetWorkUtils {
    //登录链接
    private val _loginUrl = "http://210.36.80.160/jsxsd/xk/LoginToXk"
    //课表链接
    private val _courseUrl = "http://210.36.80.160/jsxsd/xskb/xskb_list.do"
    //退出登录链接
    private val _logoutUrl = "http://210.36.80.160/jsxsd/xk/LoginToXk?method=exit&tktime="
    //一言api
    private val _hitokotoUrl= "https://v1.hitokoto.cn?charset=utf-8"
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
        _client = OkHttpClient.Builder().connectTimeout(3,TimeUnit.SECONDS).cookieJar(cookieJar).build()
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

    /**
     * 获取每周课程表的连接方法
     */
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

    /**
     * 注销帐号
     */
    fun logout(time: Long, cookie: String, callback: Callback) {
        val url = _logoutUrl + time
        val request = Request.Builder()
            .url(url)
            .addHeader(_connection, _connectionValue)
            .addHeader("Cookie", cookie)
            .build()
        _client.newCall(request).enqueue(callback)
    }

    /**
     * 获取一言的方法
     */
    fun getHitokoto(callback: Callback) {
        val request = Request.Builder()
            .url(_hitokotoUrl)
            .build()
        _client.newCall(request).enqueue(callback)
    }

}