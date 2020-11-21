package com.nihfkeol.curriculum.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View

/**
 * 自定义Dialog
 */
class MyBaseDialog : Dialog {
    private var myView: View? = null

    /**
     * 自定义view
     */
    constructor(
        context: Context,
        themeResId: Int,
        view: View
    ) : super(context, themeResId) {
        initView(view,-1)
    }

    /**
     * 默认下载调用
     */
    constructor(
        context: Context,
        themeResId: Int,
        res: Int
    ) : super(context, themeResId) {
        initView(myView,res)
    }

    private fun initView(view: View?, res: Int) {
        if (res != -1) {
            setContentView(res)
        }else{
            setContentView(view!!)
            window?.attributes?.width = 850
        }
        setCanceledOnTouchOutside(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }
}