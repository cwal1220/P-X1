package com.inspeco.X1.HomeView

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle

import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.inspeco.X1.R
import com.inspeco.data.*
import com.inspeco.extensions.bundleOf
import com.inspeco.socket.data.SockWave
import com.inspeco.socket.udp.UdpSocket
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_home.*
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


class P1Model : ViewModel() {
    private val TAG = "bobopro-P1Model"

    private var udpSocket: UdpSocket? = null
    private lateinit var context : Context
    private lateinit var p1 : P1
    private lateinit var x1 : X1

    // wave data
    //val mpWave: PublishSubject<SockWave> = PublishSubject.create()
    val mpP1WaveInfo: PublishSubject<Int> = PublishSubject.create()
    val mpWaveGraph: PublishSubject<Int> = PublishSubject.create()

    var lastWave : SockWave? = null

    public var audioPlayer: AudioTrack? = null
    //var waveSignalBuf = ShortArray(512)
    var wavRecorder: WaveRecorder? = null
    var recordingCnt = 0
    var dBFileName :String? = null

    var realDBArray: ArrayList<Float> = arrayListOf()
    var avrDBArray: ArrayList<Float> = arrayListOf()

    override fun onCleared() {
        Log.d(TAG, "## P1Model - onCleared() called!!")
        super.onCleared()
    }

    var fileRealDb = ""
    var viewUpdateCount = 0
    var isStopRealdB = false


    fun initP1Model(aContext: Context) {
        context = aContext
        p1 = P1.getInstance()
        x1 = X1.getInstance()
        p1.x1 = x1;
        x1.p1 = p1;

    }


    fun connectP1Socket() {
        val ip = "192.168.7.1"
        val port = 8080
        closeP1Socket()
        Thread(Runnable {
            Log.i(TAG, "connectP1Socket: Connect P1")
            udpSocket = UdpSocket(p1Handler, ip, port)
            udpSocket!!.sendMessageTarget(ip, port, "connected")
        }).start()
    }


    fun closeP1Socket() {
        if (udpSocket != null) {
            Log.i(TAG, "aaa Close P1")
            udpSocket!!.close()
            udpSocket = null
        }
    }


    /**
     * 오디오 재생 시작하기
     */
    fun startPlayer() {

        audioPlayer = AudioTrack(
                AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 1024, AudioTrack.MODE_STREAM
        )

        if (audioPlayer!!.playState != AudioTrack.PLAYSTATE_PLAYING) {
            audioPlayer!!.play()
        }

    }


    /**
     * 오디오 재생 멈추기
     */
    fun stopPlayer() {
        audioPlayer?.stop()
        audioPlayer = null

    }


    /**
     * 녹음시작
     */
    fun startRecording(isMix: Boolean) {
        if (wavRecorder == null) {

            val maxCount = ((1000 / 31).toDouble() * Cfg.RecordingTime)

            recordingCnt = 0
            dBFileName = getAudioFileName(0)

            wavRecorder = WaveRecorder(true, "", 16000, isMix)
            p1.isRecording = true
            Log.v("bobopro", "startRecording $dBFileName , max:$maxCount")
        }
    }


    /**
     * 녹음 끝
     */
    fun stopRecording(dis: String?, temp: String, hum: String, realdB: String, realdBInt: String ) {
        if (wavRecorder != null) {

            dBFileName = getAudioFileName(realdBInt.toInt())
            Log.v("bobopro", "===> Stop Recording $dBFileName, $realdBInt")

            wavRecorder?.saveFileName = dBFileName+".wav"
            wavRecorder?.stopRecording(dis, temp, hum, realdB)
            wavRecorder = null
            p1.isRecording = false
        }
    }


