package com.nihfkeol.curriculum

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_about.*
import java.lang.Exception

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        textViewShowVersion.text = packageInfo.versionName

        linearLayoutHelpInfo.setOnClickListener {
            AlertDialog.Builder(this).also {
                it.setMessage(resources.getString(R.string.HelpInfo2))
                it.setCancelable(true)
                it.create()
            }.show()
        }

        linearLayoutQQ.setOnClickListener {
            try {
                val url = "mqqwpa://im/chat?chat_type=wpa&uin=" + resources.getString(R.string.QQ_Number)
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(url)
                startActivity(intent)
            }catch (e : Exception){
                Toast.makeText(this, "请检查是否安装最新版QQ", Toast.LENGTH_SHORT).show()
            }
        }

        linearLayoutGitHub.setOnClickListener {
            val url = "https://github.com/Nihfkeol/Curriculum"
            val intent = Intent()
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        linearLayoutOkHttp.setOnClickListener {
            val url = "https://github.com/square/okhttp"
            val intent = Intent()
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        linearLayoutJsoup.setOnClickListener {
            val url = "https://github.com/jhy/jsoup/"
            val intent = Intent()
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        linearLayoutCheckVersionLib.setOnClickListener {
            val url = "https://github.com/AlexLiuSheng/CheckVersionLib"
            val intent = Intent()
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }
}