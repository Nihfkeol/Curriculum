package com.nihfkeol.curriculum.pojo

/**
 * recycler中item的颜色和字体的颜色
 */
class ItemColor(){
    private var textColor:Int = 0
    private var cardViewColor:Int = 0
    constructor(textColor:Int,cardViewColor:Int):this(){
        this.textColor = textColor
        this.cardViewColor = cardViewColor
    }

    fun getTextColor(): Int {
        return this.textColor
    }

    fun getCardViewColor(): Int {
        return this.cardViewColor
    }
}