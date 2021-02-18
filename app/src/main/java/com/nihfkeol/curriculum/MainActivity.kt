package com.nihfkeol.curriculum

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nihfkeol.curriculum.fragment.ShowCurriculumFragment
import com.nihfkeol.curriculum.fragment.ShowScoreFragment
import com.nihfkeol.curriculum.model.UtilsModel
import com.nihfkeol.curriculum.utils.NetWorkUtils
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private val utilsModel by viewModels<UtilsModel>()
    private val fragmentList = mutableListOf<Fragment>()
    private val myHandler = MyHandler()

    private var showCurriculumFragment: ShowCurriculumFragment? = null
    private var showScoreFragment: ShowScoreFragment? = null
    private val _showCurriculumFragment = "showCurriculumFragment"
    private val _showScoreFragment = "showScoreFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = intent
        val cookieKey = resources.getString(R.string.COOKIE_KEY)
        if (intent.hasExtra(cookieKey)) {
            utilsModel.setCookie(intent.getStringExtra(cookieKey)!!)
        }

        if (savedInstanceState != null) {
            showCurriculumFragment = supportFragmentManager.getFragment(
                savedInstanceState,
                _showCurriculumFragment
            ) as ShowCurriculumFragment
            showScoreFragment = supportFragmentManager.getFragment(
                savedInstanceState,
                _showScoreFragment
            ) as ShowScoreFragment
            addFragmentToList(showCurriculumFragment)
            addFragmentToList(showScoreFragment)
        } else {
            showCurriculumFragment = ShowCurriculumFragment.newInstance()
            addFragment(showCurriculumFragment!!)
            showFragment(showCurriculumFragment!!)
        }


        nav.also {
            it.setCheckedItem(R.id.viewCourseMenu)
            it.itemIconTintList = null
            it.setNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.viewCourseMenu -> {
                        if (showCurriculumFragment == null) {
                            showCurriculumFragment = ShowCurriculumFragment.newInstance()
                        }
                        addFragment(showCurriculumFragment!!)
                        showFragment(showCurriculumFragment!!)
                        supportActionBar!!.title = "课程表"
                    }
                    R.id.viewScoreMenu -> {
                        if (showScoreFragment == null) {
                            showScoreFragment = ShowScoreFragment.newInstance()
                        }
                        addFragment(showScoreFragment!!)
                        showFragment(showScoreFragment!!)
                        supportActionBar!!.title = "成绩"
                    }
                }
                true
            }
        }

        quitButton.setOnClickListener {
            logout()
            val intent2 = Intent(this, LoginActivity::class.java)
            intent2.putExtra(
                resources.getString(R.string.FROM_ACTION),
                true
            )
            startActivity(intent2)

            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (showCurriculumFragment != null) {
            supportFragmentManager.putFragment(
                outState,
                _showCurriculumFragment,
                showCurriculumFragment!!
            )
        }
        if (showScoreFragment != null) {
            supportFragmentManager.putFragment(
                outState,
                _showScoreFragment,
                showScoreFragment!!
            )
        }
        super.onSaveInstanceState(outState)
    }

    private fun addFragmentToList(fragment: Fragment?) {
        if (fragment != null) {
            fragmentList.add(fragment)
        }
    }

    private fun addFragment(fragment: Fragment) {
        if (!fragment.isAdded) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.frameLayout, fragment)
                .commit()
            fragmentList.add(fragment)
        }
    }

    private fun showFragment(fragment: Fragment) {
        for (frag in fragmentList) {
            if (frag != fragment) {
                supportFragmentManager
                    .beginTransaction()
                    .hide(frag)
                    .commit()
            }
        }
        supportFragmentManager.beginTransaction().show(fragment).commit()
    }

    /**
     * 注销cookie
     */
    private fun logout() {

        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val msg = Message()
                msg.what = 1
                myHandler.sendMessage(msg)
            }
        }
        NetWorkUtils(utilsModel.getCookie().value!!).logout(
            callback,
            Date().time
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
             * 1 注销cookie
             */
            when (msg.what) {
                1 -> {
                    Toast.makeText(applicationContext, "注销成功", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}