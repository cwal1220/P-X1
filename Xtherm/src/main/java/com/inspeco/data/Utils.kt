package com.inspeco.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment

import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.gson.Gson
//import com.google.gson.Gson
import org.apache.commons.io.comparator.LastModifiedFileComparator
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

///**
// * 팝업다이얼로그1
// */
//fun showPopupDlg(
//    context: Context, desc: String,
//    ok: String, okClick: View.OnClickListener,
//    cancel: String?, cancelClick: View.OnClickListener?
//): CommonDialog {
//
//    val commonDialog = CommonDialog(context, desc, ok, cancel)
//    commonDialog.setOkListener(okClick)
//    commonDialog.setCancelListener(cancelClick)
//
//    commonDialog.show()
//
//    return commonDialog
//}
//
///**
// * 팝업다이얼로그2
// */
//fun showPopupDlg2(
//    context: Context, title:String, desc: String,
//    ok: String, okClick: View.OnClickListener,
//    cancel: String, cancelClick: View.OnClickListener?
//): CommonDialog2 {
//
//    val commonDialog = CommonDialog2(context, title, desc, ok, cancel)
//    commonDialog.setOkListener(okClick)
//    commonDialog.setCancelListener(cancelClick)
//
//    commonDialog.show()
//
//    return commonDialog
//}
//
///**
// * 시스템 다이얼로그
// */
//fun showAlertDialog(mContext:Context, title:String, msg:String , yes:String, yesClick:DialogInterface.OnClickListener, no:String?, noClick:DialogInterface.OnClickListener? ) {
//    val builder = AlertDialog.Builder(mContext)
//    builder.setTitle(title)
//    builder.setMessage(msg)
//    builder.setPositiveButton(yes, yesClick)
//    if (!TextUtils.isEmpty(no)) {
//        builder.setNegativeButton(no, noClick)
//    }
//    builder.show()
//}

/**
 * 키보드 보이기
 * @param context
 * @param et EditText
 */
fun showSoftKeyboard(context: Context, et: EditText) {
    val timer = Timer()
    timer.schedule(object : TimerTask() {
        override fun run() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(et, 0)
        }
    }, 500)
}

/**
 * 키보드 숨기기
 * @param context
 * @param view
 */
fun hideKeyboard(context: Context, view: View) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

/**
 * 어플리케이션 버전네임 가져오기
 * @param context
 * @return String versionName
 */
fun getAppVersionName(context: Context): String {
    var result = ""

    result = try {
        val manager = context.packageManager
        val info = manager.getPackageInfo(context.packageName, 0)

        info.versionName

    } catch (e: PackageManager.NameNotFoundException) {
        "1.0.0"
    }

    return result
}

/**
 * major.minor.bugfix 형태의 버전 이름 비교
 * @param me        versionName
 * @param other     versionName
 * @return
 * 음수: other이 높다. 0: 같다. 양수: me가 높다.
 */
fun versionNameCompare(me: String, other: String): Int {
    val meTokens = me.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val otherTokens = other.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    for (i in 0..2) {
        val meVersion = if (meTokens.size - 1 < i) 0 else Integer.valueOf(meTokens[i])
        val otherVersion = if (otherTokens.size - 1 < i) 0 else Integer.valueOf(otherTokens[i])
        if (meVersion - otherVersion != 0) {
            return meVersion - otherVersion
        }
    }

    return 0
}

/**
 *  밀리세컨드 타입을 지정한 포맷에 맞게 String 으로 만들어 리턴
 */
fun getStringDate(mill: Long, pattern: String) : String {
    val date = Date(mill)
    val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return dateFormat.format(date)
}

/**
 * 현재 시간으로 파일 이름 생성 및 리턴
 */
fun getFileNameForTime() : String{
    val now = System.currentTimeMillis()
    val date = Date(now)
    var sdf = SimpleDateFormat(Cfg.p1_dateStr+"_HHmmss", Locale.getDefault())

    var lang = Locale.getDefault().language


//    if (lang == "en") {
//        //sdf = SimpleDateFormat("MMddyyyy_HHmmss", Locale.getDefault())
//    } else if (lang == "zh") {
//    }

    var sDate = sdf.format(date)
    Log.d("bobopro", "Filename  {$lang} {$sDate}")

    return sdf.format(date)
}

