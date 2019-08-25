package com.example.flow

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import java.lang.ref.WeakReference
import kotlin.math.max

/**
 * 功能描述
 * @author YangJ
 * @since 2019/8/24
 */
class FlowLayout : ViewGroup {

    // 手指按下的坐标
    private var mDownX = 0f
    private var mDownY = 0f
    // 长按拖拽傀儡view到任意目标childView的下标
    private var mPosition = AbsListView.INVALID_POSITION
    // 是否长按任意childView
    private var mIsHolder = false
    //
    private var mTouchFrame: Rect? = null
    // 手指长按时拖拽的childView
    private var mHoldView: View? = null
    // 傀儡view
    private var mPuppetView: PuppetView? = null
    // 手势控制器
    private lateinit var mGestureDetector: GestureDetector

    companion object {
        private const val HANDLER_WHAT = 0
        private const val HANDLER_DELAYED = 100L
        private lateinit var mHandler: MainHandler
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize()
    }

    private fun initialize() {
        mHandler = MainHandler(this)
        mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onLongPress(e: MotionEvent) {
                if (mIsHolder) return
                // 查找长按选中的childView
                getViewByLongPress(e)
                //
                mIsHolder = true
            }

        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        // 通过模拟摆放childView来测量childView大小
        val childMeasure = getMeasureChild(widthMeasureSpec, heightMeasureSpec)
        // 根据FlowLayout宽高布局属性决定控件大小
        val measuredWidth = getMeasureWidth(widthMode, widthSize, childMeasure[0])
        val measuredHeight = getMeasureHeight(heightMode, heightSize, childMeasure[1])
        // 设置控件大小
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var targetWidth = 0
        var targetHeight = 0
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            if (childView is PuppetView) {
                continue
            }
            val measureWidth = childView.measuredWidth
            val measureHeight = childView.measuredHeight
            // 判断当前剩余宽度是否足以放下这个childView
            if (targetWidth + measureWidth > measuredWidth) { // 如果放不下
                // 换行摆放该childView
                targetWidth = 0
                targetHeight += measureHeight
            }
            // 摆放childView
            childView.layout(targetWidth, targetHeight, targetWidth + measureWidth, targetHeight + measureHeight)
            // 摆放childView之后需要对已使用的宽度进行累加
            targetWidth += measureWidth
        }
    }

    /**
     * 通过模拟摆放childView来测量需要的大小
     */
    private fun getMeasureChild(widthMeasureSpec: Int, heightMeasureSpec: Int): IntArray {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        // 已使用的宽度
        var usedWidth = 0
        //
        var measuredWidth = 0
        var measuredHeight = 0
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            if (childView is PuppetView) {
                continue
            }
            val params = childView.layoutParams
            val widthPadding = childView.paddingLeft + childView.paddingRight
            val heightPadding = childView.paddingTop + childView.paddingBottom
            childView.measure(
                getChildMeasureSpec(widthMeasureSpec, widthPadding, params.width),
                getChildMeasureSpec(heightMeasureSpec, heightPadding, params.height)
            )
            //
            if (i == 0) {
                usedWidth = childView.measuredWidth
                measuredHeight = childView.measuredHeight
                continue
            } else {
                // 如果已使用宽度 + childView宽度不超过屏幕宽度
                if (usedWidth + childView.measuredWidth <= widthSize) {
                    usedWidth += childView.measuredWidth
                } else { // 否则已使用宽度 + childView宽度超过了屏幕宽度
                    usedWidth = childView.measuredWidth
                    if (measuredWidth == 0) {
                        measuredWidth = widthSize
                    }
                    //
                    if (measuredHeight + childView.measuredHeight <= heightSize) {
                        // 需要执行换行，并对高度进行叠加
                        measuredHeight += childView.measuredHeight
                    } else {
                        measuredHeight = heightSize
                    }
                }
            }
        }
        return intArrayOf(max(measuredWidth, usedWidth), measuredHeight)
    }

    private fun getMeasureWidth(widthMode: Int, widthSize: Int, childWidth: Int): Int {
        return when (widthMode) {
            MeasureSpec.EXACTLY -> {
                widthSize
            }
            MeasureSpec.AT_MOST -> {
                childWidth
            }
            else -> {
                widthSize
            }
        }
    }

    private fun getMeasureHeight(heightMode: Int, heightSize: Int, childHeight: Int): Int {
        return when (heightMode) {
            MeasureSpec.EXACTLY -> {
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                childHeight
            }
            else -> {
                heightSize
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mGestureDetector.onTouchEvent(event)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = event.x
                mDownY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (mIsHolder) {
                    mPuppetView?.let { view ->
                        //
                        val position = pointToPosition(event.x.toInt(), event.y.toInt())
                        println("position = $position")
                        if (AbsListView.INVALID_POSITION != position && mPosition != position) {
                            if (mHandler.hasMessages(HANDLER_WHAT)) {
                                mHandler.removeMessages(HANDLER_WHAT)
                            }
                            //
                            val msg = Message.obtain()
                            msg.what = HANDLER_WHAT
                            msg.arg1 = position
                            mHandler.sendMessageDelayed(msg, HANDLER_DELAYED)
                        }
                        // 控制傀儡view跟随手指移动
                        movePuppetView(view, event.x, event.y)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mPuppetView != null) {
                    removeView(mPuppetView)
                    mPuppetView = null
                }
                requestLayout()
                mHoldView?.visibility = View.VISIBLE
                mIsHolder = false
            }
        }
        return true
    }

    private fun swapChildView(position: Int) {
        removeView(mHoldView)
        addView(mHoldView, position)
        mPosition = position
        println("mPosition = $mPosition")
    }

    /**
     * 控制傀儡view跟随手指移动
     */
    private fun movePuppetView(view: PuppetView, x: Float, y: Float) {
        // 获取拖动距离
        val distanceX = x - mDownX
        val distanceY = y - mDownY
        // 让傀儡view跟随手指移动
        view.layout(
            (view.getAxisL() + distanceX).toInt(), (view.getAxisT() + distanceY).toInt(),
            (view.getAxisR() + distanceX).toInt(), (view.getAxisB() + +distanceY).toInt()
        )
    }

    /**
     * 获取长按选中的childView
     */
    private fun getViewByLongPress(e: MotionEvent) {
        // 查找长按选中的childView
        val size = childCount
        for (i in 0 until size) {
            val childView = getChildAt(i)
            val location = IntArray(2)
            childView.getLocationOnScreen(location)
            val x = location[0]
            val y = location[1]
            if (e.rawX < x || e.rawX > (x + childView.width)
                || e.rawY < y || e.rawY > (y + childView.height)
            ) {
                continue
            }
            // 隐藏长按选中的childView并在手指抬起的时候重置为显示状态
            childView.visibility = View.INVISIBLE
            // 获取选中的childView坐标轴属性
            val l = childView.x.toInt()
            val t = childView.y.toInt()
            val r = l + childView.width
            val b = t + childView.height
            mHoldView = childView
            // 生成一个傀儡view用于响应用户手指拖拽
            val puppetView = PuppetView(context)
            puppetView.onAxisClone(l, t, r, b)
            puppetView.background = DrawableUtils.createDrawable(Color.BLACK)
            addView(puppetView, childView.layoutParams)
            puppetView.layout(l, t, r, b)
            mPuppetView = puppetView
            break
        }
    }

    /**
     * 该方法从AbsListView拷贝，这里做了稍微修改
     */
    private fun pointToPosition(x: Int, y: Int): Int {
        var frame: Rect? = mTouchFrame
        if (frame == null) {
            mTouchFrame = Rect()
            frame = mTouchFrame
        }
        // 根据手指触摸点的坐标遍历childView
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            // 这里需要注意不要判断傀儡view
            if (child is PuppetView) {
                continue
            }
            if (child.visibility == View.VISIBLE) {
                child.getHitRect(frame)
                if (frame!!.contains(x, y)) {
                    return i
                }
            }
        }
        return AdapterView.INVALID_POSITION
    }

    private class MainHandler(layout: FlowLayout) : Handler() {
        private var mReference: WeakReference<FlowLayout> = WeakReference(layout)

        override fun handleMessage(msg: Message?) {
            if (HANDLER_WHAT == msg?.what) {
                val layout = mReference.get() ?: return
                layout.swapChildView(msg.arg1)
            }
        }
    }
}