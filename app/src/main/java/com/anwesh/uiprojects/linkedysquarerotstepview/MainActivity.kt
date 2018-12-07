package com.anwesh.uiprojects.linkedysquarerotstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.ysquarerotstepview.YSquareRotStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        YSquareRotStepView.create(this)
    }
}
