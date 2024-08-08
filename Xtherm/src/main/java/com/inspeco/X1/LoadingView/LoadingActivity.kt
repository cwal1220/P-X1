package com.inspeco.X1.LoadingView

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.net.NetworkInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.inspeco.X1.HomeActivity

import com.inspeco.X1.R
import com.inspeco.data.*
import com.inspeco.dialog.PaletteDialog
import com.serenegiant.usb.USBMonitor
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_loading.*
import java.lang.Exception
import java.util.ArrayList

class LoadingActivity : AppCompatActivity() {

    private var mUSBMonitor: USBMonitor? = null
    private val TAG = "bobopro-Loading"
    private lateinit var p1 : P1
    private lateinit var x1 : X1
    private lateinit var wifiConfiguration: WifiConfiguration
    private var wifiSelectDialog:WifiSelectDialog? = null

    private fun tedPermission() {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {}
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {

                Snackbar.make(homeView, "설정에서 권한을 허가 해주세요", Snackbar.LENGTH_LONG).show();

                finish()
            }
        }

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("서비스 사용을 위해서 몇가지 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 설정할 수 있습니다.")
                .setPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                )
                .check()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        tedPermission()


        p1 = P1.getInstance()
        x1 = X1.getInstance()
        States.webCamState = 0
        States.ondoCamState = 0

        mUSBMonitor = USBMonitor(this, mOnDeviceConnectListener)

        Log.i("bobopro", "MODEL = " + Build.MODEL)
        States.modelNO = Build.MODEL

        val filter = IntentFilter()
