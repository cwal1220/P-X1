package com.inspeco.X1.GraphView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import com.inspeco.extensions.graphTextPaint2

import java.lang.System.arraycopy
import kotlin.math.pow

interface FFTView {
    fun onFFT(fft: FloatArray)
}
/**
 * FFT Band View
 */


private const val TAG = "CircleImageView"


class FFTBandView : androidx.appcompat.widget.AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var bands = 550
    var average = .0f

    var waveLen = 0
    val fft: FloatArray = FloatArray(16000)
    val gp1 = FloatArray(5120)
    val paintBandsFill: Paint = Paint()
    val paintGridLine: Paint = Paint()
    val paintText2: Paint = graphTextPaint2()

    var valueMax1 = 200f
    var testValueMax1 = 0f

//    var width1 = 0
//    var height1 = 0
    //val bg = Color.parseColor("#0039424F")

    init {
        paintBandsFill.color = 0xdd17bbaa.toInt()
        paintBandsFill.style = Paint.Style.FILL

//        paintBands.color = Color.parseColor("#AAFF2C00")
//        paintBands.strokeWidth = 1f
//        paintBands.style = Paint.Style.STROKE

        paintGridLine.color = 0x7A337756
        paintGridLine.strokeWidth = 1f
        paintGridLine.style = Paint.Style.STROKE
    }


    override fun onDraw(canvas: Canvas?) {



        val hDiv = height / 4.0f
        val wDiv = width / 8.0f
        canvas!!.drawColor(0xff333333.toInt())



        //if (States.viewMode == Const.VIEW_GRAPH_FFT_ACTIVITY) {
        canvas.drawLine(0f, hDiv*3, width.toFloat(), hDiv*3, paintGridLine)
        canvas.drawLine(0f, hDiv*2, width.toFloat(), hDiv*2, paintGridLine)
        canvas.drawLine(0f, hDiv*1, width.toFloat(), hDiv*1, paintGridLine)

        canvas.drawLine(wDiv*1, 0f, wDiv*1, height.toFloat(),  paintGridLine)
        canvas.drawLine(wDiv*2, 0f, wDiv*2, height.toFloat(),  paintGridLine)
        canvas.drawLine(wDiv*3, 0f, wDiv*3, height.toFloat(),  paintGridLine)
        canvas.drawLine(wDiv*4, 0f, wDiv*4, height.toFloat(),  paintGridLine)
        canvas.drawLine(wDiv*5, 0f, wDiv*5, height.toFloat(),  paintGridLine)
        canvas.drawLine(wDiv*6, 0f, wDiv*6, height.toFloat(),  paintGridLine)
        canvas.drawLine(wDiv*7, 0f, wDiv*7, height.toFloat(),  paintGridLine)

        canvas.drawText("30dB", 7f, hDiv*1-12.0f, paintText2)
        canvas.drawText("20dB", 7f, hDiv*2-12.0f, paintText2)
        canvas.drawText("10dB", 7f, hDiv*3-12.0f, paintText2)
        canvas.drawText(" 0dB", 7f, hDiv*4-12.0f, paintText2)

        canvas.drawText("1KHz", wDiv*1+2f, hDiv*4-12.0f, paintText2)
        canvas.drawText("2KHz", wDiv*2+2f, hDiv*4-12.0f, paintText2)
        canvas.drawText("3KHz", wDiv*3+2f, hDiv*4-12.0f, paintText2)
        canvas.drawText("4KHz", wDiv*4+2f, hDiv*4-12.0f, paintText2)
        canvas.drawText("5KHz", wDiv*5+2f, hDiv*4-12.0f, paintText2)
        canvas.drawText("6KHz", wDiv*6+2f, hDiv*4-12.0f, paintText2)
        canvas.drawText("7KHz", wDiv*7+2f, hDiv*4-12.0f, paintText2)
        // }

        //Log.d("bobopro", "w$bands fftb ${gp1[5]} ${gp1[6]} ${gp1[7]} ${gp1[8]}" )

        var oldY = gp1[1]
        for (i in 2 until bands-4) {

            var left1 = (i).toFloat()
            var top1 = height - (height * (gp1[i] / valueMax1).toFloat()) - height * .02f
            //if (top1<(height-3)) {
            //    canvas.drawRect(left1, top1, left1+1.0f, top1+1, paintBandsFill)
            //}
            if (i>2) {
                canvas.drawLine(i - 1.0f, oldY, i.toFloat(), top1, paintBandsFill)
            }
            oldY = top1
        }


        super.onDraw(canvas)
    }

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
//    {
////        width1 = widthMeasureSpec
////        height1 = heightMeasureSpec
////
////
//    }



    fun onFFT(fft: FloatArray) {
        synchronized(this.fft) {
            //Log.d("bobopro","$width,  $height")
            //Noise noise = Noise.real(4000) // input size == 4096
//            arraycopy(fft, 2, this.fft, 0, fft.size - 2)
//            drawSurface(this::drawAudio)
            waveLen = fft.size-2

            arraycopy(fft, 2, this.fft, 0, waveLen - 2)
            //Log.d("bobopro", "ffta ${this.fft[5]} ${this.fft[6]} ${this.fft[7]} ${this.fft[8]}" )
            //Log.d("bobopro","fft len $waveLen")
            //drawSurface(this::drawAudio)

            bands = width-4
            var bandSize = waveLen / bands

            //Log.d("bobopro","fft len $waveLen  band $bands")

            val i2 = Math.round(waveLen / bands / 4.0f)
            // valueMax1  = valueMax1.toDouble().pow(2.0).toFloat()
            // canvas.drawColor(bg)
            for (i in 2 until bands-2) {
                var accum = .0f
                val idx = i * bandSize
                accum = (fft[idx].toDouble().pow(2.0) + fft[idx+2].toDouble().pow(2.0)).toFloat()
                accum += (fft[idx + 2].toDouble().pow(2.0) + fft[idx+3].toDouble().pow(2.0)).toFloat()
                accum += (fft[idx + 4].toDouble().pow(2.0) + fft[idx+6].toDouble().pow(2.0)).toFloat()
                accum /= 3
                val accum2 = Math.sqrt(accum.toDouble()) / 10000f
                gp1[i] = accum2.toFloat()
            }
            invalidate()
            //Log.d("bobopro", "w$bands ffta ${gp1[5]} ${gp1[6]} ${gp1[7]} ${gp1[8]}" )
        }
    }
}


