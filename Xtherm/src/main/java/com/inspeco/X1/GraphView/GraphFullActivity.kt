package com.inspeco.X1.GraphView

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.inspeco.X1.R
import com.inspeco.data.*
import com.paramsen.noise.Noise
import kotlinx.android.synthetic.main.activity_graph_full.*
import kotlinx.android.synthetic.main.activity_home.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class GraphFullActivity : AppCompatActivity() {

    val samplingRate = 16000

    private var timerTask: Timer? = null
    val src = FloatArray(4096)
    val dst = FloatArray(4096 + 2)
    val noise = Noise.real().optimized().init(4000, true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph_full)

        captureButton.setOnClickListener {

            screenCapture()

        }

    }


    override fun onResume() {
        super.onResume()
        timerTask = kotlin.concurrent.timer(period = 100) {	// timer() 호출
            if (Fft.fftReady) {
                val fftLen = Fft.fftSourceBuf.position()
                //Log.d("bobopro", "${fftLen} ${Fft.fftSourceBuf[0]} ${Fft.fftSourceBuf[1]} ${Fft.fftSourceBuf[4090]} ${Fft.fftSourceBuf[4091]}" )
                System.arraycopy(
                        Fft.fftSourceBuf.array(),
                        0,
                        src,
                        0,
                        4096
                )
                // Log.d("bobopro", "${fftLen} ${src[0]} ${src[1]} ${src[4090]} ${src[4091]}" )
                val fftResult = noise.fft(src, dst)
                //runOnUiThread {
                    fftBandView.onFFT(fftResult )
                    Fft.fftSourceBuf.clear()
                    Fft.fftReady = false
                //}

            }
        }
    }


    override fun onPause() {
        super.onPause()
        timerTask?.cancel();
    }


    private fun screenCapture() {
        captureButton.visibility = View.GONE
        Handler().postDelayed({

            val p1 = P1.getInstance()
            p1.playShutter(this)

            var fos: FileOutputStream? = null
            var bitmap: Bitmap? = null
            var canvas: Canvas? = null
            val view = graphView
            val w = view!!.width
            val h = view!!.height
            var imgFolder: File? = initFolder(Consts.WAVEFORM_SHOT_FOLDER)
            var captureImageName = imgFolder!!.absolutePath + "/" + Consts.PREFIX_S + getFileName(
                    Consts.WAVEFORM_SHOT_FOLDER, null
            ) + ".png"

            Log.d("bobopro", "src $w x $h")
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap)
            view.draw(canvas)

            fos = FileOutputStream(captureImageName)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)

            if (fos != null) {
                try {
                    fos.close()
                } catch (ioe: IOException) {
                    ioe.printStackTrace()
                }
            }
            bitmap.recycle()


            Handler().postDelayed({
                captureButton.visibility = View.VISIBLE
                Toast.makeText(this, "스크린샷이 저장되었습니다.", Toast.LENGTH_LONG).show()
            }, 250)

            this.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(captureImageName))))
//
        }, 100)

    }


}