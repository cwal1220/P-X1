package com.inspeco.X1.ReportView

import android.content.Context
import android.graphics.Point
import android.graphics.pdf.PdfDocument
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.inspeco.X1.R
import com.inspeco.X1.StatusJudgView.ImageListDialog
import com.inspeco.data.*
import com.l_github.derlio.waveform.soundfile.SoundFile
import kotlinx.android.synthetic.main.activity_report_result_wave.*
import kotlinx.android.synthetic.main.activity_report_result_wave.materialLabel

import java.io.File
import java.io.FileOutputStream

class ReportWaveResultActivity : AppCompatActivity() {


    private var udr:UDR? = null
    private var report:Report? = null
    private var docFolder: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_result_wave)

        if (States.isReportFile==false) {
            States.reportDate = getDateStr()
            States.reportLineName = ""
            States.reportPoleNo = ""
            States.reportWeather = "-"
            States.reportImageFile2 = FileData()
            val msg = getResources().getString(R.string.Defective_content_Coment)
            States.reportMemo = msg
            udr = getUDRFile(States.diagFileData.filePath)
        } else{
            report = getReportFile(States.diagFileData.filePath)
            udr = report!!.udr
            States.reportDate = report!!.reportDate
            States.reportLineName = report!!.reportLineName
            States.reportPoleNo = report!!.reportPoleNo
            States.reportWeather = report!!.reportWeather
            States.reportImageFile2 = report!!.imageData2
            States.reportMemo = report!!.reportMemo
            if (States.reportImageFile2!= null ) {
                Glide.with(this).load(File(States.reportImageFile2.filePath)).into(resultPicture2)
            }
        }


        dateLabel.text = States.reportDate
        lineNameLabel.text = States.reportLineName
        poleNoLabel.text = States.reportPoleNo
        weatherLabel.text = States.reportWeather

        ondoLabel.text = stringFromFloatAuto( Cfg.getOndoFC( udr!!.waveOndo ))+"°"+Cfg.p1_cGiho
        humiLabel.text = stringFromFloatAuto(udr!!.waveHumi)+"%"

        resultText2B.text = States.reportMemo

                if (udr!!.lati>0.1) {
            gpsLabel.text = String.format("N %.5f°", udr!!.lati)+"\n" +String.format("W %.5f°", udr!!.longi)
        } else {
            gpsLabel.text = "-"
        }
        eqipmentLabel.text = udr!!.equipment
        materialLabel.text = udr!!.material
        voltLabel.text = stringFromFloatAuto(udr!!.volt)+"kV"
        distanceLabel.text = stringFromFloatAuto(udr!!.distance)+"m"
        faultLabel.text = udr!!.faultTypeStr


        if ( (udr!!.waveData != null) && (udr!!.waveData.filePath.isEmpty()==false)) {
            Log.i("bobopro", " wave Form ${udr!!.waveData.filePath}")
                var soundfile = SoundFile.create(
                        udr!!.waveData.filePath,
                        object : SoundFile.ProgressListener {
                            override fun reportProgress(fractionComplete: Double): Boolean {
                                return true
                            }
                        })
                //waveformView.
                reportWaveformView.setAudioFile(soundfile)

        } else
        {
            val msg = getResources().getString(R.string.no_file_msg)
            val toast = Toast.makeText(this, "Wave "+msg, Toast.LENGTH_SHORT)

            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
            toast.show()
        }



