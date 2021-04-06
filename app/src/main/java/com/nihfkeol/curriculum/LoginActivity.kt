package com.nihfkeol.curriculum

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.allenliu.versionchecklib.core.http.HttpHeaders
import com.allenliu.versionchecklib.v2.AllenVersionChecker
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder
import com.allenliu.versionchecklib.v2.builder.UIData
import com.allenliu.versionchecklib.v2.callback.CustomDownloadFailedListener
import com.allenliu.versionchecklib.v2.callback.CustomVersionDialogListener
import com.allenliu.versionchecklib.v2.callback.RequestVersionListener
import com.nihfkeol.curriculum.databinding.ActivityLoginBinding
import com.nihfkeol.curriculum.model.AccountViewModel
import com.nihfkeol.curriculum.model.UtilsModel
import com.nihfkeol.curriculum.ui.MyBaseDialog
import com.nihfkeol.curriculum.utils.NetWorkUtils
import com.nihfkeol.curriculum.utils.ParseUtils
import com.nihfkeol.curriculum.utils.UserAgent
import com.nihfkeol.curriculum.utils.UserAgentValue
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.dialog_show_updata_version.*
import kotlinx.android.synthetic.main.dialog_show_version_info.view.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    private val myViewModel by viewModels<AccountViewModel>()
    private val utilsModel by viewModels<UtilsModel>()
    var cookieStore: List<Cookie> = mutableListOf()
    private val myHandler = MyHandler()
    private val versionCheckerInstance = AllenVersionChecker.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityLoginBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.also {
            it.data = myViewModel
            it.lifecycleOwner = this

            it.checkBoxAuto.setOnClickListener { _ ->
                val isCheck = it.checkBoxAuto.isChecked
                myViewModel.setIsAuto(isCheck)
                //当点击自动登录的时候，保存密码也被打勾
                if (isCheck) {
                    myViewModel.setIsSave(isCheck)
                }
                //取消保存时，保存按钮状态
                if (!isCheck) {
                    myViewModel.saveCheck()
                }
            }

            it.checkBoxSave.setOnClickListener { _ ->
                val isCheck = it.checkBoxSave.isChecked
                myViewModel.setIsSave(isCheck)
                //当取消保存密码的时候，保存密码也被取消打勾，保存按钮状态
                if (!isCheck) {
                    myViewModel.setIsAuto(isCheck)
                    myViewModel.saveCheck()
                }
            }

            //监听输入框输入状态存入数据
            it.editTextStudentId.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    myViewModel.setStudentId(charSequence.toString())
                }

                override fun afterTextChanged(editable: Editable) {}
            })
            it.editTextPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    myViewModel.setPassword(charSequence.toString())
                }

                override fun afterTextChanged(editable: Editable) {}
            })

            //登录
            it.buttonLogin.setOnClickListener {
                toLogin()
            }

            //关于界面
            it.linearLayoutAbout.setOnClickListener {
                val intent2 = Intent()
                intent2.setClass(this, AboutActivity::class.java)
                startActivity(intent2)
            }
        }


        getHitokoto()

        /**
         * 检查更新
         */
        //获取版本名
        val packageInfo = packageManager.getPackageInfo(applicationContext.packageName, 0)
        val name = packageInfo.versionName
        //设置请求头
        val headers = HttpHeaders()
        headers[UserAgent] = UserAgentValue
        //下载文件路径
        val file = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!
        val downloadBuilder = versionCheckerInstance
            .requestVersion()
            .setRequestUrl("https://gitee.com/nihfkeol/Curriculum/raw/master/versionCheck")
            .setHttpHeaders(headers)
            .request(object : RequestVersionListener {
                override fun onRequestVersionSuccess(
                    downloadBuilder: DownloadBuilder?,
                    result: String?
                ): UIData? {
                    try {
                        val jsonObject = JSONObject(result!!)
                        val newVersionName = jsonObject.getString("versionName")
                        if (newVersionName != name) {
                            val url = jsonObject.getString("URL")
                            val versionInfo = jsonObject.getString("versionInfo")
                            return UIData.create()
                                .setDownloadUrl(url)
                                .setTitle("V$newVersionName")
                                .setContent(versionInfo)
                        }
                    } catch (e: Exception) {
                        e.message
                    }
                    //解析出错或最新版本-去判断是否自动跳转
                    toStartDecide()
                    return null
                }

                override fun onRequestVersionFailure(message: String?) {
                    //连接失败-去判断是否自动跳转
                    toStartDecide()
                }

            })

        downloadBuilder.apply {
            //设置下载路径
            downloadAPKPath = file.toString()
            //下载安装包命名
            apkName = resources.getString(R.string.app_name)
            //版本更新提示框
            customVersionDialogListener =
                CustomVersionDialogListener { context, versionBundle ->
                    val dialog = MyBaseDialog(
                        context,
                        R.style.ThemeOverlay_MaterialComponents_Dialog_Alert,
                        R.layout.dialog_show_updata_version
                    )
                    dialog.tv_title.text = versionBundle.title
                    dialog.tv_msg.text = versionBundle.content
                    dialog.textView_Version_dialog_cancel.setOnClickListener {
                        destory()
                        versionCheckerInstance.cancelAllMission()
                        dialog.dismiss()
                        //取消更新-去判断是否自动跳转
                        toStartDecide()
                    }
                    dialog
                }
            //下载失败提示框
            customDownloadFailedListener =
                CustomDownloadFailedListener { context, _ ->
                    val dialog = MyBaseDialog(
                        context,
                        R.style.ThemeOverlay_MaterialComponents_Dialog_Alert,
                        R.layout.dialog_download_failed
                    )
                    dialog
                }
            executeMission(applicationContext)
        }
    }

    /**
     * 页面自动跳转逻辑
     */
    private fun toStartDecide() {
        //获取是否自动登录
        val isCheckAuto = myViewModel.getIsAuto().value!!
        /**
         * 用Dialog显示版本信息
         */
        if (!myViewModel.getIsNotShowVersionInfo().value!!) {
            var myBaseDialog: MyBaseDialog? = null
            val view = layoutInflater.inflate(R.layout.dialog_show_version_info, null)
            view.also {
                it.textViewVersionInfo.text = resources.getString(R.string.HelpInfo)
                it.checkBoxIsShowVersion.setOnClickListener { _ ->
                    val isCheck = it.checkBoxIsShowVersion.isChecked
                    myViewModel.setIsNotShowVersionInfo(isCheck)
                }
                it.buttonCancel.setOnClickListener {
                    myBaseDialog!!.cancel()
                    //如果自动登录就跳转
                    if (isCheckAuto) {
                        toLogin()
                    }
                }
            }
            myBaseDialog =
                MyBaseDialog(this, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert, view).apply {
                    show()
                    //设置对话框显示大小
                    val params = window!!.attributes
                    params.width = utilsModel.getWidthPixels().value!!
                    window!!.attributes = params
                }

        }else{
            /**
             * 每次进入这个activity，判断是不是从另一个activity跳转过来的，
             * 如果是就不跳转，不自动登录
             */
            val intent = intent
            if (!intent.hasExtra(resources.getString(R.string.FROM_ACTION))) {
                //如果自动登录就跳转
                if (isCheckAuto) {
                    toLogin()
                }
            }
        }



    }

    private fun getHitokoto() {
        thread {
            val netWorkUtils = NetWorkUtils()
            val msg = Message()
            val callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    msg.also {
                        it.what = 2
                        it.obj = response.body!!.string()
                    }
                    myHandler.sendMessage(msg)

                }
            }
            netWorkUtils.getHitokoto(callback)
        }

    }

    /**
     * 登录
     */
    private fun toLogin() {
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
            val message = Message()
            val callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    message.what = 0
                    myHandler.sendMessage(message)
                }

                override fun onResponse(call: Call, response: Response) {
                    val loginHtml = response.body!!.string()
                    val parseUtils = ParseUtils(loginHtml)
                    val isLogin: Boolean = parseUtils.parseIsLogin()
                    message.also {
                        it.what = 1
                        it.obj = isLogin
                    }
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

    override fun onDestroy() {
        super.onDestroy()
        versionCheckerInstance.cancelAllMission()
    }

    @SuppressLint("HandlerLeak")
    private inner class MyHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            /**
             * 0 网络连接错误，如果保存了课表就读取本地模式
             * 1 登录判断
             * 2 获取一言成功
             */
            when (msg.what) {
                0 -> {
                    if (myViewModel.getIsSaveCourseInfo().value!!) {
                        Toast.makeText(applicationContext, "网络连接失败，将读取本地储存的课表", Toast.LENGTH_SHORT)
                            .show()
                        val intent = Intent()
                        intent.setClass(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "网络连接失败", Toast.LENGTH_SHORT).show()
                    }
                }
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
                        intent.setClass(applicationContext, MainActivity::class.java)
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
                    try {
                        val jsonObject = JSONObject(msg.obj.toString())
                        val hitokoto = jsonObject.getString("hitokoto")
                        var hitokotoInfo = "—— "
                        val fromWho = jsonObject.getString("from_who")
                        if ("null" != fromWho) {
                            hitokotoInfo += fromWho
                        }
                        val from = jsonObject.getString("from")
                        if ("null" != from) {
                            hitokotoInfo += "「$from」"
                        }
                        textViewHitokoto.text = hitokoto
                        textViewHitokotoInfo.text = hitokotoInfo
                        linearLayoutHitokoto.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        e.message
                    }

                }
            }
        }
    }
}