    var p1Handler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            try {
                when (msg.what) {
                    UdpSocket.CMD_WAITING -> {

                        Log.i(TAG, "Waiting Connect P1")
                        Toast.makeText(
                                context,
                                // context.resources.getString(R.string.waiting_connect),
                                "Waiting Connect P1",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    UdpSocket.CMD_MY_IP -> {
                        val receive = msg.obj as String
                        //Log.i(TAG, "Recv UDP : $receive")
                    }
                    UdpSocket.CMD_RECV_DATA -> {
                        p1.isDeviceAttatched = true
                        val packet = msg.obj as ByteArray
                        val udp0Tcp1 = msg.arg1
                        //Log.i(TAG, "Recv UDP Data")
                        if (packet[0] == 0xF2.toByte() && packet[1] == 0xF2.toByte()) {
                            //Log.i(TAG, "Recv UDP ")

                            val wave = SockWave(packet, true, udp0Tcp1)
                            lastWave = wave
                            syncUdpPacket()  // wave.mode 0일때 Sync
                            val audioBytes = wave.wave.wave
                            val bufsize = 1024
                            if (wave.wave != null) {

                                if (!p1.arrangeMode) {

                                    if (!p1.isMute) {
                                        System.arraycopy(audioBytes, 0, p1.waveAudio,0,1024)

//                                val time2 = System.currentTimeMillis();
//                                val iDiff = time2-p1.lastWaveTime
//                                Log.e(TAG, " play Diff ${iDiff}");
                                        // audioPlayer?.write(wave.wave.wave, 0, 1024)

                                        audioPlayer?.write(wave.wave.wave, 0, 1024)

                                    }
                                    p1.lastWaveTime = System.currentTimeMillis()
                                    val payload = wave.wave
                                    p1.db = payload.rev_db
                                    p1.db_f = (p1.db / 100).toFloat()
                                    p1.db_str = payload.dbStr
                                    p1.ondo = payload.temp
                                    p1.humi = payload.humi
                                    p1.vol = payload.vol
                                    p1.sen = payload.sens
                                    p1.mode = payload.mode

                                    //Log.i(TAG, "Recv p1 db: ${p1.db})")

                                    if (p1.isRecording) {
                                        wavRecorder?.writeAudioDataToFile(audioBytes, bufsize)
                                        recordingCnt++

                                        var divider = 31
                                        // Log.v("bobopro", "recording $p1.db_f")
                                        // info { "jtyoo db = " + db}
                                        realDBArray.add(p1.db_f)
                                        if (recordingCnt >= ((1000 / divider).toDouble() * Cfg.RecordingTime) ) {
                                            var dis = Cfg.p1_dist

                                            var sRealDb = getRealDb()


                                            //Log.v("bobopro", "recording $sRealDb, $fileRealDb")

                                            stopRecording(
                                                    String.format("%05.2f", dis),
                                                    String.format("%04.1f", p1.ondo),
                                                    String.format("%04.1f", p1.humi),
                                                    sRealDb, fileRealDb,
                                            )
                                        }
                                        //Log.v("bobopro", "recording ${p1.db_f}")
                                    }
                                    viewUpdateCount++
//
                                    calcAvrPeak()

                                    ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN)
                                            .asShortBuffer().get(
                                                    p1.waveSignal
                                            )

                                    Fft.addBuf()

                                    if ( States.curView == Consts.VIEW_HOME) {
                                        mpWaveGraph.onNext(1)
                                        if (viewUpdateCount % 3 == 0) { // 1초에 33번 정도 들어오는데 3번에 1번만 업데이트 함
                                            mpP1WaveInfo.onNext(1)
                                        }
                                    }

                                } // is not arrange mode

                            } // endif wave != null
                        }
                        this.removeMessages(UdpSocket.CMD_RECV_DATA)
                    } // end when CMD_RECV_DATA
                    UdpSocket.CMD_TIME_OUT -> {
                        Log.e(TAG, "CMD_TIME_OUT");
                        p1.isDeviceAttatched = false
                        connectP1Socket()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }




        } // end handle Message
    }


    private fun calcAvrPeak() {

        // 계산 설정 Max 30초
        if ( avrDBArray.size>(31*30)-1 ) {
            avrDBArray.removeAt(0)
        }
        avrDBArray.add(p1.db_f)
        val bufSize = avrDBArray.size
        var sum = 0f
        p1.peakDb = 0f

        var avrCount = Cfg.p1_avrDbSec*30
        var peakCount = Cfg.p1_maxDbSec*30
        if (avrCount<30) avrCount = 30
        if (peakCount<30) peakCount = 30

        if (bufSize<avrCount) {
            avrCount = bufSize
        }
        if (bufSize<peakCount) {
            peakCount = bufSize
        }

        // 평균 계산
        var iMin = bufSize - avrCount
        if (iMin<0) iMin = 0
        for ( i in bufSize-1 downTo iMin ) {
            sum += avrDBArray[i]
        }


        p1.alarmCnt = 0
        if (Cfg.p1_alram_set == true) {
            if (Cfg.p1_alram_checksec>0) {
                val alarmSetMaxCount = Cfg.p1_alram_checksec * 29
                var cnt = 0;
                for ( i in bufSize-1 downTo iMin ) {
                    if (cnt<alarmSetMaxCount) {
                        if ((Cfg.p1_alram_db) < avrDBArray[i].toInt()) {
                            p1.alarmCnt++
                        }
                    } else break
                    cnt++
                }
            }
        }

//        if (bufSize>1) {
//            Log.d(TAG, "${avrDBArray[bufSize-1]}")
//        }

        p1.avrDb = sum / avrCount

        // 맥스 계산
        for ( i in bufSize-1 downTo (bufSize-peakCount) ) {
            if (avrDBArray[i]> p1.peakDb) {
                p1.peakDb = avrDBArray[i]
            }
        }

        for ( i in 0 until bufSize-1) {
            sum += avrDBArray[i]

            if (avrDBArray[i]> p1.peakDb) {
                p1.peakDb = avrDBArray[i]
            }
        }

//        if ((viewUpdateCount % 3)==0) {
//            Log.d(TAG, "${p1.avrDb} / $bufSize")
//        }
    }



    /* 리얼 디비 계산하기*/
    private fun getRealDb(): String {
        var realDb = "0"
        try {
            val aver : ArrayList<Float> = arrayListOf()
            var divider = 32
            for (i in 0 until 10) {
                var a = 0.0
                var end = Math.min(divider, realDBArray.size)
                for (j in 0 until end) {
                    a += realDBArray[j]
                }
                aver.add(a.toFloat() / divider)
                for (j in 0 until end) {
                    realDBArray.removeAt(0)
                }
            }
            // 최대, 최소 삭제
            var maxPos = aver.indexOf(Collections.max(aver))
            aver.removeAt(maxPos)
            var minPos = aver.indexOf(Collections.min(aver))
            aver.removeAt(minPos)
            var averFloat = 0.0
            for (i in 0 until aver.size) {
                averFloat += aver[i]
            }
            realDb = String.format("%05.2f", (averFloat / aver.size).toFloat())
            fileRealDb = String.format("%.0f", (averFloat / aver.size).toFloat())
            //info { "jtyoo realDb = " + realDb }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        realDBArray.clear()
        return realDb
    }

    /* 리얼 디비 계산하기*/
//    private fun getRealDbInt(): String {
//        var realDb = "0"
//        try {
//            val aver : ArrayList<Float> = arrayListOf()
//            var divider = 32
//            for (i in 0 until 10) {
//                var a = 0.0
//                var end = Math.min(divider, realDBArray.size)
//                for (j in 0 until end) {
//                    a += realDBArray[j]
//                }
//                aver.add(a.toFloat() / divider)
//                for (j in 0 until end) {
//                    realDBArray.removeAt(0)
//                }
//            }
//            // 최대, 최소 삭제
//            var maxPos = aver.indexOf(Collections.max(aver))
//            aver.removeAt(maxPos)
//            var minPos = aver.indexOf(Collections.min(aver))
//            aver.removeAt(minPos)
//            var averFloat = 0.0
//            for (i in 0 until aver.size) {
//                averFloat += aver[i]
//            }
//            realDb = String.format("%.0f", (averFloat / aver.size).toFloat())
//            //info { "jtyoo realDb = " + realDb }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        realDBArray.clear()
//        return realDb
//    }


    /**
     * sock wave  장비로 데이터 보냄
     */
    fun sendControlData(sockWaveData: SockWave?) {
        Thread(Runnable {
            if (udpSocket != null) {
                try {
//                  error { "sens = ${wave!!.wave.sens} , freq = ${wave!!.wave.freq} , vol = ${wave!!.wave.vol} , mode = ${wave!!.wave.mode}" }
                    if (sockWaveData != null) {
                        udpSocket!!.sendMessageTarget(sockWaveData.getClientPacket(false, false))
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "sendControlData P1 Error : ${e.localizedMessage}")
                }
            }
        }).start()
    }

    fun sendControlDataFreq(value:Int) {
        if (lastWave!= null) {
            sendControlData(
                    SockWave(
                            value, // freq
                            lastWave!!.wave.sens,
                            lastWave!!.wave.mode,
                            lastWave!!.wave.vol,
                            lastWave!!.wave.lang
                    )
            )

            Handler().postDelayed({
                sendControlData(
                        SockWave(
                                value, // freq
                                lastWave!!.wave.sens,
                                lastWave!!.wave.mode,
                                lastWave!!.wave.vol,
                                lastWave!!.wave.lang
                        )
                )
            }, 420)
        }
    }


    fun sendControlDataVolume(value:Int) {
        if (lastWave!= null) {
            sendControlData(
                    SockWave(
                            lastWave!!.wave.freq,
                            lastWave!!.wave.sens,
                            lastWave!!.wave.mode,
                            value,
                            lastWave!!.wave.lang
                    )
            )

            Handler().postDelayed({
                sendControlData(
                        SockWave(
                                lastWave!!.wave.freq,
                                lastWave!!.wave.sens,
                                lastWave!!.wave.mode,
                                value,
                                lastWave!!.wave.lang
                        )
                )
            }, 420)
        }
    }



    private fun syncUdpPacket() {
        if (lastWave!!.wave.mode != 0) {
            sendControlData(
                SockWave(
                    lastWave!!.wave.freq,
                    lastWave!!.wave.sens,
                    0,
                    lastWave!!.wave.vol,
                    lastWave!!.wave.lang
                )
            )
        }
    }



    /**
     * 로케이션 매니져
     */
    var locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            if (location!=null) {
                p1.fLati = location?.latitude!!.toFloat()
                p1.fLongi =  location?.longitude!!.toFloat()
                p1.lati = "N " + (String.format("%.6f", p1.fLati))
                p1.longi = "E " + (String.format("%.6f", p1.fLongi))
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String?) {}
        override fun onProviderDisabled(provider: String?) {}
    }



    /**
     * 네트워크 상태 체크를 위한 브로드캐스트 리시버
     */
    var wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            //val wifiInfo = wifiManager.connectionInfo as WifiInfo
            //val sSID = wifiInfo.ssid
            if (intent.action == WifiManager.RSSI_CHANGED_ACTION) {
                wifiManager.startScan()

                if (wifiManager.connectionInfo == null) {
                    p1.wifiLevel = 0
                    p1.isDeviceAttatched = false
                } else {
                    val newRssi = wifiManager.connectionInfo.rssi
                    p1.wifiLevel = WifiManager.calculateSignalLevel(newRssi, 5)
                }
            }
        }
    }



}