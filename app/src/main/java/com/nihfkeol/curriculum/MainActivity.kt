package com.nihfkeol.curriculum

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.nihfkeol.curriculum.databinding.ActivityMainBinding
import com.nihfkeol.curriculum.model.AccountViewModel
import com.nihfkeol.curriculum.utils.NetWorkUtils
import com.nihfkeol.curriculum.utils.ParseUtils
import okhttp3.*
import java.io.IOException
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val myViewModel by viewModels<AccountViewModel>()
    var cookieStore: List<Cookie> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.data = myViewModel
        binding.lifecycleOwner = this

        val intent = intent

        val isCheckSave: Boolean = myViewModel.getIsSave().value!!
        if (isCheckSave) {
            //如果勾选了保存按钮，传值到输入框
            binding.editTextStudentId.setText(myViewModel.getStudentId().value)
            binding.editTextPassword.setText(myViewModel.getPassword().value)
        }

        binding.checkBoxAuto.setOnClickListener {
            val isCheck = binding.checkBoxAuto.isChecked
            myViewModel.setIsAuto(isCheck)
            //当点击自动登录的时候，保存密码也被打勾
            if (isCheck) {
                myViewModel.setIsSave(isCheck)
            }
        }
        binding.checkBoxSave.setOnClickListener {
            val isCheck = binding.checkBoxSave.isChecked
            myViewModel.setIsSave(isCheck)
            //当取消保存密码的时候，保存密码也被取消打勾
            if (!isCheck) {
                myViewModel.setIsAuto(isCheck)
            }
        }

        /**
         * 每次进入这个activity，判断是不是从另一个activity跳转过来的，
         * 如果是就不跳转，不自动登录
         */
        if (!intent.hasExtra(resources.getString(R.string.FROM_ACTION))) {
            val isCheckAuto = myViewModel.getIsAuto().value!!
            //如果自动登录就跳转
            if (isCheckAuto) {
                toLogin()
            }
        }

        //监听输入框输入状态存入数据
        binding.editTextStudentId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                myViewModel.setStudentId(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        binding.editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                myViewModel.setPassword(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        binding.buttonLogin.setOnClickListener {
            toLogin()
        }

        binding.linearLayoutAbout.setOnClickListener {
            val intent2 = Intent()
            intent2.setClass(this,AboutActivity::class.java)
            startActivity(intent2)
        }
    }

    /**
     * 登录
     */
    private fun toLogin() {
        val myHandler = MyHandler()
        //cookie
        val cookieJar: CookieJar = object : CookieJar {
            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore
            }

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore = cookies
            }
        }
        thread {
            val netWorkUtils = NetWorkUtils(cookieJar)
            val callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val message = Message()
                    message.what = 0
                    myHandler.sendMessage(message)
                }

                override fun onResponse(call: Call, response: Response) {
                    val loginHtml = response.body!!.string()
                    val parseUtils = ParseUtils(loginHtml)
                    val isLogin: Boolean = parseUtils.parseIsLogin()
                    val message = Message()
                    message.what = 1
                    message.obj = isLogin
                    myHandler.sendMessage(message)
                }

            }
            netWorkUtils.isLogin(
                myViewModel.getStudentId().value!!,
                myViewModel.getPassword().value!!,
                callback
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (!myViewModel.getIsSave().value!!) {
            myViewModel.clearAccount()
        }
    }


    @SuppressLint("HandlerLeak")
    private inner class MyHandler() : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            /**
             * 1 登录判断
             * 2 网络连接错误，如果保存了课表就读取本地模式
             */
            when (msg.what) {
                1 -> {
                    val isLogin = msg.obj as Boolean
                    if (isLogin) {
                        //保存勾选状态
                        myViewModel.saveCheck()
                        val cookie = cookieStore[0]
                        val intent = Intent()
                        if (myViewModel.getIsSave().value!!) {
                            //如果勾选了保存，那么就保存账户
                            myViewModel.saveAccount()
                        } else {
                            myViewModel.clearAccount()
                        }
                        intent.setClass(applicationContext, ShowCurriculumActivity::class.java)
                        intent.putExtra(
                            resources.getString(R.string.COOKIE_KEY),
                            cookie.toString()
                        )
                        startActivity(intent)
                        finish()
                    } else {
                        myViewModel.clearAccount()
                        Toast.makeText(applicationContext, "帐号或用户名错误", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                2 -> {
                    if (myViewModel.getIsSaveCourseInfo().value!!){
                        Toast.makeText(applicationContext, "网络连接失败，将读取本地储存的课表", Toast.LENGTH_SHORT).show()
                        val intent = Intent()
                        intent.setClass(applicationContext, ShowCurriculumActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(applicationContext, "网络连接失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
