package com.inspeco.wave;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.inspeco.X1.R;

import java.util.LinkedList;
import java.util.Locale;

/**
 * TODO: document your custom view class.
 */
public class WaveformView extends View {
    public static final int MODE_RECORDING = 1;
    public static final int MODE_PLAYBACK = 2;

    private static final int HISTORY_SIZE = 24;
    public float dB = 40.0f;

    private TextPaint mTextPaint, gridFont;
    private Paint mCamStrokePaint, mFillPaint, mMarkerPaint, paintGridLine;
    // private Paint mStrokePaint, mCamStrokePaint, mFillPaint, mMarkerPaint;

    // Used in draw
    private int brightness;
    private Rect drawRect;

    private int width, height;
    private float xStep, centerY;
    private int mMode, mAudioLength, mMarkerPosition, mSampleRate, mChannels;
    private short[] mSamples;
    private LinkedList<float[]> mHistoricalData;
    private Picture mCachedWaveform;
    private Bitmap mCachedWaveformBitmap;
    private int colorDelta = 255 / (HISTORY_SIZE + 1);
    private boolean showTextAxis = true;

    public boolean isUdpMode = true;
    public int viewMode = 0;

    public WaveformView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.WaveformView, defStyle, 0);

        mMode = a.getInt(R.styleable.WaveformView_mode, MODE_PLAYBACK);

        float strokeThickness = a.getFloat(R.styleable.WaveformView_waveformStrokeThickness, 1f);
//        int mStrokeColor = a.getColor(R.styleable.WaveformView_waveformColor1,
//                ContextCompat.getColor(context, R.color.default_waveform));

//        int mCameraStrokeColor = a.getColor(R.styleable.WaveformView_waveformColor1,
//                ContextCompat.getColor(context, R.color.default_camWaveform));

        int mFillColor = a.getColor(R.styleable.WaveformView_waveformFillColor,
                ContextCompat.getColor(context, R.color.default_waveformFill));
        int mMarkerColor = a.getColor(R.styleable.WaveformView_playbackIndicatorColor,
                ContextCompat.getColor(context, R.color.default_playback_indicator));
        int mTextColor = a.getColor(R.styleable.WaveformView_timecodeColor,
                ContextCompat.getColor(context, R.color.default_timecode));

        a.recycle();

//        color = Color.parseColor("#")
//        style = Paint.Style.FILL
//        textSize = 12f.px
//        typeface = Typeface.MONOSPACE


        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
                mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(AudioUtils.getFontSize(getContext(),
                android.R.attr.textAppearanceSmall));


        gridFont= new TextPaint();
        gridFont.setColor( 0xFF888888 );
        gridFont.setTextSize(25.0f);

