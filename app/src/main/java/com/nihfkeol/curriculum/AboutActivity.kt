package com.nihfkeol.curriculum

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_about.*
import java.lang.Exception

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)


        linearLayoutQQ.setOnClickListener {
            try {
                val url = "//mqqwpa://im/chat?chat_type=wpa&uin=" + resources.getString(R.string.QQ_Number)
                val intent = Intent()
                intent.data = Uri.parse(url)
                startActivity(intent)
            }catch (e : Exception){
                Toast.makeText(this, "请检查是否安装QQ", Toast.LENGTH_SHORT).show()
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
    }
}