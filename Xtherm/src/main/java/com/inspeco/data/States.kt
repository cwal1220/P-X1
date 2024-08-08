package com.inspeco.data

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.usb.UsbDevice
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usbcameracommon.UVCCameraHandler

@SuppressLint("StaticFieldLeak")
object States {

    var mCameraHandler: UVCCameraHandler? = null

    var deviceWebCam: UsbDevice? = null
    var deviceTempCam: UsbDevice? = null
    var mainContext: Context? = null
    var webCamCtrlBlock: USBMonitor.UsbControlBlock? = null
    var tempCamCtrlBlock: USBMonitor.UsbControlBlock? = null

    var webCamState = 0
    var ondoCamState = 0

    var curView = 0

    var screenWidth = 0
    var screenHeight = 0
    var densityDpi = 0

    var modelNO = ""
    var SSID = ""
    var isWifiConnecting = false
    var isP1WifiConnected = false

    var diagGubun = 0
    var diagPage = 0
    var diagOndoType = 0  // 열화상 판정방법
    var diagFacility = 0  //설비
    var diagVolt = 0f  //볼트
    var diagEquipment = EquipmentData() // 기자재
    var diagBaseOndo = 0f  // 진단 기준온도
    var diagFaulty = ConditionData()  // 불량유형
    var diagMaterial = MaterialData()
    var diagDistance = 12.5f
    var diagTargetOndo = 0f


    var diagLati = 0f
    var diagLongi = 0f
    var diagOndo = 0f
    var diagHumi = 0f

    var diagSelectOndoIdx = 0
    var diagSelectOndo = 0f
    var diagOndoList = mutableListOf<Float>()

    var diagImageFile = FileData()

    var diagWaveInfo = WaveFileInfo()

    var diagFileData = FileData()

    var diagPlResult = DiagData()
    var diagMixResult = DiagData()
    var diagOndoResult = DiagData()

    var dialogString = ""
    var dialogInt = 0
    var dialogFloat = 0f
    var isReportFile = false
    var reportDate = ""
    var reportLineName = ""
    var reportMemo = ""
    var reportPoleNo = ""
    var reportWeather = "-"
    var reportImageFile = FileData()
    var reportImageFile2 = FileData()
    var reportPlResult = DiagData()
    var reportMixResult = DiagData()
    var reportOndoResult = DiagData()

    var webCamInitCounter = 0
    var ondoCamInitCounter = 0

    var isRecording = false
    var isViewResumePass = false


    var diagResult4List: ArrayList<DiagData> = arrayListOf<DiagData>()
//    var diagResult4List: Array<DiagData> = arrayOf<DiagData>(
//            DiagData( id=1, name="양호", msg="1년 이내 재점검"),
//            DiagData( id=2, name="열화가능성", msg="한달 이내 재점검"),
//            DiagData( id=3, name="추후 결함", msg="일주일 이내 재점검"),
//            DiagData( id=4, name="결함", msg="즉시 교체"),
//    )

    var diagResult3List: ArrayList<DiagData> = arrayListOf<DiagData>()
//    var diagResult3List: Array<DiagData> = arrayOf<DiagData>(
//            DiagData( id=1, name="양호", msg="1년 이내 재점검"),
//            DiagData( id=2, name="주의", msg="3개월 이내 재점검"),
//            DiagData( id=3, name="불량", msg="즉시 교체"),
//    )

    var diagMixMatrix: Array<DiagMixResultData> = arrayOf<DiagMixResultData>(
            DiagMixResultData(5, 1, 1, 1-1),
            DiagMixResultData(6, 1, 1, 1-1),
            DiagMixResultData(4, 1, 1, 2-1),
            DiagMixResultData(3, 1, 1, 2-1),
            DiagMixResultData(4, 2, 2, 2-1),
            DiagMixResultData(3, 2, 2, 2-1),
            DiagMixResultData(6, 0, 2, 2-1),
            DiagMixResultData(5, 0, 2, 2-1),
            DiagMixResultData(6, 2, 3, 2-1),
            DiagMixResultData(5, 2, 3, 2-1),
            DiagMixResultData(2, 1, 1, 2-1),
            DiagMixResultData(2, 0, 2, 2-1),
            DiagMixResultData(1, 1, 1, 3-1),
            DiagMixResultData(1, 0, 2, 3-1),
            DiagMixResultData(5, 3, 4, 3-1),
            DiagMixResultData(6, 3, 4, 3-1),
            DiagMixResultData(2, 2, 3, 3-1),
            DiagMixResultData(2, 3, 4, 3-1),
            DiagMixResultData(1, 2, 3, 3-1),
            DiagMixResultData(4, 3, 3, 3-1),
            DiagMixResultData(3, 3, 3, 3-1),
            DiagMixResultData(4, 0, 4, 3-1),
            DiagMixResultData(3, 0, 4, 3-1),
            DiagMixResultData(1, 3, 4, 4-1),
    )

}