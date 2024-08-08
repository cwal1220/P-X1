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

package com.inspeco.X1.CamView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.inspeco.data.Consts;
import com.inspeco.data.P1;
import com.serenegiant.encoder.IVideoEncoder;
import com.serenegiant.encoder.MediaEncoder;
import com.serenegiant.encoder.MediaVideoEncoder;
import com.serenegiant.glutils.EGLBase;
import com.serenegiant.usb.ITemperatureCallback;
import com.serenegiant.utils.FpsCounter;
import com.serenegiant.widget.AspectRatioTextureView;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.GLDrawer2D1;
import com.serenegiant.widget.GLHelper1;
import com.serenegiant.widget.TouchPoint;

import java.util.concurrent.CopyOnWriteArrayList;

import static com.serenegiant.glutils.ShaderConst.GL_TEXTURE_2D;
import static com.serenegiant.glutils.ShaderConst.GL_TEXTURE_EXTERNAL_OES;

//import com.serenegiant.glutils.GLDrawer2D;

/**
 * change the view size with keeping the specified aspect ratio.
 * if you set this view with in a FrameLayout and set property "android:layout_gravity="center",
 */
public class WebCamTextureView extends AspectRatioTextureView    // API >= 14
        implements TextureView.SurfaceTextureListener, CameraViewInterface, View.OnTouchListener {

    private static final boolean DEBUG = true;
    private static final boolean TEST_MODE_NOCAM = false;
    private static final String TAG = "bobopro WebCamTextureView";

    private boolean mHasSurface;
    private RenderHandler mRenderHandler;
    private final Object mCaptureSync = new Object();
    private Bitmap mTempBitmap;
    private boolean mReqesutCaptureStillImage;
    private Callback mCallback;
    private int width, height;
    private P1 p1;


    /**
     *
     * for calculation of frame rate
     */
    private final FpsCounter mFpsCounter = new FpsCounter();

    public WebCamTextureView(final Context context) {
        this(context, null, 0);
    }

    public WebCamTextureView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebCamTextureView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setSurfaceTextureListener(this);
        p1 = P1.getInstance();

        if (TEST_MODE_NOCAM) {
            Log.v(TAG, "Cam 화면 Test Mode");

            setOnTouchListener(this);
        }

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

        if (DEBUG) Log.v(TAG, "onSurfaceTextureAvailable:");
        this.width = width;
        this.height = height;
        if (TEST_MODE_NOCAM) {
            Log.v(TAG, "NoCam Test Mode");

        } else {
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

    }

    public boolean onTouch(View v, MotionEvent e) {
        //mRenderHandler.sendEmptyMessage(1); //render

        Log.v(TAG, "Cam 화면 Touch");
        Canvas bitcanvas = lockCanvas();

        p1.drawP1Data(bitcanvas, width, height);

        unlockCanvasAndPost(bitcanvas);

        return false;
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
            //Log.d(TAG, "onSurfaceTextureUpdated Save bitmap Capturea");
            if (mTempBitmap == null)
                mTempBitmap = getBitmap();
            else
                getBitmap(mTempBitmap);


            if (mReqesutCaptureStillImage) {
                Log.d(TAG, "onSurfaceTextureUpdated Save bitmap Capturea");
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
            Log.d(TAG, "captureStillImage Save bitmap Capturea");

            mReqesutCaptureStillImage = true;
            try {
                mCaptureSync.wait();
            } catch (final InterruptedException e) {
            }
            return mTempBitmap;
        }
    }

//    public void openSysCamera() {
//        if (mRenderHandler != null) {
//            mRenderHandler.openSysCamera();
//        }
//    }
//
//    public void closeSysCamera() {
//        if (mRenderHandler != null) {
//            mRenderHandler.closeSysCamera();
//        }
//    }



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
        return null;
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
            mRenderHandler.iniTempBitmap(w, h, this.getContext());
        }

    }





    /**
     * render camera frames on this view on a private thread
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


        public void setTouchPoint(CopyOnWriteArrayList<TouchPoint> touchPoint) {
            mThread.setTouchPoint(touchPoint);
        }


        public void relayout(int rotate) {
            mThread.relayout(rotate);
        }

//        public void watermarkOnOff(boolean isWatermaker) {
//            mThread.watermarkOnOff(isWatermaker);
//        }
//
//        public void setUnitTemperature(int mode) {
//            mThread.setUnitTemperature(mode);
//        }

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


        public void setVertices(float scale) {
            if (mThread != null) {
                mThread.setVertices(scale);
            }
        }

//


        public void setSuportWH(int w, int h) {
            if (mThread != null) {
                mThread.setSuportWH(w, h);
            }
        }

        public void iniTempBitmap(int w, int h, final Context context) {
            if (mThread != null) {
                mThread.iniTempBitmap(w, h, context);
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



        // 랜더 쓰레드...
        private static final class RenderThread extends Thread {
            private final Object mSync = new Object();
            private final SurfaceTexture mSurface;
            private WebCamTextureView.RenderHandler mHandler;
            private EGLBase mEgl;
            private P1 p1;
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
            //private Camera2Helper mCamera2Helper;


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

                //mCamera2Helper = Camera2Helper.getInstance();
                p1 = P1.getInstance();
                // this.dstHighTemp,dstLowTemp,bounds ;

                setName("RenderThread");
            }

            public void iniTempBitmap(int w, int h, final Context context) {
                this.camBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                this.bitcanvas = new Canvas(camBitmap);
                this.mTouchPoint = new CopyOnWriteArrayList<TouchPoint>();
                this.paint = new Paint();


            }

            public final WebCamTextureView.RenderHandler getHandler() {
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
                //Log.i(TAG, "Aaasldkfjalsdkjflksdf=======asdkfalsdkjalsdkjf====asdklfjas;dljf===");
                if (DEBUG) Log.i(TAG, "RenderThread# updatePreviewSurface:");
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
//            private boolean isCbTemping = false;
//            private boolean isCamera2ing = false;
            //private Bitmap mCursorBlue, mCursorRed, mCursorYellow, mCursorGreen, mWatermakLogo;
            private float[] temperature1 = new float[640 * 512 + 10];

            //  private Bitmap icon,iconPalette; //建立一个空的图画板
            // private Canvas canvas,bitcanvas,paletteCanvas,paletteBitmapCanvas;//初始化画布绘制的图像到icon上
            private Paint paint;

            private Bitmap camBitmap;
            private Canvas bitcanvas;//初始化画布绘制的图像到icon上
            private CopyOnWriteArrayList<TouchPoint> mTouchPoint;
            private int temperatureAnalysisMode, UnitTemperature;
            private int rotate = 0;



            public void relayout(int rotate) {
                this.rotate = rotate;
            }



            public int mSuportWidth;//探测器的面阵
            public int mSuportHeight;

            public void setTouchPoint(CopyOnWriteArrayList<TouchPoint> touchPoint) {
                this.mTouchPoint = touchPoint;
            }

//            }

            public void setSuportWH(int w, int h) {
                mSuportHeight = h;
                mSuportWidth = w;

            }

            public void setVertices(float scale) {
                mDrawer.setVertices(scale);
            }
            private int isFirstCome =0;


            /**
             * draw a frame (and request to draw for video capturing if it is necessary)
             */
            public final void onDrawFrame() {


                p1.drawP1Data(bitcanvas,mViewWidth, mViewHeight);


                bitcanvas.save();
                mEglSurface.makeCurrent();
                mPreviewSurface.updateTexImage();

                // get texture matrix
                mPreviewSurface.getTransformMatrix(mStMatrix);

                // notify video encoder if it exist
                if (mEncoder != null) {
                    // notify to capturing thread that the camera frame is available.
                    if (mEncoder instanceof MediaVideoEncoder)
                        ((MediaVideoEncoder) mEncoder).frameAvailableSoon(mStMatrix, camBitmap);
                    else
                        mEncoder.frameAvailableSoon();
                }
                // draw to preview screen
                mDrawer.draw(mTexIds, mStMatrix, 0, camBitmap);

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
                    mHandler = new WebCamTextureView.RenderHandler(mFpsCounter, this);
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
