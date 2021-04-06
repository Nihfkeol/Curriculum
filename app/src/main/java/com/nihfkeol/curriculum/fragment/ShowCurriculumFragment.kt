package com.nihfkeol.curriculum.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.nihfkeol.curriculum.AboutActivity
import com.nihfkeol.curriculum.LoginActivity
import com.nihfkeol.curriculum.R
import com.nihfkeol.curriculum.adapter.ViewPagerAdapter
import com.nihfkeol.curriculum.model.CurriculumViewModel
import com.nihfkeol.curriculum.model.UtilsModel
import com.nihfkeol.curriculum.ui.MyBaseDialog
import com.nihfkeol.curriculum.utils.FileUtils
import com.nihfkeol.curriculum.utils.NetWorkUtils
import com.nihfkeol.curriculum.utils.ParseUtils
import kotlinx.android.synthetic.main.alertdialog_setting.view.*
import kotlinx.android.synthetic.main.fragment_show_curriculum.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class ShowCurriculumFragment : Fragment() {

    private lateinit var cViewModel: CurriculumViewModel
    private lateinit var utilsModel: UtilsModel

    //viewpager初始页面
    private var currentItem: Int = 0

    //每次启动应用第一次进入界面，默认跳转到当前周的页面
    private var nowWeek: Int = 0

    private val myHandler = MyHandler()
    private lateinit var netWorkUtils: NetWorkUtils

    private var maxWeek: Int = 0

    private val widthPixels: Int by lazy { utilsModel.getWidthPixels().value!! }

    private lateinit var fileName: String
    private lateinit var filePath: File
    private val fileUtils = FileUtils().getInstance()!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        cViewModel = ViewModelProvider(
            requireActivity(),
            SavedStateViewModelFactory(
                requireActivity().application,
                requireActivity()
            )
        ).get(CurriculumViewModel::class.java)
        utilsModel = ViewModelProvider(
            requireActivity(),
            SavedStateViewModelFactory(
                requireActivity().application,
                requireActivity()
            )
        ).get(UtilsModel::class.java)

        //TextView的宽度除以6后再减去间隔
        cViewModel.setWidth(widthPixels / 6 - cViewModel.getMarginLength())
        netWorkUtils = NetWorkUtils(utilsModel.getCookie().value!!)

        utilsModel.getStartDate().observe(requireActivity()) {
            if (it.isNotEmpty()) {
                getNowWeek()
                //如果带cookie是登录过来
                if (utilsModel.getCookie().value!! != "") {
                    maxWeek = cViewModel.getMaxWeek().value!!
                    if (maxWeek == 0) {
                        getMaxWeek()
                    } else {
                        showData()
                    }
                    saveCourse()
                } else {
                    showData()
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
        val firstDate = format.parse(utilsModel.getStartDate().value!!)!!
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
                    msg.also {
                        it.obj = response.body!!.string()
                        it.what = 1
                    }
                    myHandler.sendMessage(msg)
                }
            }
            netWorkUtils.getCourseHTML(callback, "")
        }
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
        val adapter = ViewPagerAdapter(requireActivity(), fragmentList)
        requireView().viewPager.let {
            it.adapter = adapter
            it.setCurrentItem(currentItem, false)
            it.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
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

    }

    /**
     * 保存课表
     */
    private fun saveCourse() {
        val saveCourse = utilsModel.getIsSaveCourseInfo().value!!
        if (saveCourse) {
            requireView().progressBar.let {
                it.progress = 0
                it.visibility = View.VISIBLE
                it.min = nowWeek
                it.max = maxWeek
            }
            for (i in nowWeek..maxWeek) {
                thread {
                    val msg = Message()
                    val callback = object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            msg.what = 2
                            msg.arg1 = i
                            myHandler.sendMessage(msg)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            msg.obj = response.body!!.string()
                            msg.arg1 = i
                            msg.what = 3
                            myHandler.sendMessage(msg)
                        }
                    }
                    netWorkUtils.getCourseHTML(
                        callback,
                        i.toString()
                    )
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_show_curriculum, container, false)
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setHasOptionsMenu(!hidden)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.setting_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settingMenu -> {
                //显示提示框
                alertDialogSetting()
            }
            R.id.locationMenu -> {
                currentItem = nowWeek - 1
                requireView().viewPager.currentItem = currentItem
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("InflateParams")
    private fun alertDialogSetting() {
        val view = layoutInflater.inflate(R.layout.alertdialog_setting, null).let {
            //如果version=1表示显示老师，并且switch为选中状态
            it.switchVersionMenu.isChecked = cViewModel.getVersion().value == 1
            //切换课程表是否显示任课老师
            it.switchVersionMenu.setOnCheckedChangeListener { _, b ->
                //选中为1，否则为0
                if (b) cViewModel.setVersion(1) else cViewModel.setVersion(0)
                showData()
            }
            //保存课程表
            it.switchSaveCourseInfo.isChecked = utilsModel.getIsSaveCourseInfo().value!!
            it.switchSaveCourseInfo.setOnCheckedChangeListener { _, b ->
                utilsModel.setIsSaveCourseInfo(b)
                if (b) {
                    saveCourse()
                }
            }
            //设置开学日期
            it.setStartDateMenu.setOnClickListener {
                val calendar = Calendar.getInstance()
                val dialog = DatePickerDialog(
                    requireContext(),
                    { _, year, month, dayOfMonth ->
                        val dateStr = "$year-${month + 1}-$dayOfMonth"
                        getMaxWeek()
                        utilsModel.setStartDate(dateStr)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                )
                dialog.show()
                Toast.makeText(
                    requireContext(),
                    "请前往南宁师范大学微信公众号，回复“校历表”获取正式上课日期",
                    Toast.LENGTH_LONG
                ).show()
            }
            //更新数据
            it.updateMenu.setOnClickListener {
                if (utilsModel.getIsSaveCourseInfo().value!!) {
                    saveCourse()
                }
                showData()
            }
            //关于页
            it.aboutMenu.setOnClickListener {
                val intent = Intent()
                intent.setClass(requireContext(), AboutActivity::class.java)
                requireActivity().startActivity(intent)
            }
            it
        }

        MyBaseDialog(
            requireContext(),
            R.style.ThemeOverlay_MaterialComponents_Dialog_Alert,
            view
        ).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            show()
            val params = window!!.attributes
            params.width = widthPixels
            window!!.attributes = params
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ShowCurriculumFragment()
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
                    Toast.makeText(requireContext(), " 网络连接失败", Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent()
                    intent.setClass(requireContext(), LoginActivity::class.java)
                    intent.putExtra(
                        requireContext().resources.getString(R.string.FROM_ACTION),
                        true
                    )
                    startActivity(intent)
                    requireActivity().finish()
                }
                1 -> {
                    val parseUtils = ParseUtils(msg.obj.toString())
                    val versionString = parseUtils.parseVersion(cViewModel.getVersion().value!!)
                    val courseList = parseUtils.parseCourse(versionString)
                    val maxCourse = parseUtils.parseMaxCourse(courseList)
                    cViewModel.setMaxWeek(maxCourse.maxWeek)
                    cViewModel.setCountCourse(maxCourse.setCourse)
                    showData()
                }
                2 -> {
                    //最后一次才显示连接失败
                    if (msg.arg1 == maxWeek) {
                        Toast.makeText(
                            requireContext(),
                            "连接更新失败",
                            Toast.LENGTH_SHORT
                        ).show()
                        requireView().progressBar.visibility = View.GONE
                    }
                }
                3 -> {
                    fileName = resources.getString(R.string.FILE_NAME) + "_" + msg.arg1
                    filePath =
                        File(
                            requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                            fileName
                        )
                    fileUtils.writeHtml(msg.obj.toString(), filePath)
                    requireView().progressBar.progress = msg.arg1
                    if (msg.arg1 == maxWeek) {
                        requireView().progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }
}