package com.inspeco.X1.CamView

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.inspeco.X1.R
import com.inspeco.data.States

class LoadingWebActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_loading_web)
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({
            var intent = Intent(States.mainContext, WebCamActivity::class.java)
            startActivity(intent)
            finish()
        }, 1250)

    }

}