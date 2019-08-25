package com.example.flow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author YangJ
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
        initView()
    }

    private fun initData() {

    }

    private fun initView() {
        val country = resources.getStringArray(R.array.country)
        val size = country.size
        for (i in 0 until size) {
            flowLayout.addView(createView(country[i]))
        }
    }

    private fun createView(text: String): View {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.item, null)
        view.setPadding(50, 10, 50, 10)
        view.background = DrawableUtils.createDrawable()
        if (view is TextView) {
            view.text = text
        }
        return view
    }

}