//        Log.d("bobopro-보고서", udr!!.imageData1.filePath)
//        Log.d("bobopro-보고서", udr!!.imageData1.fileName)

        udr!!.imageData1.filePath

        Glide.with(this).load(File(udr!!.imageData1.filePath)).into(resultPicture)

        if ((udr!!.waveResultIndex>0) && (udr!!.waveResultIndex<7)) {

            States.diagResult4List.forEach {
                if   (it.id == udr!!.mixResultIndex ) {
                    States.reportMixResult = it.copy()
                }
            }


            resultIcon.text = States.reportPlResult.name
            resultMsg.text = States.reportPlResult.msg
        }

        if ((udr!!.ondoResultIndex>0) && (udr!!.ondoResultIndex<4)) {
            if (udr!!.ondoPattern==Consts.Diag_3Sang) {
                States.diagResult3List.forEach {
                    if   (it.id == udr!!.ondoResultIndex ) {
                        States.reportOndoResult = it.copy()
                    }
                }
            } else {
                States.diagResult4List.forEach {
                    if   (it.id == udr!!.ondoResultIndex ) {
                        States.reportOndoResult = it.copy()
                    }
                }
            }

        }


        if ((udr!!.waveResultIndex>0) && (udr!!.waveResultIndex<7)) {
            Ini.diagPLList.forEach {
                if   (it.id == udr!!.waveResultIndex ) {
                    States.reportPlResult = it.copy()
                }
            }
            val db1 = getResources().getString(R.string.Dectected_dB)
            resultText2A.text = db1 + ": "+stringFromFloatAuto(udr!!.detectionDB)
                                // + "    보정 dB: "+stringFromFloatAuto(udr!!.effectiveDB)
        }




        dateLabel.setOnClickListener {
            val msg = getResources().getString(R.string.Date)
            val dialog = InputDialog(this,msg, States.reportDate)
            dialog.setConfirmListener {
                States.reportDate = States.dialogString
                dateLabel.text = States.reportDate
            }
            dialog.show()
        }

        lineNameLabel.setOnClickListener {
            val msg = getResources().getString(R.string.Line_Name)
            val dialog = InputDialog(this,msg, States.reportLineName)
            dialog.setConfirmListener {
                States.reportLineName = States.dialogString
                lineNameLabel.text = States.reportLineName
            }
            dialog.show()
        }


        resultText2B.setOnClickListener {
            val msg = getResources().getString(R.string.Defective_content_Coment)
            val dialog = InputDialog(this,msg, States.reportMemo)
            dialog.setConfirmListener {
                States.reportMemo = States.dialogString
                resultText2B.text = States.reportMemo
            }
            dialog.show()
        }

        poleNoLabel.setOnClickListener {
            val msg = getResources().getString(R.string.Pole_Number)
            val dialog = InputDialog(this,msg, States.reportPoleNo)
            dialog.setConfirmListener {
                States.reportPoleNo = States.dialogString
                poleNoLabel.text = States.reportPoleNo
            }
            dialog.show()
        }

        weatherLabel.setOnClickListener {
            val msg = getResources().getString(R.string.Weather)
            val dialog = InputDialog(this, msg, States.reportWeather)
            dialog.setConfirmListener {
                States.reportWeather = States.dialogString
                weatherLabel.text = States.reportWeather
            }
            dialog.show()
        }

        resultPicture.setOnClickListener {
            openImage1()
        }

        resultPicture2.setOnClickListener {
            openImage2()
        }

        saveReportButton.setOnClickListener {
            report = Report(
                    udr!!,
                    States.reportImageFile2,
                    States.reportDate,
                    States.reportLineName,
                    States.reportPoleNo,
                    States.reportWeather,
                    States.reportMemo
            )

            if (saveRepFile(report!!, Consts.REPORT_WAVE_FOLDER)) {
                val msg = getResources().getString(R.string.File_has_been_saved)
                val toast = Toast.makeText(this, "REP "+msg, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }
        }

        saveAsButton.setOnClickListener {
            val dialog = SelectFileTypeDialog(this)
            dialog.setSaveAsClickListener() {


                val fileNm = getFileNameForTime() + "_${udr!!.level}"

                val msg = getResources().getString(R.string.Save_as)
                val dialog = InputDialog(this, msg, fileNm)
                dialog.setConfirmListener {
                    if (States.dialogString.isEmpty()==false) {
                        report = Report(
                                udr!!,
                                States.reportImageFile2,
                                States.reportDate,
                                States.reportLineName,
                                States.reportPoleNo,
                                States.reportWeather,
                                States.reportMemo
                        )

                        if (saveRepAsFile(report!!, Consts.REPORT_WAVE_FOLDER, States.dialogString)) {
                            val msg = getResources().getString(R.string.File_has_been_saved)
                            val toast = Toast.makeText(this, "REP "+msg, Toast.LENGTH_SHORT)
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                            toast.show()
                        }
                    }

                }
                dialog.show()


            }
            dialog.setSavePdfClickListener() {
                // create a new document
                savePdfFile()
            }
            //dialog.setSavePngClickListener() {}
            dialog.show()

        }

    }

    private fun savePdfFile() {

        docFolder = initFolder(Consts.DOCUMENT_FOLDER)

        val document = PdfDocument()

        // crate a page description ( A4 Size 595 x 842 )
        val pageInfo = PdfDocument.PageInfo.Builder(1200, 1740, 1).create()

        // create a new page from the PageInfo
        val page = document.startPage(pageInfo)

//        var canvas = page.canvas
        val content = reportView
        content!!.draw(page.canvas)

        document.finishPage(page)

        try {
            val mFileOutStream = FileOutputStream(docFolder!!.absolutePath + "/" + getFileName(
                    Consts.DOCUMENT_FOLDER, "pdf") + ".pdf")
            document.writeTo(mFileOutStream)

            mFileOutStream.flush()
            mFileOutStream.close()

            runOnUiThread {
                val msg = getResources().getString(R.string.File_has_been_saved)
                val toast = Toast.makeText(this, msg, Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }

        } catch (e: Exception) {
            //info { e.toString() }
            val msgStr = "Error!\n" + e.toString()
            val toast = Toast.makeText(this, msgStr, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
            toast.show()

        }

        document.close()


    }

    /**
     * 이미지 선택창 열기, 왼쪽 사진 클릭 시
     */
    private fun openImage1() {
        val dialog = ImageListDialog(this)

        if (dialog.fileList.size > 0) {
            val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val params: ViewGroup.LayoutParams? = dialog?.window?.attributes

            params?.width = size.x - 100
            params?.height = size.y - 50
            dialog.window?.attributes = params as WindowManager.LayoutParams

            dialog.show()

            dialog.setVideoClickListener {
                States.reportImageFile = States.diagImageFile
                //Log.d("bobopro-보고서", States.diagImageFile.fileName)
                Glide.with(this).load(File(States.reportImageFile.filePath)).into(resultPicture)
                udr!!.imageData1.filePath = States.reportImageFile.filePath

            }

        } else {
            val msg = getResources().getString(R.string.no_file_msg)
            val toast = Toast.makeText(this,msg, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
            toast.show()
        }
    }
    /**
     * 이미지 선택창 열기, 오른쪽 사진 클릭 시
     */
    private fun openImage2() {
        val dialog = ImageListDialog(this)

        if (dialog.fileList.size > 0) {
            val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val params: ViewGroup.LayoutParams? = dialog?.window?.attributes

            params?.width = size.x - 100
            params?.height = size.y - 50
            dialog.window?.attributes = params as WindowManager.LayoutParams

            dialog.show()

            dialog.setVideoClickListener {
                States.reportImageFile2 = States.diagImageFile
                //Log.d("bobopro-보고서", States.diagImageFile.fileName)
                Glide.with(this).load(File(States.reportImageFile2.filePath)).into(resultPicture2)

            }

        } else {
            val msg = getResources().getString(R.string.no_file_msg)
            val toast = Toast.makeText(this,msg, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
            toast.show()
        }
    }



}