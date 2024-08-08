package com.inspeco.data

import android.content.Intent
import com.inspeco.X1.HomeView.P1Model
import java.nio.FloatBuffer

object Fft {
    const val FFT_SAMPLE_SIZE_UDP = 4096
    var fftSourceBuf = FloatBuffer.allocate(FFT_SAMPLE_SIZE_UDP*2)
    var fftReady = false

    fun addBuf() {

        val p1 = P1.getInstance()
        if (fftSourceBuf.position()<FFT_SAMPLE_SIZE_UDP) {
            if (p1.waveSignal.size<2048) {
                for (i in 0 until p1.waveSignal.size) {
                    fftSourceBuf.put(p1.waveSignal[i].toFloat())
                }
            }
        } else {
            fftReady = true
        }


    }
}