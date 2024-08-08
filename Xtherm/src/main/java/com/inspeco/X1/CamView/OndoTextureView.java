package com.inspeco.X1.CamView;

/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.inspeco.data.Cfg;
import com.inspeco.data.Consts;
import com.inspeco.data.X1;
import com.serenegiant.MyApp;
import com.serenegiant.encoder.IVideoEncoder;
import com.serenegiant.encoder.MediaEncoder;
import com.serenegiant.encoder.MediaVideoEncoder;
import com.serenegiant.glutils.EGLBase;
import com.serenegiant.usb.ITemperatureCallback;
import com.serenegiant.utils.FpsCounter;

import java.text.DecimalFormat;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.serenegiant.glutils.ShaderConst.GL_TEXTURE_2D;
import static com.serenegiant.glutils.ShaderConst.GL_TEXTURE_EXTERNAL_OES;

import com.inspeco.data.P1;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.GLDrawer2D1;
import com.serenegiant.widget.GLHelper1;
import com.serenegiant.widget.TouchPoint;
import com.serenegiant.widget.UVCCameraTextureView;

//import com.serenegiant.glutils.GLDrawer2D;


public class OndoTextureView extends UVCCameraTextureView
        implements TextureView.SurfaceTextureListener, CameraViewInterface {

    private static final boolean DEBUG = true;    // TODO set false on release
    private static final String TAG = "bobopro OndoTextureView";

    private boolean mHasSurface;
    private RenderHandler mRenderHandler;
    private final Object mCaptureSync = new Object();
    private Bitmap mTempBitmap;
    private boolean mReqesutCaptureStillImage;
    private Callback mCallback;
    /**
     * for calculation of frame rate
     */
    private final FpsCounter mFpsCounter = new FpsCounter();

    public OndoTextureView(final Context context) {
        this(context, null, 0);
    }

    public OndoTextureView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OndoTextureView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setSurfaceTextureListener(this);
    }

    @Override
    public void onResume() {
        if (DEBUG) Log.v(TAG, "onResume:");
        if (mHasSurface) {
            mRenderHandler = RenderHandler.createHandler(mFpsCounter, super.getSurfaceTexture(), getWidth(), getHeight());
        }
    }

    @Override
    public void onPause() {
        if (DEBUG) Log.v(TAG, "onPause:");
        if (mRenderHandler != null) {
            mRenderHandler.release();
            mRenderHandler = null;
        }
        if (mTempBitmap != null) {
            mTempBitmap.recycle();
            mTempBitmap = null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
        if (DEBUG) Log.v(TAG, "onSurfaceTextureAvailable:" + surface);
        if (mRenderHandler == null) {
            mRenderHandler = RenderHandler.createHandler(mFpsCounter, surface, width, height);
        } else {
            mRenderHandler.resize(width, height);
        }
        mHasSurface = true;
        if (mCallback != null) {
            mCallback.onSurfaceCreated(this, getSurface());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
        if (DEBUG) Log.v(TAG, "onSurfaceTextureSizeChanged:" + surface);
        if (mRenderHandler != null) {
            mRenderHandler.resize(width, height);
        }
        if (mCallback != null) {
            mCallback.onSurfaceChanged(this, getSurface(), width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
        if (DEBUG) Log.v(TAG, "onSurfaceTextureDestroyed:" + surface);
        if (mRenderHandler != null) {
            mRenderHandler.release();
            mRenderHandler = null;
        }
        mHasSurface = false;
        if (mCallback != null) {
            mCallback.onSurfaceDestroy(this, getSurface());
        }
        if (mPreviewSurface != null) {
            mPreviewSurface.release();
            mPreviewSurface = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
        synchronized (mCaptureSync) {
            if (mReqesutCaptureStillImage) {
                mReqesutCaptureStillImage = false;
                if (mTempBitmap == null)
                    mTempBitmap = getBitmap();
                else
                    getBitmap(mTempBitmap);
                mCaptureSync.notifyAll();
            }
        }
    }

    @Override
    public boolean hasSurface() {
        return mHasSurface;
    }

    /**
     * capture preview image as a bitmap
     * this method blocks current thread until bitmap is ready
     * if you call this method at almost same time from different thread,
     * the returned bitmap will be changed while you are processing the bitmap
     * (because we return same instance of bitmap on each call for memory saving)
     * if you need to call this method from multiple thread,
     * you should change this method(copy and return)
     */
    @Override
    public Bitmap captureStillImage() {
        synchronized (mCaptureSync) {
            mReqesutCaptureStillImage = true;
            try {
                mCaptureSync.wait();
            } catch (final InterruptedException e) {
            }
            return mTempBitmap;
        }
    }


    public float[] GetTemperatureData() {
        return mRenderHandler != null ? mRenderHandler.GetTemperatureData() : null;
    }

    @Override
    public SurfaceTexture getSurfaceTexture() {
        return mRenderHandler != null ? mRenderHandler.getPreviewTexture() : null;
    }

    private Surface mPreviewSurface;

    @Override
    public Surface getSurface() {
        if (DEBUG) Log.v(TAG, "getSurface:hasSurface=" + mHasSurface);
        if (mPreviewSurface == null) {
            final SurfaceTexture st = getSurfaceTexture();
            if (st != null) {
                mPreviewSurface = new Surface(st);
            }
        }
        return mPreviewSurface;
    }

    @Override
    public void setVideoEncoder(final IVideoEncoder encoder) {
        if (mRenderHandler != null)
            mRenderHandler.setVideoEncoder(encoder);
    }

    public int mSupportWidth;
    public int mSupportHeight;

    public void setSupportWidth(int width) {
        mSupportWidth = width;
    }

    public void setSupportHeight(int height) {
        mSupportHeight = height;
    }

    @Override
    public void setCallback(final Callback callback) {
        mCallback = callback;
    }

    @Override
    public ITemperatureCallback getTemperatureCallback() {
        return mRenderHandler != null ? mRenderHandler.getTemperatureCallback() : null;
    }

    public void setVertices(float scale) {
        if (mRenderHandler != null) {
            mRenderHandler.setVertices(scale);
        }
    }



    public void setSuportWH(int w, int h) {
        mSupportWidth = w;
        mSupportHeight = h;
        if (mRenderHandler != null) {
            mRenderHandler.setSuportWH(w, h);
        }
    }

    public void iniTempBitmap(int w, int h) {
        if (mRenderHandler != null) {
            mRenderHandler.iniTempBitmap(w, h);
        }
    }

//    public void setBitmap(Bitmap r, Bitmap g, Bitmap b, Bitmap y, Bitmap l) {
//        mRenderHandler.setBitmap(r, g, b, y, l);
//    }

    public void setTouchPoint(CopyOnWriteArrayList<TouchPoint> touchPoint) {
        mRenderHandler.setTouchPoint(touchPoint);
    }



    public void relayout(int rotate) {
        mRenderHandler.relayout(rotate);
    }

    public void watermarkOnOff(boolean isWatermaker) {
        mRenderHandler.watermarkOnOff(isWatermaker);
    }

    public void setUnitTemperature(int mode) {
        mRenderHandler.setUnitTemperature(mode);
    }

    public void resetFps() {
        mFpsCounter.reset();
    }

    /**
     * update frame rate of image processing
     */
    public void updateFps() {
        mFpsCounter.update();
    }

    /**
     * get current frame rate of image processing
     *
     * @return
     */
    public float getFps() {
        return mFpsCounter.getFps();
    }

    /**
     * get total frame rate from start
     *
     * @return
     */
    public float getTotalFps() {
        return mFpsCounter.getTotalFps();
    }

    /**
     * render camera frames on this view on a private thread
     *
     * @author saki
     */
    private static final class RenderHandler extends Handler
            implements SurfaceTexture.OnFrameAvailableListener {

        private static final int MSG_REQUEST_RENDER = 1;
        private static final int MSG_SET_ENCODER = 2;
        private static final int MSG_CREATE_SURFACE = 3;
        private static final int MSG_RESIZE = 4;
        private static final int MSG_TERMINATE = 9;

        private RenderHandler.RenderThread mThread;
        private boolean mIsActive = true;
        private final FpsCounter mFpsCounter;


        public static final RenderHandler createHandler(final FpsCounter counter,
                         final SurfaceTexture surface, final int width, final int height) {

            final RenderHandler.RenderThread thread = new RenderHandler.RenderThread(counter, surface, width, height);
            thread.start();
            return thread.getHandler();
        }

        private RenderHandler(final FpsCounter counter, final RenderHandler.RenderThread thread) {
            mThread = thread;
            mFpsCounter = counter;
        }

//        public void setBitmap(Bitmap r, Bitmap g, Bitmap b, Bitmap y, Bitmap l) {
//            mThread.setBitmap(r, g, b, y, l);
//        }

        public void setTouchPoint(CopyOnWriteArrayList<TouchPoint> touchPoint) {
            mThread.setTouchPoint(touchPoint);
        }



        public void relayout(int rotate) {
            mThread.relayout(rotate);
        }

        public void watermarkOnOff(boolean isWatermaker) {
            mThread.watermarkOnOff(isWatermaker);
        }

        public void setUnitTemperature(int mode) {
            mThread.setUnitTemperature(mode);
        }

        public final void setVideoEncoder(final IVideoEncoder encoder) {
            if (DEBUG) Log.v(TAG, "setVideoEncoder:");
            if (mIsActive)
                sendMessage(obtainMessage(MSG_SET_ENCODER, encoder));
        }

        public final SurfaceTexture getPreviewTexture() {
            if (DEBUG) Log.v(TAG, "getPreviewTexture:");
            if (mIsActive) {
                synchronized (mThread.mSync) {
                    sendEmptyMessage(MSG_CREATE_SURFACE);
                    try {
                        mThread.mSync.wait();
                    } catch (final InterruptedException e) {
                    }
                    return mThread.mPreviewSurface;
                }
            } else {

                return null;
            }
        }

        public void resize(final int width, final int height) {
            if (DEBUG) Log.v(TAG, "resize:");
            if (mIsActive) {
                synchronized (mThread.mSync) {
                    sendMessage(obtainMessage(MSG_RESIZE, width, height));
                    try {
                        mThread.mSync.wait();
                    } catch (final InterruptedException e) {
                    }
                }
            }
        }

        public final void release() {
            if (DEBUG) Log.v(TAG, "release:");
            if (mIsActive) {
                mIsActive = false;
                removeMessages(MSG_REQUEST_RENDER);
                removeMessages(MSG_SET_ENCODER);
                sendEmptyMessage(MSG_TERMINATE);
            }
        }

        @Override
        public final void onFrameAvailable(final SurfaceTexture surfaceTexture) {
            if (mIsActive) {
                mFpsCounter.count();
                sendEmptyMessage(MSG_REQUEST_RENDER);

            }
        }

        public ITemperatureCallback getTemperatureCallback() {
            return mThread != null ? mThread.getTemperatureCallback() : null;
        }

        public void setVertices(float scale) {
            if (mThread != null) {
                mThread.setVertices(scale);
            }
        }


        public float[] GetTemperatureData() {
            return mThread != null ? mThread.GetTemperatureData() : null;
        }


        public void setSuportWH(int w, int h) {
            if (mThread != null) {
                mThread.setSuportWH(w, h);
            }
        }

        public void iniTempBitmap(int w, int h) {
            if (mThread != null) {
                mThread.iniTempBitmap(w, h);
            }
        }

        @Override
        public final void handleMessage(final Message msg) {
            if (mThread == null) return;
            switch (msg.what) {
                case MSG_REQUEST_RENDER:
                    mThread.onDrawFrame();
                    break;
                case MSG_SET_ENCODER:
                    mThread.setEncoder((MediaEncoder) msg.obj);
                    break;
                case MSG_CREATE_SURFACE:
                    mThread.updatePreviewSurface();
                    break;
                case MSG_RESIZE:
                    mThread.resize(msg.arg1, msg.arg2);
                    break;
                case MSG_TERMINATE:
                    Looper.myLooper().quit();
                    mThread = null;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

        private static final class RenderThread extends Thread {
            private final Object mSync = new Object();
            private final SurfaceTexture mSurface;
            private RenderHandler mHandler;
            private EGLBase mEgl;
            /**
             * IEglSurface instance related to this TextureView
             */
            private EGLBase.IEglSurface mEglSurface;
            private GLDrawer2D1 mDrawer;
            private int mTexId = -1;
            private int[] mTexIds = {-1, -1, -1, -1};
            /**
             * SurfaceTexture instance to receive video images
             */
            private SurfaceTexture mPreviewSurface;
            private final float[] mStMatrix = new float[16];
            private MediaEncoder mEncoder;
            private int mViewWidth, mViewHeight;
            private final FpsCounter mFpsCounter;

            private P1 p1;
            private X1 x1;


            /**
             * constructor
             *
             * @param surface: drawing surface came from TexureView
             */
            public RenderThread(final FpsCounter fpsCounter, final SurfaceTexture surface, final int width, final int height) {
                mFpsCounter = fpsCounter;
                mSurface = surface;
                mViewWidth = width;
                mViewHeight = height;

                p1 = P1.getInstance();
                x1 = X1.getInstance();

                setName("RenderThread");
            }

            public void iniTempBitmap(int w, int h) {
                this.ondoCanvas = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); //建立一个空的图画板
                this.bitcanvas = new Canvas(ondoCanvas);
                this.mTouchPoint = new CopyOnWriteArrayList<TouchPoint>();
                this.photoPaint = new Paint();
            }

            public final RenderHandler getHandler() {
                if (DEBUG) Log.v(TAG, "RenderThread#getHandler:");
                synchronized (mSync) {
                    // create rendering thread
                    if (mHandler == null)
                        try {
                            mSync.wait();
                        } catch (final InterruptedException e) {
                        }
                }
                return mHandler;
            }

            public void resize(final int width, final int height) {
                if (((width > 0) && (width != mViewWidth)) || ((height > 0) && (height != mViewHeight))) {
                    mViewWidth = width;
                    mViewHeight = height;
                    updatePreviewSurface();
                } else {
                    synchronized (mSync) {
                        mSync.notifyAll();
                    }
                }
            }

            public final void updatePreviewSurface() {
                if (DEBUG) Log.i(TAG, "RenderThread#updatePreviewSurface:");
                synchronized (mSync) {
                    if (mPreviewSurface != null) {
                        if (DEBUG) Log.d(TAG, "updatePreviewSurface:release mPreviewSurface");
                        mPreviewSurface.setOnFrameAvailableListener(null);
                        mPreviewSurface.release();
                        mPreviewSurface = null;
                    }
                    mEglSurface.makeCurrent();
                    //           if (mTexId >= 0) {
                    //				mDrawer.deleteTex(mTexId);
                    //           }
                    if (mTexIds[0] >= 0 || mTexIds[1] >= 0 || mTexIds[2] >= 0 || mTexIds[3] >= 0) {
                        mDrawer.deleteTexes(mTexIds);
                    }
                    // create texture and SurfaceTexture for input from camera
                    //            mTexId = mDrawer.initTex();
                    int[] para = {4,
                            GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE,
                            GL_TEXTURE_2D, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE,
                            GL_TEXTURE_2D, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE,
                            GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE};
                    mTexIds = mDrawer.initTexes(para);
                    if (DEBUG) Log.v(TAG, "updatePreviewSurface:tex_id=" + mTexId);
                    mPreviewSurface = new SurfaceTexture(mTexIds[0]);
                    Log.d(TAG, "updatePreviewSurface:release mPreviewSurface "+String.valueOf(mViewWidth) + "x"+String.valueOf(mViewHeight));
                    mPreviewSurface.setDefaultBufferSize(mViewWidth, mViewHeight);
                    //mCamera2Surface.setDefaultBufferSize(mViewWidth, mViewHeight);
                    mPreviewSurface.setOnFrameAvailableListener(mHandler);
                    // notify to caller thread that previewSurface is ready
                    mSync.notifyAll();
                }
            }

            public final void setEncoder(final MediaEncoder encoder) {
                if (DEBUG) Log.v(TAG, "RenderThread#setEncoder:encoder=" + encoder);
                if (encoder != null && (encoder instanceof MediaVideoEncoder)) {
                    ((MediaVideoEncoder) encoder).setEglContext(mEglSurface.getContext(), mTexIds);
                }
                mEncoder = encoder;
            }

            /*
             * Now you can get frame data as ByteBuffer(as YUV/RGB565/RGBX/NV21 pixel format) using IFrameCallback interface
             * with UVCCamera#setFrameCallback instead of using following code samples.
             */
/*			// for part1
 			private static final int BUF_NUM = 1;
			private static final int BUF_STRIDE = 640 * 480;
			private static final int BUF_SIZE = BUF_STRIDE * BUF_NUM;
			int cnt = 0;
			int offset = 0;
			final int pixels[] = new int[BUF_SIZE];
			final IntBuffer buffer = IntBuffer.wrap(pixels); */
/*			// for part2
			private ByteBuffer buf = ByteBuffer.allocateDirect(640 * 480 * 4);
 */
            //private boolean isCbTemping = false;
            //private boolean isCamera2ing = false;
            //private Bitmap mCursorBlue, mCursorRed, mCursorYellow, mCursorGreen, mWatermakLogo;
            private float[] temperature1 = new float[640 * 512 + 10];

            //  private Bitmap icon,iconPalette; //建立一个空的图画板
            // private Canvas canvas,bitcanvas,paletteCanvas,paletteBitmapCanvas;//初始化画布绘制的图像到icon上
            private Paint photoPaint;
            private Rect dstHighTemp, dstLowTemp, bounds;//创建一个指定的新矩形的坐标
            private Bitmap ondoCanvas;
            private Canvas bitcanvas;//初始化画布绘制的图像到icon上
            private CopyOnWriteArrayList<TouchPoint> mTouchPoint;
            private int UnitTemperature;
            private int rotate = 0;
            private boolean isWatermaker = true;

//            public void setBitmap(Bitmap mRed, Bitmap mGreen, Bitmap mBlue, Bitmap mYellow, Bitmap mLogo) {
//                mCursorBlue = mBlue;
//                mCursorRed = mRed;
//                mCursorYellow = mYellow;
//                mCursorGreen = mGreen;
//                mWatermakLogo = mLogo;
//            }


            public void relayout(int rotate) {
                this.rotate = rotate;
            }

            public void watermarkOnOff(boolean isWatermaker) {
//                Log.e(TAG, "watermarkOnOff: isWatermaker" + isWatermaker);
                this.isWatermaker = isWatermaker;
            }

            public float[] GetTemperatureData() {
                return temperature1;
            }

            public int mSuportWidth;
            public int mSuportHeight;

            public void setTouchPoint(CopyOnWriteArrayList<TouchPoint> touchPoint) {
                this.mTouchPoint = touchPoint;
            }


            public void setUnitTemperature(int mode) {
                this.UnitTemperature = mode;
            }

            public final ITemperatureCallback ahITemperatureCallback = new ITemperatureCallback() {
                @Override
                public void onReceiveTemperature(float[] temperature) {
                    //Log.e(TAG, "ITemperatureCallback center"+temperature[0]);
                    // 256 x 196
                    // 49152 ( 256 x 192 )
                    //Log.e(TAG, UnitTemperature);
                    if (UnitTemperature == 0) {

                        //System.arraycopy(temperature, 0, temperature1, 0, (mSuportHeight - 4) * mSuportWidth + 10);
                        x1.max1 = temperature[3] + Cfg.ondo_offSet;
                        x1.min1 = temperature[6] + Cfg.ondo_offSet;
//                        x1.min1 = -2.345f;
                       //Log.i(TAG, String.format("%.2f, %.2f", x1.min1, x1.max1));

                        System.arraycopy(temperature, 10, x1.ondoBuf, 0, mSuportHeight * mSuportWidth);
                        //System.arraycopy(temperature, 0, temperature1, 0, mSuportHeight * mSuportWidth);

//                        if (p1.camMode == Consts.CAM_ONDO) {
//                            x1.center = 0;
//                            for (int y1=0; y1<15; y1++) {
//                                int y=y1*256;
//                                for (int x=0; x<15; x++) {
//                                    if (x1.ondoBuf[y+118+x] > x1.center) {
//                                        x1.center = x1.ondoBuf[y+118+x];
//                                    }
//                                }
//                            }
//
//                            for (int y1=0; y1<15; y1++) {
//                                int y=y1*256;
//                                for (int x=0; x<15; x++) {
//                                    x1.ondoBuf[y+118+x] = x1.center;
//                                }
//                            }
//
//                        }

//                        x1.max1 = 0;
//                        x1.min1 = 250;

//                        for (int i = 0; i < ((mSuportHeight) * mSuportWidth); i++) {
//                            if (x1.ondoBuf[i]>x1.max1) x1.max1 = x1.ondoBuf[i];
//                            if (x1.ondoBuf[i]<x1.min1) x1.min1 = x1.ondoBuf[i];
//                        }

                        //Log.i(TAG, " == RecvTemp" + String.valueOf(temperature.length));
                    } else {

//                        temperature1[0] = temperature[0] * 1.8f + 32;//中心温度
//                        temperature1[1] = temperature[1];//MAXX1
//                        temperature1[2] = temperature[2];//MAXY1
//                        temperature1[3] = temperature[3] * 1.8f + 32;//最高温
//                        temperature1[4] = temperature[4];//MINX1
//                        temperature1[5] = temperature[5];//MIXY1
////                        Log.e(TAG, "onDrawFrame[7]: "+temperature1[7]);
//                        for (int i = 6; i < ((mSuportHeight - 4) * mSuportWidth + 10); i++) {
//                            temperature1[i] = temperature[i] * 1.8f + 32;
//                        }
                    }
                }
            };

            public ITemperatureCallback getTemperatureCallback() {
                return ahITemperatureCallback;
            }


            public void setSuportWH(int w, int h) {


                Log.e(TAG, " == Width : " + String.valueOf(w)+ " Height : "+ String.valueOf(h) );
                x1.ondoWidth = w;
                x1.ondoHeight = h-4;
                mSuportWidth = w;
                mSuportHeight = h-4;

            }

            public void setVertices(float scale) {
                mDrawer.setVertices(scale);
            }
            private int isFirstCome =0;


            /**
             * draw a frame (and request to draw for video capturing if it is necessary)
             */
            public final void onDrawFrame() {

                if (p1.camMode != Consts.CAM_ONDO) {
                    return;
                } else {
                    float canvasWidth = (float) ondoCanvas.getWidth();
                    float canvasHeight = (float) ondoCanvas.getHeight();

                    p1.drawP1Data(bitcanvas, (int) canvasWidth, (int) canvasHeight);

                    mEglSurface.makeCurrent();
                    //update texture(came from camera)
                    mPreviewSurface.updateTexImage();

                    // get texture matrix
                    mPreviewSurface.getTransformMatrix(mStMatrix);
//                Matrix.rotateM(mStMatrix, 0, 90, 0, 0, 1);
//                Matrix.translateM(mStMatrix, 0, 0, -1, 0);

                    // notify video encoder if it exist
                    if (mEncoder != null) {
                        // notify to capturing thread that the camera frame is available.
                        if (mEncoder instanceof MediaVideoEncoder)
                            ((MediaVideoEncoder) mEncoder).frameAvailableSoon(mStMatrix, ondoCanvas);
                        else
                            mEncoder.frameAvailableSoon();
                    }

                    // draw to preview screen
                    mDrawer.draw(mTexIds, mStMatrix, 0, ondoCanvas);//屏幕的draw

                    mEglSurface.swap();
                }
            }


            @Override
            public final void run() {
                Log.d(TAG, getName() + " started");
                init();
                Looper.prepare();
                synchronized (mSync) {
                    mHandler = new RenderHandler(mFpsCounter, this);
                    mSync.notify();
                }

                Looper.loop();

                Log.d(TAG, getName() + " finishing");
                release();
                synchronized (mSync) {
                    mHandler = null;
                    mSync.notify();
                }
            }

            private final void init() {
                if (DEBUG) Log.v(TAG, "RenderThread#init:");
                // create EGLContext for this thread
                mEgl = EGLBase.createFrom(null, false, false);
                mEglSurface = mEgl.createFromSurface(mSurface);
                mEglSurface.makeCurrent();
                // create drawing object
                mDrawer = new GLDrawer2D1(true);
            }

            private final void release() {
                if (DEBUG) Log.v(TAG, "RenderThread#release:");
                if (mDrawer != null) {
                    mDrawer.release();
                    mDrawer = null;
                }
                if (mPreviewSurface != null) {
                    mPreviewSurface.release();
                    mPreviewSurface = null;
                }
                if (mTexId >= 0) {
                    GLHelper1.deleteTex(mTexId);
                    mTexId = -1;
                }
                if (mEglSurface != null) {
                    mEglSurface.release();
                    mEglSurface = null;
                }
                if (mEgl != null) {
                    mEgl.release();
                    mEgl = null;
                }
            }
        }
    }
}
