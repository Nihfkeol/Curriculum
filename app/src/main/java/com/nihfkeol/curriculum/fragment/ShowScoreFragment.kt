package com.nihfkeol.curriculum.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.nihfkeol.curriculum.R
import com.nihfkeol.curriculum.adapter.TranscriptListViewAdapter
import com.nihfkeol.curriculum.model.UtilsModel
import com.nihfkeol.curriculum.utils.NetWorkUtils
import com.nihfkeol.curriculum.utils.ParseUtils
import kotlinx.android.synthetic.main.fragment_show_score.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ShowScoreFragment : Fragment() {

    private lateinit var netWorkUtils: NetWorkUtils

    private lateinit var utilsModel: UtilsModel

    private val myHandle = MyHandle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        utilsModel = ViewModelProvider(
            requireActivity(),
            SavedStateViewModelFactory(
                requireActivity().application,
                requireActivity()
            )
        ).get(UtilsModel::class.java)

        netWorkUtils = NetWorkUtils(utilsModel.getCookie().value!!)

        if (utilsModel.getCookie().value!! != "") {
            getSchoolYear()
        } else {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_show_score, container, false)
    }

    private fun getSchoolYear() {
        val msg = Message()
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                msg.what = 0
                myHandle.sendMessage(msg)
            }

            override fun onResponse(call: Call, response: Response) {
                msg.also {
                    it.obj = response.body!!.string()
                    it.what = 1
                }
                myHandle.sendMessage(msg)
            }

        }
        netWorkUtils.getSchoolYearList(callback)
    }

    fun getTranscript(year: String) {
        val msg = Message()
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                msg.what = 0
                myHandle.sendMessage(msg)
            }

            override fun onResponse(call: Call, response: Response) {
                msg.also {
                    it.obj = response.body!!.string()
                    it.what = 2
                }
                myHandle.sendMessage(msg)
            }
        }
        netWorkUtils.getTranscript(callback, year)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setHasOptionsMenu(!hidden)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.setGroupVisible(R.menu.setting_menu, false)
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            ShowScoreFragment()
    }

    @SuppressLint("HandlerLeak")
    private inner class MyHandle : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            when (msg.what) {
                0 -> {
                    Toast.makeText(requireContext(), "网络连接错误，获取课程表失败", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    val parseUtils = ParseUtils(msg.obj.toString())
                    val parseSchoolYearList = parseUtils.parseSchoolYear()
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        parseSchoolYearList
                    ).also {
                        it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    requireView().schoolYearSpinner.also {
                        it.adapter = adapter
                        it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                getTranscript(parseSchoolYearList[position])
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }
                        }
                    }
                }
                2 -> {
                    val parseUtils = ParseUtils(msg.obj.toString())
                    val parseScoreList = parseUtils.parseTranscript()
                    val adapter = TranscriptListViewAdapter(requireContext(),parseScoreList)
                    requireView().transcriptListView.adapter = adapter
                }
            }
        }
    }
}