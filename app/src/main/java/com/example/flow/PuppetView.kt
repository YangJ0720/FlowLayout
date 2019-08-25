package com.example.flow

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * 功能描述
 * @author YangJ
 * @since 2019/8/24
 */
class PuppetView : View {

    private var mL = 0
    private var mT = 0
    private var mR = 0
    private var mB = 0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun onAxisClone(l: Int, t: Int, r: Int, b: Int) {
        this.mL = l
        this.mT = t
        this.mR = r
        this.mB = b
    }

    fun getAxisL(): Int {
        return mL
    }

    fun getAxisT(): Int {
        return mT
    }

    fun getAxisR(): Int {
        return mR
    }

    fun getAxisB(): Int {
        return mB
    }

}