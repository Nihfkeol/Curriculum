package com.nihfkeol.curriculum.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.nihfkeol.curriculum.LoginActivity
import com.nihfkeol.curriculum.R
import com.nihfkeol.curriculum.adapter.CourseRecyclerViewAdapter
import com.nihfkeol.curriculum.model.CurriculumViewModel
import com.nihfkeol.curriculum.model.UtilsModel
import com.nihfkeol.curriculum.pojo.Course
import com.nihfkeol.curriculum.utils.FileUtils
import com.nihfkeol.curriculum.utils.NetWorkUtils
import com.nihfkeol.curriculum.utils.ParseUtils
import kotlinx.android.synthetic.main.fragment_course_detail.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread


private const val ARG_WEEK = "weekKey"

class CourseDetailFragment : Fragment() {
    private var week: String? = null

    private lateinit var viewModel: CurriculumViewModel
    private lateinit var utilsModel:UtilsModel

    private lateinit var parseUtils: ParseUtils
    private lateinit var courseList: List<Course>

    //recycler相关属性
    private lateinit var gManager: GridLayoutManager
    private lateinit var decoration: CourseRecyclerViewAdapter.SpacesItemDecoration
    private lateinit var adapter: CourseRecyclerViewAdapter

    //文件操作
    private lateinit var fileName: String
    private lateinit var filePath: File
    private val fileUtils = FileUtils().getInstance()!!
    private var courseHTML: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            week = it.getString(ARG_WEEK)
        }
        viewModel = ViewModelProvider(
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

        //禁止滑动，设置49格
        gManager = object : GridLayoutManager(context, 49, HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean {
                return false
            }
        }
        //如果显示信息就只占一个，其他占8格
        gManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (position % 7 == 0)
                    return 1
                return 8
            }
        }
        //间距
        decoration = CourseRecyclerViewAdapter.SpacesItemDecoration(viewModel.getMarginLength())
        fileName = requireActivity().resources.getString(R.string.FILE_NAME) + "_" + week
        filePath =
            File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        dataTools()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_course_detail, container, false)
        view.showCourseRecyclerView.removeItemDecoration(decoration)
        view.showCourseRecyclerView.layoutManager = gManager
        view.showCourseRecyclerView.addItemDecoration(decoration)
        return view
    }

    /**
     * 数据类
     * 如果是勾选了保存文件就读取本地的
     * 否则从网络获取
     */
    private fun dataTools() {
        if (utilsModel.getIsSaveCourseInfo().value!!) {
            if (filePath.exists()) {
                courseHTML = fileUtils.readHtml(filePath)
                showData()
            } else {
                getDataFromNet()
            }
        } else {
            getDataFromNet()
        }
    }

    /**
     * 展示数据
     */
    private fun showData() {
        parseUtils = ParseUtils(courseHTML!!)
        val versionStr = parseUtils.parseVersion(viewModel.getVersion().value!!)
        courseList = parseUtils.parseCourse(versionStr)

        if (isAdded){
            adapter =
                CourseRecyclerViewAdapter(
                    requireActivity(),
                    courseList,
                    viewModel.getWidth().value!!,
                    utilsModel.getWidthPixels().value!!,
                    week!!,
                    viewModel.getCountCourse().value!!
                )
            requireView().showCourseRecyclerView.adapter = adapter
        }
    }

    /**
     * 从网络获取课程表
     */
    private fun getDataFromNet() {
        val myHandle = MyHandle()
        thread {
            val netWorkUtils = NetWorkUtils(utilsModel.getCookie().value!!)
            val msg = Message()
            val callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    msg.what = -1
                    myHandle.sendMessage(msg)
                }

                override fun onResponse(call: Call, response: Response) {
                    courseHTML = response.body!!.string()
                    msg.what = 1
                    myHandle.sendMessage(msg)
                }
            }
            netWorkUtils.getCourseHTML(callback, week!!)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(week: String) =
            CourseDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_WEEK, week)
                }
            }
    }

    @SuppressLint("HandlerLeak")
    private inner class MyHandle : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            /**
             * 1:登录成功
             * -1:登录失败
             */
            when (msg.what) {
                1 -> {
                    /**
                     * 如果勾选了，并且文件不存在，就保存文件
                     */
                    if (utilsModel.getIsSaveCourseInfo().value!!) {
                        if (!filePath.exists()) {
                            fileUtils.writeHtml(courseHTML!!, filePath)
                        }
                    }
                    showData()
                }
                -1 -> {
                    Toast.makeText(requireActivity(), " 网络连接失败", Toast.LENGTH_SHORT).show()
                    val intent = Intent()
                    intent.setClass(requireActivity(), LoginActivity::class.java)
                    intent.putExtra(
                        requireActivity().resources.getString(R.string.FROM_ACTION),
                        true
                    )
                    startActivity(intent)
                    requireActivity().finish()

                }
            }
        }
    }
}