//        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
//        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION)
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)            // 와이파이 상태
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)            // AP 리스트 검색
//        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)
//        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)                    // 와이파이 활성화
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION)                            // 안테나 감도 변경
        registerReceiver(wifiReceiver, filter)

        val app = getPackageManager().getPackageInfo(this.getPackageName(), 0)
        buildNoText.text = "ver. ${app.versionName}"

        SsidText.visibility = View.GONE

    }


    /**
     * View onResume
     */
    override fun onResume() {
        super.onResume()
        Log.w(TAG, "onResume()")
        if (!mUSBMonitor!!.isRegistered) {
            Log.w(TAG, "USBMonitor() register")
            mUSBMonitor!!.register()
        }

        val mUsbDeviceList = mUSBMonitor!!.deviceList
        for (device in mUsbDeviceList) {
            checkMyDevicePermission(device)
        }


        Handler().postDelayed({
            checkDevices()
        }, 2000)

    }


    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "onDestroy()")
        try {
            unregisterReceiver(wifiReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * View onStop
     */
    override fun onStop() {
        super.onStop()
        Log.w(TAG, "onStop()")

        if (mUSBMonitor!!.isRegistered) {
            Log.w(TAG, "USBMonitor() unregister")
            mUSBMonitor!!.unregister()
        }

    }


    /**
     * 장비 체크 하고 창 뛰움...
     */
    fun checkDevices() {
        Log.w(TAG, "체크 디바이스")
        SsidText.text = States.SSID
        SsidText.visibility = View.VISIBLE
        checkSSID2()

        //showProfileMenuDialog()
    }

    fun showProfileMenuDialog() {
        val dialog = ProfileMenuDialog(this)
        dialog.setSel01Listener {
            Log.w(TAG, "프로필 선택")
            var intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()

        }
        dialog.setSel02Listener {
            Log.w(TAG, "게스트 모드")
            var intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        dialog.show()
    }


    /**
     * 네트워크 상태 체크를 위한 BroadcastReceiver
     */
    val wifiReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo as WifiInfo

            States.SSID = wifiInfo.ssid.replace("\"", "")
            Log.i(TAG, "Wifi onReceive Stat SSID: ${States.SSID}")

            if (action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) { // WiFi 스캔 완료되었을 때

                Log.d("bobopro", "스캔 완료 되었을때 WIFI SCAN_RESULTS_AVAILABLE_ACTION")

            } else if (action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
                val info = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                val state = info.detailedState
                if (state == NetworkInfo.DetailedState.CONNECTED ) {
                    Log.d("bobopro", "네트웍에 연결되었을 때 NETWORK_STATE_CHANGED_ACTION CheckSSID")
                    if (States.isWifiConnecting) {
                        checkSSID2()
                    }
                }

            }
        }
    }


    /**
     * Wifi 스캔
     */
    fun scanWifi() {
        Log.i(TAG, "scanWifi()")

        val wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val success = wifiManager.startScan()

        var list = arrayListOf<ScanResult>()
        list = wifiManager.scanResults as ArrayList<ScanResult>

        val wifiList = mutableListOf<ScanResult>()

        list.forEach {
            if (it.SSID.contains(Consts.WifiName) ) {
                Log.i(TAG, "wifi Item : ${it.SSID},  ${it.centerFreq0}, ${it.centerFreq1}, ${it.channelWidth}, ${it.level} ")
                wifiList.add(it)
            }
        }

        if (wifiSelectDialog == null) {
            wifiSelectDialog = WifiSelectDialog(this, wifiList)
        } else {
            wifiSelectDialog!!.setWifiList(wifiList)
        }

        if (!wifiSelectDialog!!.isShowing) {
            wifiSelectDialog!!.show()

        }
        // wifi List Ui
        Handler().postDelayed({
            wifiSelectDialog!!.showProgress(View.GONE)
        }, 1000)

    }


    /**
     * 연결된 wifi ssid 확인
     */
    fun checkSSID2() {
        // Log.i(TAG, "checkSSID SSID : ${States.SSID}")
        States.isP1WifiConnected = false
        States.isWifiConnecting = false
        val sSID = States.SSID
        // ssid 확인
        if (sSID.contains(Consts.WifiName) ) {
            SsidText.text = States.SSID
            States.isP1WifiConnected = true
            showProfileMenuDialog()
        } else {
            if (States.isWifiConnecting) {
                SsidText.text = "Connecting..."
                // 연결 중이니까 기다리자..
            } else {
                Log.i(TAG, "Scan wifi")
                scanWifi()
            }
        }
    }


    /**
     * 와이파이 연결
     */
    fun connectWifi(wifi: ScanResult, pw:String) {
        Log.i("bobopro", "connectWifi()")

        // 장비 연결중 ui 보이도록
        // processing.onNext(MsgCode.Connecting)
        //
        States.isWifiConnecting = true
        SsidText.text = "Connecting..."

        Log.i("bobopro", "connectWifi - {$wifi.capabilities}")

        var sSSid = wifi.SSID
        var networkId = -1
        if (wifi.capabilities.contains("WPA")) {
            Log.i("bobopro", "connectWifi - WPA  {$sSSid}")
            networkId = connectWifiForWPA(wifi.SSID, pw)
        }

        //info{ "network Id = $networkId"}
        if (networkId == -1) {// WiFi 연결 실패시
            // 장비찾기 ui 보임
            //processing.onNext(MsgCode.Detect)
            // 연결 실패 ui 보임
            //message.onNext(MsgCode.E003)
            States.isWifiConnecting = false
        }
    }


    private fun connectWifiForWPA(ssid: String, password: String): Int {
        wifiConfiguration = WifiConfiguration()
        wifiConfiguration.SSID = "\"" + ssid + "\""
        wifiConfiguration.status = WifiConfiguration.Status.DISABLED
        wifiConfiguration.priority = 40
        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP)
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
        wifiConfiguration.preSharedKey = "\"" + password + "\""
        return connecting()
    }


    /**
     * Connecting
     */
    private fun connecting(): Int {
        Log.i("bobopro", "connecting wifi()")
        val wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        // 설정된 네트워크 목록 가져오기
        val configList = wifiManager.configuredNetworks
        var isConfigured = false

        var networkId = -1

        for (wifiConfig in configList) {
            Log.d("bobopro" ,"wifiConfig.SSID:"+wifiConfig.SSID)
            if (wifiConfig.SSID == wifiConfiguration.SSID) {
                networkId = wifiConfig.networkId
                isConfigured = true
                break
            }
        }
        // 설정되지 않았다면 wfc 값으로 설정하여 추가합니다.
        if (!isConfigured) {
            networkId = wifiManager.addNetwork(wifiConfiguration)
        }

        if (networkId != -1) {
            wifiManager.disconnect()
            Log.d("bobopro" ,"is connect:" + wifiManager.enableNetwork(networkId, true) )
        }

        return networkId
    }




    /**
     * USB 디바이스 연결
     */
    fun getProductName(device: UsbDevice) : String {
        var name = device.productName
        if (name == null) { name = "Full HD New" }
        return name
    }

    fun checkMyDevicePermission(device: UsbDevice) {
        var name = getProductName(device)
        if ( name.contains("Full")) {
            States.deviceWebCam = device
            x1.isDeviceAttatched = true
        } else if ( name.contains("Xmodule")) {
            States.deviceTempCam = device
            x1.isDeviceAttatched = true

        }
    }

    private val mOnDeviceConnectListener: USBMonitor.OnDeviceConnectListener = object : USBMonitor.OnDeviceConnectListener {
        override fun onAttach(device: UsbDevice) {
            checkMyDevicePermission(device)
        }

        override fun onDettach(device: UsbDevice) {
            var name = getProductName(device)
            Log.e(TAG, "onDettach: $name")
            if ( name.contains("Full")) {
                States.webCamState= Consts.DEVICE_DETTATCHED
                States.deviceWebCam = null
            } else if ( name.contains("Xmodule")) {
                x1.isDeviceAttatched = false
                States.ondoCamState= Consts.DEVICE_DETTATCHED
                States.deviceTempCam = null
            }
        }

        override fun onConnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock, createNew: Boolean) {
            var name = getProductName(device)
            Log.w(TAG, "onConnect: $name")
        }

        override fun onDisconnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock) {
            var name = getProductName(device)
            Log.w(TAG, "onDisconnect: $name")
        }

        override fun onCancel(device: UsbDevice) {
            var name = getProductName(device)
            Log.e(TAG, "onCancel: $name")
        }
    }

}