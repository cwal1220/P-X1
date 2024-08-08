package com.inspeco.X1.ReportView

import android.content.Context
import android.graphics.Point
import android.graphics.pdf.PdfDocument
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.inspeco.X1.R
import com.inspeco.X1.StatusJudgView.ImageListDialog
import com.inspeco.data.*
import kotlinx.android.synthetic.main.activity_report_result_mix.*

import java.io.File
import java.io.FileOutputStream

class ReportMixResultActivity : AppCompatActivity() {

    private var udr:UDR? = null
    private var report:Report? = null
    private var docFolder: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_result_mix)

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

        ondoLabel.text = stringFromFloatAuto( Cfg.getOndoFC( udr!!.waveOndo))+"°"+Cfg.p1_cGiho
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


//        Log.d("bobopro-보고서", udr!!.imageData1.filePath)
//        Log.d("bobopro-보고서", udr!!.imageData1.fileName)

        udr!!.imageData1.filePath

        Glide.with(this).load(File(udr!!.imageData1.filePath)).into(resultPicture)

        if ((udr!!.mixResultIndex>0) && (udr!!.mixResultIndex<4)) {
            States.diagResult4List.forEach {
                if   (it.id == udr!!.mixResultIndex ) {
                    States.reportMixResult = it.copy()
                }
            }
            resultIcon.text = States.reportMixResult.name
            resultMsg.text = States.reportMixResult.msg
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
            resultIcon1.text = States.reportOndoResult.name
            resultText1A.text = udr!!.ondoStr1
            resultText1B.text = udr!!.ondoStr2 +"    "+udr!!.ondoStr3
        }


        if ((udr!!.waveResultIndex>0) && (udr!!.waveResultIndex<7)) {
            Ini.diagPLList.forEach {
                if   (it.id == udr!!.waveResultIndex ) {
                    States.reportPlResult = it.copy()
                }
            }
            resultIcon2.text = States.reportPlResult.name

            val db1 = getResources().getString(R.string.Dectected_dB)
            val db2 = getResources().getString(R.string.correction_db)

            resultText2A.text = db1 + ": "+stringFromFloatAuto(udr!!.detectionDB) + "    "+  db2 + ": "+stringFromFloatAuto(udr!!.effectiveDB)
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


        resultPicture2.setOnClickListener {
            openImage()
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

            if (saveRepFile(report!!, Consts.REPORT_MIX_FOLDER)) {
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

                        if (saveRepAsFile(report!!, Consts.REPORT_MIX_FOLDER, States.dialogString)) {
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
     * 이미지 선택창 열기
     */
    private fun openImage() {
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