//        mStrokePaint = new Paint();
//        mStrokePaint.setColor(mStrokeColor);
//        mStrokePaint.setStyle(Paint.Style.STROKE);
//        mStrokePaint.setStrokeWidth(strokeThickness);
//        mStrokePaint.setAntiAlias(true);

        paintGridLine = new Paint();
        paintGridLine.setColor( 0x7A337756 );
        paintGridLine.setStyle(Paint.Style.FILL);

        mCamStrokePaint = new Paint();
        mCamStrokePaint.setColor(0xFF0000aa);
        mCamStrokePaint.setStyle(Paint.Style.STROKE);
        mCamStrokePaint.setStrokeWidth(2);
        mCamStrokePaint.setStrokeWidth(strokeThickness);

        mFillPaint = new Paint();
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setAntiAlias(true);
        mFillPaint.setColor(mFillColor);

        mMarkerPaint = new Paint();
        mMarkerPaint.setStyle(Paint.Style.STROKE);
        mMarkerPaint.setStrokeWidth(0);
        mMarkerPaint.setAntiAlias(true);
        mMarkerPaint.setColor(mMarkerColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = getMeasuredWidth();
        height = getMeasuredHeight();
        xStep = width / (mAudioLength * 1.0f);
        centerY = height / 2f;
        drawRect = new Rect(0, 0, width, height);

        if (mHistoricalData != null) {
            mHistoricalData.clear();
        }
        if (mMode == MODE_PLAYBACK) {
            createPlaybackWaveform();
        }
    }

    private void drawGrid(Canvas canvas) {
//        if (viewMode == Const.VIEW_GRAPH_WAVE_ACTIVITY)
//        {
//            float hDiv = height / 8.0f;
//            canvas.drawLine(0f, hDiv * 1, width, hDiv * 1, paintGridLine);
//            canvas.drawLine(0f, hDiv * 2, width, hDiv * 2, paintGridLine);
//            canvas.drawLine(0f, hDiv * 3, width, hDiv * 3, paintGridLine);
//            canvas.drawLine(0f, hDiv * 4, width, hDiv * 4, paintGridLine);
//            canvas.drawLine(0f, hDiv * 5, width, hDiv * 5, paintGridLine);
//            canvas.drawLine(0f, hDiv * 6, width, hDiv * 6, paintGridLine);
//            canvas.drawLine(0f, hDiv * 7, width, hDiv * 7, paintGridLine);
//
//            canvas.drawText("30dB", 7f, hDiv*1-12.0f, gridFont);
//            canvas.drawText("20dB", 7f, hDiv*2-12.0f, gridFont);
//            canvas.drawText("10dB", 7f, hDiv*3-12.0f, gridFont);
//            canvas.drawText(" 0dB", 7f, hDiv*4-12.0f, gridFont);
//            canvas.drawText("-10dB", 7f, hDiv*5-12.0f, gridFont);
//            canvas.drawText("-20dB", 7f, hDiv*6-12.0f, gridFont);
//            canvas.drawText("-30dB", 7f, hDiv*7-12.0f, gridFont);
//        }

    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        LinkedList<float[]> temp = mHistoricalData;

        if (mMode == MODE_RECORDING && temp != null) {
            if (temp.size()>0) {
                brightness = colorDelta;

//            if ( viewMode == Const.VIEW_CAMERA_ACTIVITY)
//            {
                //mCamStrokePaint.setAlpha(255);

//                canvas.drawLines(temp.getLast(), mCamStrokePaint);

                for (float[] p : temp) {
                    canvas.drawLines(p, mCamStrokePaint);
                }




//            } else {
//                canvas.drawColor(0xff333333);
//
//                drawGrid(canvas);
//                for (float[] p : temp) {
//                    mCamStrokePaint.setAlpha(brightness);
//                    canvas.drawLines(p, mCamStrokePaint);
//                    brightness += colorDelta;
//                }
//
//            }

            }

        } else if (mMode == MODE_PLAYBACK) {
            if (mCachedWaveform != null) {
                canvas.drawPicture(mCachedWaveform);
            } else if (mCachedWaveformBitmap != null) {
                canvas.drawBitmap(mCachedWaveformBitmap, null, drawRect, null);
            }
            if (mMarkerPosition > -1 && mMarkerPosition < mAudioLength)
                canvas.drawLine(xStep * mMarkerPosition, 0, xStep * mMarkerPosition, height, mMarkerPaint);
        }
    }

    public int getMode() {
        return mMode;
    }

    public void setMode(int mMode) {
        mMode = mMode;
    }

    public short[] getSamples() {
        return mSamples;
    }

    public void setSamples(short[] samples) {
        if (samples!=null) {
            mSamples = samples;
            calculateAudioLength();
            onSamplesChanged();
        } else {
            mHistoricalData = null;
            postInvalidate();
        }
    }

    public int getMarkerPosition() {
        return mMarkerPosition;
    }

    public void setMarkerPosition(int markerPosition) {
        mMarkerPosition = markerPosition;
        postInvalidate();
    }

    public int getAudioLength() {
        return mAudioLength;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public void setSampleRate(int sampleRate) {
        mSampleRate = sampleRate;
        calculateAudioLength();
    }

    public int getChannels() {
        return mChannels;
    }

    public void setChannels(int channels) {
        mChannels = channels;
        calculateAudioLength();
    }

    public boolean showTextAxis() {
         return showTextAxis;
    }

    public void setShowTextAxis(boolean showTextAxis) {
        this.showTextAxis = showTextAxis;
    }

    private void calculateAudioLength() {
        if (mSamples == null || mSampleRate == 0 || mChannels == 0)
            return;

        mAudioLength = AudioUtils.calculateAudioLength(mSamples.length, mSampleRate, mChannels);
    }

    private void onSamplesChanged() {
        if (mMode == MODE_RECORDING) {
            //Log.i("bobopro", "Graph MODE_RECORDING");
            // 홈 Wave그래프 플레이 화면, 카메라뷰에서는 나중에...

            if (mHistoricalData == null)
                mHistoricalData = new LinkedList<>();
            LinkedList<float[]> temp = new LinkedList<>(mHistoricalData);

            // For efficiency, we are reusing the array of points.
            float[] waveformPoints;
            if (temp.size() == HISTORY_SIZE) {
                waveformPoints = temp.removeFirst();
            } else {
                waveformPoints = new float[50*4];
            }

            drawRecordingWaveform(mSamples, waveformPoints);
            temp.addLast(waveformPoints);

            //float[] waveformPoints;
            float lWidth = width;
            int iSize = temp.size();
            //Log.i("bobopro",  Integer.toString(iSize));

            boolean isForward = false;

            if (isForward) {
                //순방향
                for (int j = iSize - 1; j >= 0; j--) {
                    //Log.i("bobopro",  Integer.toString(iSize)+", "+ Integer.toString(j));
                    waveformPoints = temp.get(j);
                    lWidth = lWidth - 50f;
                    for (int i = 0; i < 50; i++) {
                        int iPos = i*4;
                        waveformPoints[iPos+0] = lWidth+i;
                        waveformPoints[iPos+2] = lWidth+i;
                    }
                    //canvas.drawLines(waveformPoints, mCamStrokePaint);
                }
            } else {
                //역방향
                int startPos = -1;
                for (int j = iSize - 1; j >= 0; j--) {
                    //Log.i("bobopro",  Integer.toString(iSize)+", "+ Integer.toString(j));
                    waveformPoints = temp.get(j);
                    //lWidth = lWidth - 50f;
                    for (int i = 50-1; i >=0; i--) {
                        int iPos = i*4;
                        waveformPoints[iPos+0] = startPos;
                        startPos++;
                        waveformPoints[iPos+2] = startPos;
                        startPos++;
                    }
                    //canvas.drawLines(waveformPoints, mCamStrokePaint);
                }
            }


            mHistoricalData = temp;
            postInvalidate();


        } else if (mMode == MODE_PLAYBACK) {
            //Log.i("bobopro", "Graph MODE_PLAYBACK");
            mMarkerPosition = -1;
            xStep = width / (mAudioLength * 1.0f);
            createPlaybackWaveform();
        }
    }

    void drawRecordingWaveform(short[] buffer, float[] waveformPoints) {
        float lastX = -1;
        float lastY = -1;
        int pointIndex = 0;
        int bufferLen = buffer.length;
        //float max = Short.MAX_VALUE;
        float max = Short.MAX_VALUE;
        float calcDb = dB + 15.0f;
        float curDbv = calcDb / 40.0f;
        float maxV = 0f;
//

        for (int x = 0; x < bufferLen; x++) {
            // width수 500개 정도...샘플링.
            float sampleV = buffer[x] / max;
            if (sampleV > maxV) {
                maxV = sampleV;
            }
            //short sample = buffer[index];
        }

        float calcR = 1.0f;
//        if (curDbv>0) {
//            if (maxV > curDbv) {
//                calcR = curDbv / maxV;
//            }
//
//        }

        if (curDbv>1.0) curDbv = 1.0f;
        calcR = curDbv;

        //Log.d("bobopro", "waveform "+Integer.toString(width) + ", "+Integer.toString(bufferLen));
        for (int x = 0; x < 50; x++) {
            int index = (int) ((  (float)x / 50) * bufferLen);
            float samplev = buffer[index] / max;
            //short sample = buffer[index];

            float y = centerY - ( (samplev)*calcR * centerY) ;

            if (lastX != -1) {
                waveformPoints[pointIndex++] = lastX;
                waveformPoints[pointIndex++] = lastY;
                waveformPoints[pointIndex++] = x;
                waveformPoints[pointIndex++] = y;
            }

            lastX = x;
            lastY = y;
        }


    }

    Path drawPlaybackWaveform(int width, int height, short[] buffer) {
        Path waveformPath = new Path();
        float centerY = height / 2f;
        float max = Short.MAX_VALUE;

        short[][] extremes = AudioUtils.getExtremes(buffer, width);

        waveformPath.moveTo(0, centerY);

        // draw maximums
        for (int x = 0; x < width; x++) {
            short sample = extremes[x][0];
            float y = centerY - ((sample / max) * centerY);
            waveformPath.lineTo(x, y);
        }

        // draw minimums
        for (int x = width - 1; x >= 0; x--) {
            short sample = extremes[x][1];
            float y = centerY - ((sample / max) * centerY);
            waveformPath.lineTo(x, y);
        }

        waveformPath.close();

        return waveformPath;
    }

    private void createPlaybackWaveform() {
        if (width <= 0 || height <= 0 || mSamples == null)
            return;

        Canvas cacheCanvas;
        if (Build.VERSION.SDK_INT >= 23 && isHardwareAccelerated()) {
            mCachedWaveform = new Picture();
            cacheCanvas = mCachedWaveform.beginRecording(width, height);
        } else {
            mCachedWaveformBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            cacheCanvas = new Canvas(mCachedWaveformBitmap);
        }
///
        Path mWaveform = drawPlaybackWaveform(width, height, mSamples);
        //cacheCanvas.drawPath(mWaveform, mFillPaint);
        cacheCanvas.drawPath(mWaveform, mCamStrokePaint);
        drawAxis(cacheCanvas, width);

        if (mCachedWaveform != null)
            mCachedWaveform.endRecording();
    }

    private void drawAxis(Canvas canvas, int width) {
        if (!showTextAxis) return;
        int seconds = mAudioLength / 1000;
        float xStep = width / (mAudioLength / 1000f);
        float textHeight = mTextPaint.getTextSize();
        float textWidth = mTextPaint.measureText("10.00");
        int secondStep = (int)(textWidth * seconds * 2) / width;
        secondStep = Math.max(secondStep, 1);
        for (float i = 0; i <= seconds; i += secondStep) {
            canvas.drawText(String.format(Locale.getDefault(), "%.1f", i), i * xStep, textHeight, mTextPaint);
        }
    }
}
