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

package com.serenegiant.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

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



//import com.serenegiant.glutils.GLDrawer2D;

/**
 * change the view size with keeping the specified aspect ratio.
 * if you set this view with in a FrameLayout and set property "android:layout_gravity="center",
 * you can show this view in the center of screen and keep the aspect ratio of content
 * XXX it is better that can set the aspect ratio as xml property
 */
public class UVCCameraTextureView extends AspectRatioTextureView    // API >= 14
        implements TextureView.SurfaceTextureListener, CameraViewInterface {

    private static final boolean DEBUG = true;    // TODO set false on release
    private static final String TAG = "UVCCameraTextureView";

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

    public UVCCameraTextureView(final Context context) {
        this(context, null, 0);
    }

    public UVCCameraTextureView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UVCCameraTextureView(final Context context, final AttributeSet attrs, final int defStyle) {
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

    public void openSysCamera() {
        if (mRenderHandler != null) {
            mRenderHandler.openSysCamera();
        }
    }

    public void closeSysCamera() {
        if (mRenderHandler != null) {
            mRenderHandler.closeSysCamera();
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

    public void setTemperatureCbing(boolean isTempCbing) {
        if (mRenderHandler != null) {
            mRenderHandler.setTemperatureCbing(isTempCbing);
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

    public void setBitmap(Bitmap r, Bitmap g, Bitmap b, Bitmap y, Bitmap l) {
        mRenderHandler.setBitmap(r, g, b, y, l);
    }

    public void setTouchPoint(CopyOnWriteArrayList<TouchPoint> touchPoint) {
        mRenderHandler.setTouchPoint(touchPoint);
    }

    public void setTemperatureAnalysisMode(int mode) {
        mRenderHandler.setTemperatureAnalysisMode(mode);
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

        private RenderThread mThread;
        private boolean mIsActive = true;
        private final FpsCounter mFpsCounter;


        public static final RenderHandler createHandler(final FpsCounter counter,
                                                        final SurfaceTexture surface, final int width, final int height) {

            final RenderThread thread = new RenderThread(counter, surface, width, height);
            thread.start();
            return thread.getHandler();
        }

        private RenderHandler(final FpsCounter counter, final RenderThread thread) {
            mThread = thread;
            mFpsCounter = counter;
        }

        public void setBitmap(Bitmap r, Bitmap g, Bitmap b, Bitmap y, Bitmap l) {
            mThread.setBitmap(r, g, b, y, l);
        }

        public void setTouchPoint(CopyOnWriteArrayList<TouchPoint> touchPoint) {
            mThread.setTouchPoint(touchPoint);
        }

        public void setTemperatureAnalysisMode(int mode) {
            mThread.setTemperatureAnalysisMode(mode);
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

        public void setTemperatureCbing(boolean isTempCbing) {
            if (mThread != null) {
                mThread.setTemperatureCbing(isTempCbing);
            }
        }

        public float[] GetTemperatureData() {
            return mThread != null ? mThread.GetTemperatureData() : null;
        }

        public void openSysCamera() {
            if (mThread != null) {
                mThread.openSysCamera();
            }
        }

        public void closeSysCamera() {
            if (mThread != null) {
                mThread.closeSysCamera();
            }
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
            private SurfaceTexture mPreviewSurface, mCamera2Surface;
            private final float[] mStMatrix = new float[16];
            private MediaEncoder mEncoder;
            private int mViewWidth, mViewHeight;
            private final FpsCounter mFpsCounter;
            private Camera2Helper mCamera2Helper;

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
                mCamera2Helper = Camera2Helper.getInstance();
                // this.dstHighTemp,dstLowTemp,bounds ;//创建一个指定的新矩形的坐标

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
            private boolean isCbTemping = false;
            private boolean isCamera2ing = false;
            private Bitmap mCursorBlue, mCursorRed, mCursorYellow, mCursorGreen, mWatermakLogo;
            private float[] temperature1 = new float[640 * 512 + 10];

            //  private Bitmap icon,iconPalette; //建立一个空的图画板
            // private Canvas canvas,bitcanvas,paletteCanvas,paletteBitmapCanvas;//初始化画布绘制的图像到icon上
            private Paint photoPaint;
            private Rect dstHighTemp, dstLowTemp, bounds;//创建一个指定的新矩形的坐标
            private Bitmap ondoCanvas;
            private Canvas bitcanvas;//初始化画布绘制的图像到icon上
            private CopyOnWriteArrayList<TouchPoint> mTouchPoint;
            private int temperatureAnalysisMode, UnitTemperature;
            private int rotate = 0;
            private boolean isWatermaker = true;

            public void setBitmap(Bitmap mRed, Bitmap mGreen, Bitmap mBlue, Bitmap mYellow, Bitmap mLogo) {
                mCursorBlue = mBlue;
                mCursorRed = mRed;
                mCursorYellow = mYellow;
                mCursorGreen = mGreen;
                mWatermakLogo = mLogo;
            }

            //private P1 p1;

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

            public void openSysCamera() {
                if (mCamera2Helper == null) {
                    mCamera2Helper = mCamera2Helper.getInstance();
                }

                mCamera2Helper.openCamera(640, 480);
                isCamera2ing = true;
            }

            public void closeSysCamera() {
                if (mCamera2Helper != null) {
                    mCamera2Helper.closeCamera();
                }
                isCamera2ing = false;
            }

            public int mSuportWidth;//探测器的面阵
            public int mSuportHeight;

            public void setTouchPoint(CopyOnWriteArrayList<TouchPoint> touchPoint) {
                this.mTouchPoint = touchPoint;
            }

            public void setTemperatureAnalysisMode(int mode) {
                this.temperatureAnalysisMode = mode;
            }

            public void setUnitTemperature(int mode) {
                this.UnitTemperature = mode;
            }

            public final ITemperatureCallback ahITemperatureCallback = new ITemperatureCallback() {
                @Override
                public void onReceiveTemperature(float[] temperature) {
                    //Log.e(TAG, "ITemperatureCallback center");
                    //Log.e(TAG, "ITemperatureCallback center"+temperature[0]);
                    if (UnitTemperature == 0) {
                        System.arraycopy(temperature, 0, temperature1, 0, (mSuportHeight - 4) * mSuportWidth + 10);
                    } else {
                        temperature1[0] = temperature[0] * 1.8f + 32;//中心温度
                        temperature1[1] = temperature[1];//MAXX1
                        temperature1[2] = temperature[2];//MAXY1
                        temperature1[3] = temperature[3] * 1.8f + 32;//最高温
                        temperature1[4] = temperature[4];//MINX1
                        temperature1[5] = temperature[5];//MIXY1
                        Log.e(TAG, "onDrawFrame[7]: "+temperature1[7]);
                        for (int i = 6; i < ((mSuportHeight - 4) * mSuportWidth + 10); i++) {
                            temperature1[i] = temperature[i] * 1.8f + 32;
                        }
                    }
                }
            };

            public ITemperatureCallback getTemperatureCallback() {
                return ahITemperatureCallback;
            }

            public void setTemperatureCbing(boolean isTempCbing) {
                isCbTemping = isTempCbing;
            }

            public void setSuportWH(int w, int h) {
                mSuportHeight = h;
                mSuportWidth = w;

            }

            public void setVertices(float scale) {
                mDrawer.setVertices(scale);
            }
            private int isFirstCome =0;
            /**
             * 选择变换
             *
             * @param origin 原图
             * @param alpha  旋转角度，可正可负
             * @return 旋转后的图片
             */
            private Bitmap rotateBitmap(Bitmap origin, float alpha) {
                if (origin == null) {
                    return null;
                }
                int width = origin.getWidth();
                int height = origin.getHeight();
                Matrix matrix = new Matrix();
                matrix.setRotate(alpha);
                // 围绕原地进行旋转
                Bitmap newBM = null;
//                if(isT3){
//////                  newBM = Bitmap.createScaledBitmap(origin,  icon.getWidth() / 2, icon.getHeight() / 2,false);
//                      newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
////                    newBM = skewBitmap(newBM);
////                    newBM = Bitmap.createBitmap(origin,0,0,  icon.getWidth() / 2, icon.getHeight() / 2, matrix, false);
////                    newBM =horverImage(origin,true,true);
//                }else{
                newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
//                }
                if (newBM.equals(origin)) {
                    return newBM;
                }
                origin.recycle();
                return newBM;
            }

            private Boolean isT3 = MyApp.deviceName.contentEquals("T3") || MyApp.deviceName.contentEquals("DL13")|| MyApp.deviceName.contentEquals("DV");

            /**
             * draw a frame (and request to draw for video capturing if it is necessary)
             */
            public final void onDrawFrame() {
                String extern;
                float x, y;

                if (UnitTemperature == 0) {
                    extern = "°C";
                } else {
                    extern = "°F";
                }
                bitcanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                Log.e("rotate:",rotate+"");
                //bitcanvas.drawARGB(0,0,0,0);
//                if (isWatermaker && (rotate == 0 || rotate == 180)) {
//                    x = (float) icon.getWidth() / 2;
//                    y = (float) icon.getHeight() / 2;
//
////                        bitcanvas.rotate(rotate, x, y);
//                bitcanvas.drawBitmap(mWatermakLogo, icon.getWidth() - mWatermakLogo.getWidth() - 10, 10, photoPaint);
////                        bitcanvas.rotate(360 - rotate, x, y);
//                    isFirstCome++;
//                }
//                if (isWatermaker && (rotate == 90 || rotate == 270)) {
//                    x = (float) icon.getWidth() / 2;
//                    y = (float) icon.getHeight() / 2;
//                    float offset = (float) (icon.getWidth() - icon.getHeight()) / 2.0f;
//                    if (isT3) {
////                        bitcanvas.rotate(360 - rotate, x, y);
//                        bitcanvas.drawBitmap(mWatermakLogo, offset + icon.getHeight() - mWatermakLogo.getWidth() - 10, -offset + 10, photoPaint);
////                        bitcanvas.rotate(rotate, x, y);
//                    } else {
////                        bitcanvas.rotate(rotate, x, y);
//                        bitcanvas.drawBitmap(mWatermakLogo, offset + icon.getHeight() - mWatermakLogo.getWidth() - 10, -offset + 10, photoPaint);
////                        bitcanvas.rotate(360 - rotate, x, y);
//                    }
//                }
//                if (isCamera2ing) {
//                    Bitmap mCamera2Bitmap = mCamera2Helper.getCamera2Bitmap();
//                    if (mCamera2Bitmap != null) {
//                        Bitmap bp = null;
//                        if (isT3) {
//                            bp = rotateBitmap(mCamera2Bitmap, 90);
//                            bitcanvas.drawBitmap(bp, 1, 1, photoPaint);
////                            bitcanvas.drawBitmap(bp,icon.getWidth()-mCamera2Bitmap.getHeight(),icon.getHeight()-mCamera2Bitmap.getWidth(),  photoPaint);
//                        } else {
//                            bp = rotateBitmap(mCamera2Bitmap, 270);
//                            bitcanvas.drawBitmap(bp, 1, 1, photoPaint);
//                        }
////                        Log.e(TAG, "onDrawFrame: mCamera2Bitmap!=null width:" + mCamera2Bitmap.getWidth() + " height:" + mCamera2Bitmap.getHeight());
////                        bitcanvas.drawBitmap(bp, 1, 1, photoPaint);
//                        mCamera2Bitmap = null;
//                    }
//                }
//
                isCbTemping = false;
                if (!isCbTemping) {
                    bitcanvas.save();
                    //bitcanvas.save(Canvas.ALL_SAVE_FLAG);
                } else {
                    temperatureAnalysisMode=0;
                    if (temperatureAnalysisMode == 0) {//0:
                        DecimalFormat decimalFormat = new DecimalFormat("0.0");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                        String centerTemp = decimalFormat.format(temperature1[0]) + extern;//format 返回的是字符串
                        String maxTemp = decimalFormat.format(temperature1[3]) + extern;
                        String minTemp = decimalFormat.format(temperature1[6]) + extern;
                        String point1Temp;//= decimalFormat.format(temperature[7]);
                        String point2Temp;//= decimalFormat.format(temperature[8]);
                        String point3Temp;//= decimalFormat.format(temperature[9]);
                        String point4Temp;
                        String point5Temp;
                        //Log.e(TAG, "onReceiveTemperature:" + temperature1);
                        //	mTempbutton.setText(s);
                        float maxx1 = temperature1[1];
                        float maxy1 = temperature1[2];
                        //float minx1 = temperature1[4];
                        //float miny1 = temperature1[5];

                        float pointWidth = mCursorRed.getWidth() / 2f;
                        float pointHeight = mCursorRed.getHeight() / 2f;

                        float canvasWidth = (float) ondoCanvas.getWidth();
                        float canvasHeight = (float) ondoCanvas.getHeight();

                        //	Paint p = new Paint();
                        //清屏
                        //	p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                        //	bitcanvas.drawPaint(p);
                        //	p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                        //	p.setStyle(Paint.Style.FILL);

                        //Log.e(TAG, "onReceiveTemperature:mCursor.getWidth:"+mCursor.getWidth());
                        bitcanvas.drawBitmap(mCursorRed, canvasWidth / (float) mSuportWidth * maxx1 - pointWidth, canvasHeight / (float) (mSuportHeight - 4) * maxy1 - pointHeight, photoPaint);
                        //Log.e(TAG, "onReceiveTemperature maxx1:" + maxx1);
                        //bitcanvas.drawBitmap(mCursorBlue, (float) icon.getWidth() / (float) mSuportWidth * minx1 - (float) mCursorBlue.getWidth() / 2.0f, (float) icon.getHeight() / (float) (mSuportHeight - 4) * miny1 - (float) mCursorBlue.getHeight() / 2.0f, photoPaint);
                        //bitcanvas.drawBitmap(mCursorYellow, (float) icon.getWidth() / 2.0f - (float) mCursorYellow.getWidth() / 2.0f, (float) icon.getHeight() / 2.0f - (float) mCursorYellow.getHeight() / 2.0f, photoPaint);
                        photoPaint.setStrokeWidth(5);
                        photoPaint.setTextSize(50);
                        photoPaint.setColor(Color.GREEN);

                        //photoPaint.setTextAlign(Paint.Align.LEFT);
                        if (bounds == null) {
                            bounds = new Rect();
                        }
                        if (dstHighTemp == null) {
                            dstHighTemp = new Rect();
                        }
                        if (dstLowTemp == null) {
                            dstLowTemp = new Rect();
                        }



//                        bitcanvas.drawText(centerTemp, icon.getWidth() / 2.0f+ mCursorYellow.getWidth() / 2, icon.getHeight() / 2.0f+mCursorYellow.getHeight() / 2, photoPaint);
//                        photoPaint.getTextBounds(centerTemp, 0, centerTemp.length(), bounds);
//                        photoPaint.setColor(Color.YELLOW);
//                        x = (float) icon.getWidth() / 2.0f;
//                        y = icon.getHeight() / 2.0f;
//
//                        bitcanvas.drawText(centerTemp, x + (float) mCursorYellow.getWidth() / 2.0f, y + mCursorYellow.getHeight() / 2.0f, photoPaint);

//                        photoPaint.getTextBounds(maxTemp, 0, maxTemp.length(), dstHighTemp);
                        photoPaint.setColor(Color.RED);

                        x = canvasWidth / (float) mSuportWidth * maxx1;
                        y = canvasHeight  / (float) (mSuportHeight - 4) * maxy1;
                        x = x + 5f;
                        y = y - 8f;
                        bitcanvas.drawText(maxTemp, x + pointWidth, y + pointHeight, photoPaint);
                        //photoPaint.getTextBounds(minTemp, 0, minTemp.length(), dstLowTemp);

//                        photoPaint.setColor(Color.BLUE);
//                        x = (float) icon.getWidth() / (float) mSuportWidth * minx1;
//                        y = (float) icon.getHeight() / (float) (mSuportHeight - 4) * miny1;
//                        x = x + 5f;
//                        y = y - 8f;
//                            bitcanvas.drawText(minTemp, x + (float) mCursorBlue.getWidth() / 2.0f, y + (float) mCursorBlue.getHeight() / 2.0f, photoPaint);

                        photoPaint.setColor(Color.GREEN);
                        //         photoPaint.getTextBounds(point1Temp, 0, point1Temp.length(), bounds);
                        try {
                            for (TouchPoint j : mTouchPoint) {

                                //photoPaint.setTextSize(20);
                                float pointx = j.x * canvasWidth - pointWidth;
                                float pointy = j.y * canvasHeight - pointHeight;
                                bitcanvas.drawBitmap(mCursorGreen, pointx, pointy, photoPaint);
                                int index =0;
                                //int index;//=(int)(j.y*288*384+j.x*384-10);
                                index = (((int) (j.y * (mSuportHeight - 4))) * mSuportWidth + (int) (j.x * mSuportWidth) + 10);
                                //int index=(384*288/2+384/2+10);
                                x = j.x * canvasWidth;
                                y = j.y * canvasHeight;
                                x = x + 5f;
                                y = y - 8f;
                                switch (j.numOfPoint) {
                                    case 0:
                                        point1Temp = decimalFormat.format(temperature1[index]) + extern;
                                        bitcanvas.drawText(point1Temp, x + pointWidth / 2.0f, y + pointHeight, photoPaint);
                                        break;
                                    case 1:
                                        point2Temp = decimalFormat.format(temperature1[index]) + extern;
                                        bitcanvas.drawText(point2Temp, x + pointWidth, y + pointHeight, photoPaint);
                                        break;

                                }
                            }
                        } catch (Exception e) {

                        }
                        bitcanvas.save();
                        //bitcanvas.save(Canvas.ALL_SAVE_FLAG);
                        isCbTemping=true;

                    }

                    bitcanvas.save();
                    //bitcanvas.save(Canvas.ALL_SAVE_FLAG);
                }


                mEglSurface.makeCurrent();
                // update texture(came from camera)
                mPreviewSurface.updateTexImage();
                //mCamera2Surface.updateTexImage();

                // get texture matrix
                mPreviewSurface.getTransformMatrix(mStMatrix);
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



/*				// sample code to read pixels into Buffer and save as a Bitmap (part1)
				buffer.position(offset);
				GLES20.glReadPixels(0, 0, 640, 480, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
				if (++cnt == 100) { // save as a Bitmap, only once on this sample code
					// if you save every frame as a Bitmap, app will crash by Out of Memory exception...
					Log.i(TAG, "Capture image using glReadPixels:offset=" + offset);
					final Bitmap bitmap = createBitmap(pixels,offset,  640, 480);
					final File outputFile = MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, ".png");
					try {
						final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
						try {
							try {
								bitmap.compress(CompressFormat.PNG, 100, os);
								os.flush();
								bitmap.recycle();
							} catch (IOException e) {
							}
						} finally {
							os.close();
						}
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
					}
				}
				offset = (offset + BUF_STRIDE) % BUF_SIZE;
*/
/*				// sample code to read pixels into Buffer and save as a Bitmap (part2)
		        buf.order(ByteOrder.LITTLE_ENDIAN);	// it is enough to call this only once.
		        GLES20.glReadPixels(0, 0, 640, 480, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
		        buf.rewind();
				if (++cnt == 100) {	// save as a Bitmap, only once on this sample code
					// if you save every frame as a Bitmap, app will crash by Out of Memory exception...
					final File outputFile = MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, ".png");
			        BufferedOutputStream os = null;
					try {
				        try {
				            os = new BufferedOutputStream(new FileOutputStream(outputFile));
				            Bitmap bmp = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
				            bmp.copyPixelsFromBuffer(buf);
				            bmp.compress(Bitmap.CompressFormat.PNG, 90, os);
				            bmp.recycle();
				        } finally {
				            if (os != null) os.close();
				        }
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
					}
				}
*/
            }

/*			// sample code to read pixels into IntBuffer and save as a Bitmap (part1)
			private static Bitmap createBitmap(final int[] pixels, final int offset, final int width, final int height) {
				final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
				paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[] {
						0, 0, 1, 0, 0,
						0, 1, 0, 0, 0,
						1, 0, 0, 0, 0,
						0, 0, 0, 1, 0
					})));

				final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				final Canvas canvas = new Canvas(bitmap);

				final Matrix matrix = new Matrix();
				matrix.postScale(1.0f, -1.0f);
				matrix.postTranslate(0, height);
				canvas.concat(matrix);

				canvas.drawBitmap(pixels, offset, width, 0, 0, width, height, false, paint);

				return bitmap;
			} */

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