fun getDateStr() : String{
    val now = System.currentTimeMillis()
    val date = Date(now)
    var sdf = SimpleDateFormat(Cfg.p1_dateStr2, Locale.getDefault())

    var lang = Locale.getDefault().language


//    if (lang == "en") {
//        //sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
//    } else if (lang == "zh") {
//    }

    var sDate = sdf.format(date)
    Log.d("bobopro", "Filename  {$lang} {$sDate}")

    return sdf.format(date)
}



/**
 * 이미지/동영상/Document 의 저장할 파일 이름을 생성/리턴
 */
fun getFileName(m_f:String, ext:String?): String {
    val folder = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/" + m_f)
    if (!folder.exists()) {
        folder.mkdir()
    }

    // 파일 이름 지정
    var fileNm = ""

    // 카운팅
    var cnt = 0
    val files = folder.listFiles()

    val newFiles = arrayListOf<File>()
    if (!TextUtils.isEmpty(ext)) {
        for (i in 0 until files.size) {
            if (files[i].name.contains(ext!!)) {
                // 확장자가 포함된 것만 넣음
                newFiles.add(files[i])
            }
        }
    } else {
        newFiles.addAll(files)
    }

    cnt = if (newFiles == null || newFiles.isEmpty()) {
        0
    } else {
        // 시간순 정렬
        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR)
//        newFiles.sort()
        val lastFile = newFiles[newFiles.size-1]

        val prefix = lastFile.name.substring(0, lastFile.name.indexOf("_"))

        val count = prefix.replace("[^0-9]".toRegex(), "")

        try {
            count.toInt()
        } catch (e: Exception) {
            0
        }
//        if (f == Const.SCREEN_SHOT_FOLDER) {
//            lastFile.name.substring(1, 4).toInt() // 마지막 파일의 카운팅 가져옴
//        } else {
//            lastFile.name.substring(0, 3).toInt() // 마지막 파일의 카운팅 가져옴
//        }
    }

    cnt++
    if (cnt > 999) {
        cnt = 1 // 100을 넘기면 다시 1부터
    }
    // 카운팅을 3자리 수로 만들어 파일 이름에 붙임
    fileNm += String.format("%03d", cnt)

    // 년월일시분초 를 파일 이름에 붙임
    fileNm += "_" + getFileNameForTime()

    return fileNm
}

/**
 * 오디오 저장 임시파일 삭제
 */
fun deleteAudioTempFile() {
    // 임시파일삭제
    val filepath = Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER  + "/" + Consts.AUDIO_RECORDER_FOLDER
    val tempFile = File(filepath, Consts.AUDIO_RECORDER_TEMP_FILE)
    if (tempFile.exists())
        tempFile.delete()
}

/**
 * 파일 이름 생성 및 리턴
 */
fun getScreenFileName() : String {

    // 폴더 확인
    val folder = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/" + Consts.SCREEN_SHOT_FOLDER)
    if (!folder.exists()) {
        folder.mkdir()
    }

    // db 붙임
    val fileNm =  getFileNameForTime()



    return fileNm

}




fun getAudioFileName(db:Int) : String {
    var fileNm = ""
    //var startTime =  System.currentTimeMillis()
    //deleteAudioTempFile()

    // 폴더 확인
    val folder = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/" + Consts.AUDIO_RECORDER_FOLDER)
    if (!folder.exists()) {
        folder.mkdir()
    }

    // 카운팅
//    var cnt = 0
//    val files = folder.listFiles()
//    cnt = if (files == null || files.isEmpty()) {
//        0
//    } else {
//        // 시간순 정렬
//        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR)
//        val lastFile = files[files.size-1]
//        try {
//            lastFile.name.substring(0, 3).toInt() // 마지막 파일의 카운팅 가져옴
//        } catch (e: Exception) {
//            // 잘못된 파일 이름이라 카운팅을 할 수 없을 땐 초기화
//            0
//        }
//    }
//
//    cnt++
//    if (cnt > 999) {
//        cnt = 1 // 100을 넘기면 다시 1부터
//    }
//
//    // 카운팅을 3자리 수로 만들어 파일 이름에 붙임
//    fileNm += String.format("%03d", cnt)

    // db 붙임
    fileNm =  getFileNameForTime()
    fileNm += "_" + String.format("%02d", db) + "db"

//    var diffTime =  System.currentTimeMillis() - startTime
//    Log.i("AU2","make filename Done ${diffTime}ms"  )

    return fileNm

}



/**
 * 파일로부터 UDR 클래스로 파싱하여 리턴
 */
