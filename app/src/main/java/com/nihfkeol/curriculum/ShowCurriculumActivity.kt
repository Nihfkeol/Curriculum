package com.nihfkeol.curriculum

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.nihfkeol.curriculum.adapter.ViewPagerAdapter
import com.nihfkeol.curriculum.fragment.CourseDetailFragment
import com.nihfkeol.curriculum.model.CurriculumViewModel
import com.nihfkeol.curriculum.ui.MyBaseDialog
import com.nihfkeol.curriculum.utils.FileUtils
import com.nihfkeol.curriculum.utils.NetWorkUtils
import com.nihfkeol.curriculum.utils.ParseUtils
import kotlinx.android.synthetic.main.activity_show_curriculum.*
import kotlinx.android.synthetic.main.alertdialog_setting.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class ShowCurriculumActivity : AppCompatActivity() {

    private lateinit var shp: SharedPreferences

    private val cViewModel by viewModels<CurriculumViewModel>()

    private var courseHTML: String? = ""

    //viewpager初始页面
    private var currentItem: Int = 0

    //每次启动应用第一次进入界面，默认跳转到当前周的页面
    private var nowWeek: Int = 0

    private val myHandler = MyHandler()
    private val netWorkUtils = NetWorkUtils()

    private var maxWeek: Int = 0

    private lateinit var fileName: String
    private lateinit var filePath: File
    private val fileUtils = FileUtils().getInstance()!!
    private val TAG = "ShowCurriculumActivityLog："

    /**
     * 定义数据
     */
    private fun initData() {
        //获取屏幕宽度，好设置RecyclerView中每个TextView的宽度
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        //TextView的宽度除以6后再减去间隔
        cViewModel.setWidth(dm.widthPixels / 6 - cViewModel.getMarginLength())

        shp = getSharedPreferences(
            resources.getString(R.string.SHP_NAME),
            Context.MODE_PRIVATE
        )

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_curriculum)
        Log.w(TAG, "onCreate")
        initData()
        val intent = intent
        val cookieKey = resources.getString(R.string.COOKIE_KEY)

        getNowWeek()

        //如果带cookie是登录过来
        if (intent.hasExtra(cookieKey)) {
            cViewModel.setCookie(intent.getStringExtra(cookieKey)!!)

            maxWeek = cViewModel.getMaxWeek().value!!
            if (maxWeek == 0) {
                getMaxWeek()
            } else {
                showData()
            }
            saveCourse()
        }


    }

    /**
     * 保存课表
     */
    private fun saveCourse() {
        val saveCourse = cViewModel.getIsSaveCourseInfo().value!!
        if (saveCourse) {
            progressBar.progress = 0
            progressBar.visibility = View.VISIBLE
            progressBar.min = nowWeek
            progressBar.max = maxWeek
            for (i in nowWeek..maxWeek) {
                Log.w(TAG, "saveCourse-$i")
                thread {
                    val msg = Message()
                    val callback = object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            msg.what = 2
                            msg.arg1 = i
                            myHandler.sendMessage(msg)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            Log.w(TAG, "onResponse-$i")
                            msg.obj = response.body!!.string()
                            msg.arg1 = i
                            msg.what = 3
                            myHandler.sendMessage(msg)
                        }
                    }
                    netWorkUtils.getCourseHTML(
                        cViewModel.getCookie().value!!,
                        callback,
                        i.toString()
                    )
                }
            }
        }
    }


    /**
     * 获取现在是第几周
     */
    private fun getNowWeek() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        var nowDate = Date()
        nowDate = format.parse(format.format(nowDate))!!
        //TODO 自动获取开学的时间
        val firstDate = format.parse("2020-9-14")!!
        val nd = 1000 * 24 * 60 * 60.toLong()
        val diff = nowDate.time - firstDate.time
        nowWeek = (diff / nd / 7).toInt() + 1
        currentItem = nowWeek - 1
    }

    /**
     * 获取第几周没课
     */
    private fun getMaxWeek() {
        thread {
            val msg = Message()
            val callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    msg.what = 0
                    myHandler.sendMessage(msg)
                }

                override fun onResponse(call: Call, response: Response) {
                    courseHTML = response.body!!.string()
                    msg.what = 1
                    myHandler.sendMessage(msg)
                }
            }
            netWorkUtils.getCourseHTML(cViewModel.getCookie().value!!, callback)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.setting_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settingMenu -> {
                //显示提示框
                alertDialogSetting()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("InflateParams")
    private fun alertDialogSetting() {
        val view = layoutInflater.inflate(R.layout.alertdialog_setting, null)
        //如果version=1表示显示老师，并且switch为选中状态
        view.switchVersionMenu.isChecked = cViewModel.getVersion().value == 1
        //切换课程表是否显示任课老师
        view.switchVersionMenu.setOnCheckedChangeListener { _, b ->
            //选中为1，否则为0
            if (b) cViewModel.setVersion(1) else cViewModel.setVersion(0)
            showData()
        }
        //保存课程表
        view.switchSaveCourseInfo.isChecked = cViewModel.getIsSaveCourseInfo().value!!
        view.switchSaveCourseInfo.setOnCheckedChangeListener { _, b ->
            cViewModel.setIsSaveCourseInfo(b)
            if (b) {
                saveCourse()
            }
        }
        //更新数据
        view.upDataMenu.setOnClickListener {
            if (cViewModel.getIsSaveCourseInfo().value!!) {
                saveCourse()
            }
            showData()

        }
        //注销帐号
        view.quitMenu.setOnClickListener {
            logout()
            val intent = Intent()
            intent.setClass(this, MainActivity::class.java)
            intent.putExtra(resources.getString(R.string.FROM_ACTION), true)
            startActivity(intent)
            finish()
        }
        //关于页
        view.aboutMenu.setOnClickListener {
            val intent = Intent()
            intent.setClass(this, AboutActivity::class.java)
            startActivity(intent)
        }
        val myBaseDialog = MyBaseDialog(this, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert,view)
        myBaseDialog.setCancelable(true)
        myBaseDialog.setCanceledOnTouchOutside(true)
        myBaseDialog.show()
    }

    /**
     * 展示数据
     */
    private fun showData() {
        val maxWeek = cViewModel.getMaxWeek().value!!
        val fragmentList: MutableList<Fragment> = mutableListOf()
        fragmentList.clear()
        for (i in 1..maxWeek) {
            fragmentList.add(CourseDetailFragment.newInstance(i.toString()))
        }
        val adapter = ViewPagerAdapter(this, fragmentList)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(currentItem, false)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                currentItem = position
            }
        })


    }

    /**
     * 注销cookie
     */
    private fun logout() {
        val msg = Message()
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                msg.what = 4
                myHandler.sendMessage(msg)
            }
        }
        netWorkUtils.logout(
            Date().time,
            cViewModel.getCookie().value!!,
            callback
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        logout()
    }

    @SuppressLint("HandlerLeak")
    private inner class MyHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            /**
             * 0 获取周数失败
             * 1 解析最大周数
             * 2 下载课程表失败（服务器炸了）
             * 3 储存课表
             * 4 注销cookie
             */
            when (msg.what) {
                0 -> {
                    Toast.makeText(this@ShowCurriculumActivity, " 网络连接失败", Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent()
                    intent.setClass(this@ShowCurriculumActivity, MainActivity::class.java)
                    intent.putExtra(
                        this@ShowCurriculumActivity.resources.getString(R.string.FROM_ACTION),
                        true
                    )
                    startActivity(intent)
                    this@ShowCurriculumActivity.finish()
                }
                1 -> {
                    val parseUtils = ParseUtils(courseHTML!!)
                    val versionString = parseUtils.parseVersion(cViewModel.getVersion().value!!)
                    val courseList = parseUtils.parseCourse(versionString)
                    val maxCourse = parseUtils.parseMaxCourse(courseList)
                    cViewModel.setMaxWeek(maxCourse.getMaxWeek())
                    cViewModel.setCountCourse(maxCourse.getSetCourse())
                    showData()
                }
                2 -> {
                    //最后一次才显示连接失败
                    if (msg.arg1 == maxWeek) {
                        Toast.makeText(
                            this@ShowCurriculumActivity,
                            "连接更新失败",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressBar.visibility = View.GONE
                    }
                }
                3 -> {
                    Log.w(TAG, "what=3-${msg.arg1}")
                    fileName = resources.getString(R.string.FILE_NAME) + "_" + msg.arg1
                    filePath =
                        File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
                    fileUtils.writeHtml(msg.obj.toString(), filePath)
                    progressBar.progress = msg.arg1
                    if (msg.arg1 == maxWeek) {
                        progressBar.visibility = View.GONE
                    }
                }
                4 -> {
                    Toast.makeText(applicationContext, "注销成功", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}