//
//class FFTBandView1(context: Context, attrs: AttributeSet?) : FFTView {
//
//    var bands = 550
//    //val maxConst = 1750000000000//reference max value for accum magnitude
//    var average = .0f
//
//    var waveLen = 0
//    val fft: FloatArray = FloatArray(16000)
//    val paintBandsFill: Paint = Paint()
////    val paintBands: Paint = Paint()
//    val paintGridLine: Paint = Paint()
//    //val paintText: Paint = graphTextPaint()
//    val paintText2: Paint = graphTextPaint2()
//    //val noise = Noise.real()
//
//    //val bg = Color.parseColor("#0039424F")
//
//    init {
//        paintBandsFill.color = 0xdd17bbaa.toInt()
//        paintBandsFill.style = Paint.Style.FILL
//
////        paintBands.color = Color.parseColor("#AAFF2C00")
////        paintBands.strokeWidth = 1f
////        paintBands.style = Paint.Style.STROKE
//
//        paintGridLine.color = 0x7A337756
//        paintGridLine.strokeWidth = 1f
//        paintGridLine.style = Paint.Style.STROKE
//    }
//
//    fun drawAudio(canvas: Canvas): Canvas {
//        canvas.drawColor(0xff333333.toInt())
//        bands = width-4
//        var bandSize = waveLen / bands
//        var valueMax1 = 200f
//        var testValueMax1 = 0f
//
//
//        //Log.d("bobopro","fft len $waveLen  band $bands")
//
//        val i2 = Math.round(waveLen / bands / 4.0f)
//        val gp1 = FloatArray(bands)
//        // valueMax1  = valueMax1.toDouble().pow(2.0).toFloat()
//        // canvas.drawColor(bg)
//        for (i in 2 until bands-2) {
//            var accum = .0f
//
//            val idx = i * bandSize
//
////                accum = (fft[idx].toDouble().pow(2.0) + fft[idx+2].toDouble().pow(2.0)).toFloat()
////                accum += (fft[idx + 2].toDouble().pow(2.0) + fft[idx+3].toDouble().pow(2.0)).toFloat()
////                accum += (fft[idx + 4].toDouble().pow(2.0) + fft[idx+6].toDouble().pow(2.0)).toFloat()
////                accum /= 3
////                val accum2 = Math.sqrt(accum.toDouble()) / 10000f
////                gp1[i] = accum2.toFloat()
////                if (testValueMax1<accum2) {
////                    testValueMax1 = accum2.toFloat()
////                }
//            //synchronized(fft) {
//                accum = (fft[idx].toDouble().pow(2.0) + fft[idx+2].toDouble().pow(2.0)).toFloat()
//                accum += (fft[idx + 2].toDouble().pow(2.0) + fft[idx+3].toDouble().pow(2.0)).toFloat()
//                accum += (fft[idx + 4].toDouble().pow(2.0) + fft[idx+6].toDouble().pow(2.0)).toFloat()
//                accum /= 3
//                val accum2 = Math.sqrt(accum.toDouble()) / 10000f
//                gp1[i] = accum2.toFloat()
////                if (valueMax1<accum2) {
////                    valueMax1 = accum2.toFloat()
////                }
//            //}
//        }
//
//
//
//        //Log.d("bobopro", "bands ${gp1[5]} ${gp1[6]} ${gp1[7]} ${gp1[8]}" )
//
//        //Log.d("bobopro","FFT Max : $valueMax1, $waveLen, $bands, $i2")
//
//        var oldY = gp1[1]
//
//        val hDiv = height / 4.0f
//        val wDiv = width / 8.0f
//
//
//        //if (States.viewMode == Const.VIEW_GRAPH_FFT_ACTIVITY) {
//            canvas.drawLine(0f, hDiv*3, width.toFloat(), hDiv*3, paintGridLine)
//            canvas.drawLine(0f, hDiv*2, width.toFloat(), hDiv*2, paintGridLine)
//            canvas.drawLine(0f, hDiv*1, width.toFloat(), hDiv*1, paintGridLine)
//
//            canvas.drawLine(wDiv*1, 0f, wDiv*1, height.toFloat(),  paintGridLine)
//            canvas.drawLine(wDiv*2, 0f, wDiv*2, height.toFloat(),  paintGridLine)
//            canvas.drawLine(wDiv*3, 0f, wDiv*3, height.toFloat(),  paintGridLine)
//            canvas.drawLine(wDiv*4, 0f, wDiv*4, height.toFloat(),  paintGridLine)
//            canvas.drawLine(wDiv*5, 0f, wDiv*5, height.toFloat(),  paintGridLine)
//            canvas.drawLine(wDiv*6, 0f, wDiv*6, height.toFloat(),  paintGridLine)
//            canvas.drawLine(wDiv*7, 0f, wDiv*7, height.toFloat(),  paintGridLine)
//
//            canvas.drawText("30dB", 7f, hDiv*1-12.0f, paintText2)
//            canvas.drawText("20dB", 7f, hDiv*2-12.0f, paintText2)
//            canvas.drawText("10dB", 7f, hDiv*3-12.0f, paintText2)
//            canvas.drawText(" 0dB", 7f, hDiv*4-12.0f, paintText2)
//
//            canvas.drawText("1KHz", wDiv*1+2f, hDiv*4-12.0f, paintText2)
//            canvas.drawText("2KHz", wDiv*2+2f, hDiv*4-12.0f, paintText2)
//            canvas.drawText("3KHz", wDiv*3+2f, hDiv*4-12.0f, paintText2)
//            canvas.drawText("4KHz", wDiv*4+2f, hDiv*4-12.0f, paintText2)
//            canvas.drawText("5KHz", wDiv*5+2f, hDiv*4-12.0f, paintText2)
//            canvas.drawText("6KHz", wDiv*6+2f, hDiv*4-12.0f, paintText2)
//            canvas.drawText("7KHz", wDiv*7+2f, hDiv*4-12.0f, paintText2)
//        // }
//
//
//
//        for (i in 2 until bands-4) {
//
//            var left1 = (i).toFloat()
//            var top1 = height - (height * (gp1[i] / valueMax1).toFloat()) - height * .02f
//            //if (top1<(height-3)) {
//            //    canvas.drawRect(left1, top1, left1+1.0f, top1+1, paintBandsFill)
//            //}
//            if (i>2) {
//                canvas.drawLine(i - 1.0f, oldY, i.toFloat(), top1, paintBandsFill)
//            }
//            oldY = top1
//        }
//
//
//
//        return canvas
//    }
//
//    override fun onFFT(fft: FloatArray) {
//        synchronized(this.fft) {
//
//            //Noise noise = Noise.real(4000) // input size == 4096
////            arraycopy(fft, 2, this.fft, 0, fft.size - 2)
////            drawSurface(this::drawAudio)
//            waveLen = fft.size-2
//
//            arraycopy(fft, 2, this.fft, 0, waveLen - 2)
//            //Log.d("bobopro", "ffta ${this.fft[5]} ${this.fft[6]} ${this.fft[7]} ${this.fft[8]}" )
//            //Log.d("bobopro","fft len $waveLen")
//            drawSurface(this::drawAudio)
//        }
//    }
//}
//
//