fun getUDRFile(path: String): UDR? {

    var udr:UDR? = null

    val udrPath = path

    val dbFile = File(udrPath)

    if (dbFile.exists()) {
        // 파일이 존재하면
        try {
            val fis = FileInputStream(dbFile)
            val buffer = ByteArray(fis.available())

            fis.read(buffer)
            fis.close()

            val str = String(buffer)

            val gson = Gson()
            udr = gson.fromJson(str, UDR::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } else {
        Log.d("Utils", " 파일이 없음")
    }

    return udr
}

/**
 * 파일로부터 Report 클래스로 파싱하여 리턴
 */
fun getReportFile(path: String): Report? {

    var rep:Report? = null

    val repPath = path

    val dbFile = File(repPath)

    if (dbFile.exists()) {
        // 파일이 존재하면
        try {
            val fis = FileInputStream(dbFile)
            val buffer = ByteArray(fis.available())

            fis.read(buffer)
            fis.close()

            val str = String(buffer)

            val gson = Gson()
            rep = gson.fromJson(str, Report::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } else {
        Log.d("Utils", " 파일이 없음")
    }

    return rep
}

///**
// * UDR 파일 저장
// */
fun saveUdrFile(udr: UDR?, level:String, folder: String) :Boolean{
    val gson = Gson()
    val json = gson.toJson(udr, UDR::class.java)

    val folder = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/"+folder )
    if (!folder.exists()) {
        folder.mkdir()
    }

    // 파일 이름 지정
    var fileNm = ""

//    // 카운팅
//    var cnt = 0
//    val files = folder.listFiles()
//    cnt = if (files == null || files.isEmpty()) {
//        0
//    } else {
//        val lastFile = files[files.size-1]
//        lastFile.name.substring(0, 3).toInt() // 마지막 파일의 카운팅 가져옴
//    }
//
//    cnt++
//    if (cnt > 100) {
//        cnt = 1 // 100을 넘기면 다시 1부터
//    }
//    // 카운팅을 3자리 수로 만들어 파일 이름에 붙임
//    fileNm += String.format("%03d", cnt)
//

    // 년월일시분초 를 파일 이름에 붙임
    fileNm += getFileNameForTime()

    // 레벨 명을 파일 이름에 붙임
    fileNm += "_$level"

    // 지정된 폴더에 저장
    return try {
        val buf = BufferedWriter(FileWriter(folder.absolutePath + "/" + fileNm + ".udr", true))
        buf.append(json)
        buf.close()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

}

///**
// * Rep 파일 저장
// */
fun saveRepFile(report: Report, folder: String) :Boolean{
    val gson = Gson()
    val json = gson.toJson(report, Report::class.java)


    val folder = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/" + folder)
    if (!folder.exists()) {
        folder.mkdir()
    }

    // 파일 이름 지정
    var fileNm = ""
//
//    // 카운팅
//    var cnt = 0
//    val files = folder.listFiles()
//    cnt = if (files == null || files.isEmpty()) {
//        0
//    } else {
//        val lastFile = files[files.size-1]
//        lastFile.name.substring(0, 3).toInt() // 마지막 파일의 카운팅 가져옴
//    }
//
//    cnt++
//    if (cnt > 100) {
//        cnt = 1 // 100을 넘기면 다시 1부터
//    }
//    // 카운팅을 3자리 수로 만들어 파일 이름에 붙임
//    fileNm += String.format("%03d", cnt)

    // 년월일시분초 를 파일 이름에 붙임
    fileNm += getFileNameForTime()

    // 레벨 명을 파일 이름에 붙임
    fileNm += "_" + report.udr.level


    // 지정된 폴더에 저장
    return try {
        val buf = BufferedWriter(FileWriter(folder.absolutePath + "/" + fileNm + ".rep", true))
        buf.append(json)
        buf.close()

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

}



fun saveRepAsFile(report: Report, folder: String, fileNm: String) :Boolean{
    val gson = Gson()
    val json = gson.toJson(report, Report::class.java)

    val folder = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/" + folder)
    if (!folder.exists()) {
        folder.mkdir()
    }

    // 지정된 폴더에 저장
    return try {
        val buf = BufferedWriter(FileWriter(folder.absolutePath + "/" + fileNm + ".rep", true))
        buf.append(json)
        buf.close()

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

}
//

//

/**
 * 폴더 관련 초기화
 */
fun initFolder(f: String): File? {
    // 폴더 생성
    val filepath = Environment.getExternalStorageDirectory().path
    val rootFolder = File(filepath, Consts.ROOT_FOLDER)

    if (!rootFolder.exists()) {
        rootFolder.mkdirs()
    }

    val audioRecFolder = File(rootFolder.path, Consts.AUDIO_RECORDER_FOLDER)
    if (!audioRecFolder.exists()) {
        audioRecFolder.mkdirs()
    }

    val imgFolder = File(rootFolder.path, Consts.SCREEN_SHOT_FOLDER)
    if (!imgFolder.exists()) {
        imgFolder.mkdirs()
    }

    val videoFolder = File(rootFolder.path, Consts.REC_FOLDER)
    if (!videoFolder.exists()) {
        videoFolder.mkdirs()
    }

    val udrWaveFolder = File(rootFolder.path, Consts.UDR_WAVE_FOLDER)
    if (!udrWaveFolder.exists()) {
        udrWaveFolder.mkdirs()
    }
    val udrMixFolder = File(rootFolder.path, Consts.UDR_MIX_FOLDER)
    if (!udrMixFolder.exists()) {
        udrMixFolder.mkdirs()
    }
    val udrTempFolder = File(rootFolder.path, Consts.UDR_TEMP_FOLDER)
    if (!udrTempFolder.exists()) {
        udrTempFolder.mkdirs()
    }

    val reportWaveFolder = File(rootFolder.path, Consts.REPORT_WAVE_FOLDER)
    if (!reportWaveFolder.exists()) {
        reportWaveFolder.mkdirs()
    }
    val reportMixFolder = File(rootFolder.path, Consts.REPORT_MIX_FOLDER)
    if (!reportMixFolder.exists()) {
        reportMixFolder.mkdirs()
    }
    val reportTempFolder = File(rootFolder.path, Consts.REPORT_TEMP_FOLDER)
    if (!reportTempFolder.exists()) {
        reportTempFolder.mkdirs()
    }

    val documentFolder = File(rootFolder.path, Consts.DOCUMENT_FOLDER)
    if (!documentFolder.exists()) {
        documentFolder.mkdirs()
    }

    val waveformFolder = File(rootFolder.path, Consts.WAVEFORM_SHOT_FOLDER)
    if (!waveformFolder.exists()) {
        waveformFolder.mkdir()
    }

    when (f) {
        Consts.AUDIO_RECORDER_FOLDER -> {
            return audioRecFolder
        }
        Consts.SCREEN_SHOT_FOLDER -> {
            return imgFolder
        }
        Consts.REC_FOLDER -> {
            return videoFolder
        }
        Consts.UDR_WAVE_FOLDER -> {
            return udrWaveFolder
        }
        Consts.UDR_MIX_FOLDER -> {
            return udrMixFolder
        }
        Consts.UDR_TEMP_FOLDER -> {
            return udrTempFolder
        }
        Consts.REPORT_WAVE_FOLDER -> {
            return reportWaveFolder
        }
        Consts.REPORT_MIX_FOLDER -> {
            return reportMixFolder
        }
        Consts.REPORT_TEMP_FOLDER -> {
            return reportTempFolder
        }
        Consts.DOCUMENT_FOLDER -> {
            return documentFolder
        }

        Consts.WAVEFORM_SHOT_FOLDER -> {
            return waveformFolder
        }
        else -> {
            return null
        }
    }

}



fun stringFromFloatAuto(fValue: Float) : String {
    var sValue = ""
    val aPoint = fValue - fValue.toInt().toFloat()
    if (aPoint>0.01) {
        sValue = String.format("%.1f", fValue)
    } else {
        sValue = String.format("%.0f", fValue)
    }
    return sValue
}


fun floatFromString(sValue: String) : Float {
    if ( (sValue==null) or (sValue.isEmpty())) {
        return 0f
    } else {
        return sValue.toFloat()
    }

}

/**
 * 기준일로부터 오늘까지의 날짜 일수
 */
fun diffDate(date: Long) : Int {
    // 오늘
    val today = Date()
    val cal = Calendar.getInstance()
    cal.time = today


    // 기준일 설정.
    val cal2 = Calendar.getInstance()
    cal2.time = Date(date)


    // 기준일로 설정. month의 경우 해당월수-1을 해줍니다.
    var count = 0
    while (!cal2.after(cal))
    {
        count++
        cal2.add(Calendar.DATE, 1)


        //다음날로 바뀜
        println(
            (cal2.get(Calendar.YEAR)).toString() + "년 "
                    + (cal2.get(Calendar.MONTH) + 1)
                    + "월 " + cal2.get(Calendar.DATE) + "일"
        )
    }
    println("기준일로부터 " + count + "일이 지났습니다.")
    return count
}