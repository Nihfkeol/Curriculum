package com.nihfkeol.curriculum.utils

import okhttp3.*
import java.util.concurrent.TimeUnit

const val UserAgent = "User-Agent"
const val UserAgentValue =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36"
const val Connection = "Connection"
const val ConnectionValue = "keep-alive"
class NetWorkUtils {
    //登录链接
    private val _loginUrl = "http://210.36.80.160/jsxsd/xk/LoginToXk"

    //课表链接
    private val _courseUrl = "http://210.36.80.160/jsxsd/xskb/xskb_list.do"

    //退出登录链接
    private val _logoutUrl = "http://210.36.80.160/jsxsd/xk/LoginToXk?method=exit&tktime="

    //一言api
    private val _hitokotoUrl = "https://v1.hitokoto.cn?charset=utf-8"

    //成绩学年列表
    private val _schoolYearUrl =
        "http://210.36.80.160/jsxsd/kscj/cjcx_query?Ves632DSdyV=NEW_XSD_XJCJ"

    //成绩列表
    private val _scoreUrl = "http://210.36.80.160/jsxsd/kscj/cjcx_list"

    private var _client: OkHttpClient
    private lateinit var requestBuilder: Request.Builder
    private val _cookie = "Cookie"

    //获取课表/成绩使用
    constructor(cookie: String) {
        _client = OkHttpClient.Builder().build()
        requestBuilder = Request.Builder()
            .addHeader(Connection, ConnectionValue)
            .addHeader(UserAgent, UserAgentValue)
            .addHeader(_cookie, cookie)
    }

    //一言使用
    constructor() {
        _client = OkHttpClient.Builder().build()
    }

    //登录使用
    constructor(cookieJar: CookieJar) {
        _client =
            OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).cookieJar(cookieJar).build()
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
            .addHeader(Connection, ConnectionValue)
            .addHeader(UserAgent, UserAgentValue)
            .build()
        _client.newCall(request).enqueue(callback)
    }

    /**
     * 获取每周课程表的连接方法
     */
    fun getCourseHTML(callback: Callback, week: String) {
        val body = FormBody.Builder()
            .add("zc", week)
            .build()
        val request = requestBuilder
            .url(_courseUrl)
            .post(body)
            .build()
        _client.newCall(request).enqueue(callback)
    }

    /**
     * 注销帐号
     */
    fun logout(callback: Callback, time: Long) {
        val url = _logoutUrl + time
        val request = requestBuilder
            .url(url)
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

    /**
     * 获取学年列表
     */
    fun getSchoolYearList(callback: Callback) {
        val request = requestBuilder
            .url(_schoolYearUrl)
            .build()
        _client.newCall(request).enqueue(callback)
    }

    /**
     * 获取成绩单
     */
    fun getTranscript(callback: Callback, year: String) {
        val body = FormBody.Builder()
            .add("kksj", year)
            .add("xsfs", "all")
            .build()
        val request = requestBuilder
            .url(_scoreUrl)
            .post(body)
            .build()
        _client.newCall(request).enqueue(callback)
    }
}