//
//
//class FFTBandView(context: Context, attrs: AttributeSet?) : SimpleSurface(context, attrs), FFTView {
//
//    var bands = 550
//    //val maxConst = 1750000000000//reference max value for accum magnitude
//    var average = .0f
//
//    var waveLen = 0
//    val fft: FloatArray = FloatArray(16000)
//    val paintBandsFill: Paint = Paint()
//    //    val paintBands: Paint = Paint()
//    val paintGridLine: Paint = Paint()
//    //val paintText: Paint = graphTextPaint()
//    val paintText2: Paint = graphTextPaint2()
//    //val noise = Noise.real()
//
//    //val bg = Color.parseColor("#0039424F")
//
//    init {
//        paintBandsFill.color = 0xdd17bbaa.toInt()
//        paintBandsFill.style = Paint.Style.FILL
//
////        paintBands.color = Color.parseColor("#AAFF2C00")
////        paintBands.strokeWidth = 1f
////        paintBands.style = Paint.Style.STROKE
//
//        paintGridLine.color = 0x7A337756
//        paintGridLine.strokeWidth = 1f
//        paintGridLine.style = Paint.Style.STROKE
//    }
//
//    fun drawAudio(canvas: Canvas): Canvas {
//        canvas.drawColor(0xff333333.toInt())
//        bands = width-4
//        var bandSize = waveLen / bands
//        var valueMax1 = 200f
//        var testValueMax1 = 0f
//
//
//        //Log.d("bobopro","fft len $waveLen  band $bands")
//
//        val i2 = Math.round(waveLen / bands / 4.0f)
//        val gp1 = FloatArray(bands)
//        // valueMax1  = valueMax1.toDouble().pow(2.0).toFloat()
//        // canvas.drawColor(bg)
//        for (i in 2 until bands-2) {
//            var accum = .0f
//
//            val idx = i * bandSize
//
////                accum = (fft[idx].toDouble().pow(2.0) + fft[idx+2].toDouble().pow(2.0)).toFloat()
////                accum += (fft[idx + 2].toDouble().pow(2.0) + fft[idx+3].toDouble().pow(2.0)).toFloat()
////                accum += (fft[idx + 4].toDouble().pow(2.0) + fft[idx+6].toDouble().pow(2.0)).toFloat()
////                accum /= 3
////                val accum2 = Math.sqrt(accum.toDouble()) / 10000f
////                gp1[i] = accum2.toFloat()
////                if (testValueMax1<accum2) {
////                    testValueMax1 = accum2.toFloat()
////                }
//            //synchronized(fft) {
//            accum = (fft[idx].toDouble().pow(2.0) + fft[idx+2].toDouble().pow(2.0)).toFloat()
//            accum += (fft[idx + 2].toDouble().pow(2.0) + fft[idx+3].toDouble().pow(2.0)).toFloat()
//            accum += (fft[idx + 4].toDouble().pow(2.0) + fft[idx+6].toDouble().pow(2.0)).toFloat()
//            accum /= 3
//            val accum2 = Math.sqrt(accum.toDouble()) / 10000f
//            gp1[i] = accum2.toFloat()
////                if (valueMax1<accum2) {
////                    valueMax1 = accum2.toFloat()
////                }
//            //}
//        }
//
//
//
//        //Log.d("bobopro", "bands ${gp1[5]} ${gp1[6]} ${gp1[7]} ${gp1[8]}" )
//
//        //Log.d("bobopro","FFT Max : $valueMax1, $waveLen, $bands, $i2")
//
//        var oldY = gp1[1]
//
//        val hDiv = height / 4.0f
//        val wDiv = width / 8.0f
//
//
//        //if (States.viewMode == Const.VIEW_GRAPH_FFT_ACTIVITY) {
//        canvas.drawLine(0f, hDiv*3, width.toFloat(), hDiv*3, paintGridLine)
//        canvas.drawLine(0f, hDiv*2, width.toFloat(), hDiv*2, paintGridLine)
//        canvas.drawLine(0f, hDiv*1, width.toFloat(), hDiv*1, paintGridLine)
//
//        canvas.drawLine(wDiv*1, 0f, wDiv*1, height.toFloat(),  paintGridLine)
//        canvas.drawLine(wDiv*2, 0f, wDiv*2, height.toFloat(),  paintGridLine)
//        canvas.drawLine(wDiv*3, 0f, wDiv*3, height.toFloat(),  paintGridLine)
//        canvas.drawLine(wDiv*4, 0f, wDiv*4, height.toFloat(),  paintGridLine)
//        canvas.drawLine(wDiv*5, 0f, wDiv*5, height.toFloat(),  paintGridLine)
//        canvas.drawLine(wDiv*6, 0f, wDiv*6, height.toFloat(),  paintGridLine)
//        canvas.drawLine(wDiv*7, 0f, wDiv*7, height.toFloat(),  paintGridLine)
//
//        canvas.drawText("30dB", 7f, hDiv*1-12.0f, paintText2)
//        canvas.drawText("20dB", 7f, hDiv*2-12.0f, paintText2)
//        canvas.drawText("10dB", 7f, hDiv*3-12.0f, paintText2)
//        canvas.drawText(" 0dB", 7f, hDiv*4-12.0f, paintText2)
//
//        canvas.drawText("1KHz", wDiv*1+2f, hDiv*4-12.0f, paintText2)
//        canvas.drawText("2KHz", wDiv*2+2f, hDiv*4-12.0f, paintText2)
//        canvas.drawText("3KHz", wDiv*3+2f, hDiv*4-12.0f, paintText2)
//        canvas.drawText("4KHz", wDiv*4+2f, hDiv*4-12.0f, paintText2)
//        canvas.drawText("5KHz", wDiv*5+2f, hDiv*4-12.0f, paintText2)
//        canvas.drawText("6KHz", wDiv*6+2f, hDiv*4-12.0f, paintText2)
//        canvas.drawText("7KHz", wDiv*7+2f, hDiv*4-12.0f, paintText2)
//        // }
//
//
//
//        for (i in 2 until bands-4) {
//
//            var left1 = (i).toFloat()
//            var top1 = height - (height * (gp1[i] / valueMax1).toFloat()) - height * .02f
//            //if (top1<(height-3)) {
//            //    canvas.drawRect(left1, top1, left1+1.0f, top1+1, paintBandsFill)
//            //}
//            if (i>2) {
//                canvas.drawLine(i - 1.0f, oldY, i.toFloat(), top1, paintBandsFill)
//            }
//            oldY = top1
//        }
//
//
//
//        return canvas
//    }
//
//    override fun onFFT(fft: FloatArray) {
//        synchronized(this.fft) {
//
//            //Noise noise = Noise.real(4000) // input size == 4096
////            arraycopy(fft, 2, this.fft, 0, fft.size - 2)
////            drawSurface(this::drawAudio)
//            waveLen = fft.size-2
//
//            arraycopy(fft, 2, this.fft, 0, waveLen - 2)
//            //Log.d("bobopro", "ffta ${this.fft[5]} ${this.fft[6]} ${this.fft[7]} ${this.fft[8]}" )
//            //Log.d("bobopro","fft len $waveLen")
//            drawSurface(this::drawAudio)
//        }
//    }
//}