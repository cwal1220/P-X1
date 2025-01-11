package com.inspeco.X1.ReportView

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
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
import kotlinx.android.synthetic.main.activity_report_result_mix.reportView
import kotlinx.android.synthetic.main.activity_report_result_mix.view.dateLabel
import kotlinx.android.synthetic.main.activity_report_result_mix.view.distanceLabel
import kotlinx.android.synthetic.main.activity_report_result_mix.view.eqipmentLabel
import kotlinx.android.synthetic.main.activity_report_result_mix.view.faultLabel
import kotlinx.android.synthetic.main.activity_report_result_mix.view.gpsLabel
import kotlinx.android.synthetic.main.activity_report_result_mix.view.humiLabel
import kotlinx.android.synthetic.main.activity_report_result_mix.view.lineNameLabel
import kotlinx.android.synthetic.main.activity_report_result_mix.view.materialLabel
import kotlinx.android.synthetic.main.activity_report_result_mix.view.ondoLabel
import kotlinx.android.synthetic.main.activity_report_result_mix.view.poleNoLabel
import kotlinx.android.synthetic.main.activity_report_result_mix.view.resultIcon
import kotlinx.android.synthetic.main.activity_report_result_mix.view.resultIcon1
import kotlinx.android.synthetic.main.activity_report_result_mix.view.resultIcon2
import kotlinx.android.synthetic.main.activity_report_result_mix.view.resultMsg
import kotlinx.android.synthetic.main.activity_report_result_mix.view.resultPicture
import kotlinx.android.synthetic.main.activity_report_result_mix.view.resultPicture2
import kotlinx.android.synthetic.main.activity_report_result_mix.view.resultText1A
import kotlinx.android.synthetic.main.activity_report_result_mix.view.resultText1B
import kotlinx.android.synthetic.main.activity_report_result_mix.view.resultText2A
import kotlinx.android.synthetic.main.activity_report_result_mix.view.resultText2B
import kotlinx.android.synthetic.main.activity_report_result_mix.view.voltLabel
import kotlinx.android.synthetic.main.activity_report_result_mix.view.weatherLabel
import kotlinx.android.synthetic.main.activity_report_result_ondo.resultIcon
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFClientAnchor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream

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
        try {
            resultIcon.text = "MPL" + (5-udr!!.level.toInt())
        } catch (e: Exception) {
            resultIcon.text = ""
        }


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
//            resultIcon.text = States.reportMixResult.name
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
                saveXlsFile()
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

    private fun saveXlsFile() {
        val workbook = XSSFWorkbook()

        // Sheet 생성
        val sheet = workbook.createSheet("Sheet1")

        // Sheet에서 현재 셀의 높이와 너비를 변경
        sheet.defaultRowHeight = 450
        sheet.setColumnWidth(0, 2750)
        sheet.setColumnWidth(1, 2200)
        sheet.setColumnWidth(2, 2750)
        sheet.setColumnWidth(3, 2200)
        sheet.setColumnWidth(4, 2750)
        sheet.setColumnWidth(5, 2200)

        // Row와 Cell 생성
        val numRows = 22
        val numCols = 6
        for (rowIndex in 0 until numRows) {
            val row = sheet.createRow(rowIndex)
            for (colIndex in 0 until numCols) {
                val cell = row.createCell(colIndex)
                cell.cellStyle = workbook.createCellStyle().apply {
//                    borderTop = BorderStyle.THIN
//                    borderBottom = BorderStyle.THIN
//                    borderLeft = BorderStyle.THIN
//                    borderRight = BorderStyle.THIN
                }

            }
        }

        // 폰트
        val topHeaderFont = workbook.createFont().apply {
            bold = true
            fontHeight = 350
        }

        // 폰트
        val headerFont = workbook.createFont().apply {
            bold = true
        }

        // 폰트
        val levelResultFont = workbook.createFont().apply {
            bold = true
            color = IndexedColors.RED.getIndex()
        }

        // 폰트
        val contentsFont = workbook.createFont().apply {
            fontHeight = 200
        }

        // 최상단 제목셀 스타일
        val topHeaderCellStyle = workbook.createCellStyle().apply {
//            borderTop = BorderStyle.THIN
//            borderBottom = BorderStyle.THIN
//            borderLeft = BorderStyle.THIN
//            borderRight = BorderStyle.THIN
//            alignment = HorizontalAlignment.CENTER
//            verticalAlignment = VerticalAlignment.CENTER
            wrapText = true
        }
        topHeaderCellStyle.setFont(topHeaderFont)

        // 제목셀 스타일
        val headerCellStyle = workbook.createCellStyle().apply {
//            borderTop = BorderStyle.THIN
//            borderBottom = BorderStyle.THIN
//            borderLeft = BorderStyle.THIN
//            borderRight = BorderStyle.THIN
//            alignment = HorizontalAlignment.CENTER
//            verticalAlignment = VerticalAlignment.TOP
            wrapText = true
        }
        headerCellStyle.setFont(headerFont)

        // 레벨 결과 셀 스타일
        val levelResultCellStyle = workbook.createCellStyle().apply {
//            borderTop = BorderStyle.NONE
//            alignment = HorizontalAlignment.CENTER
//            verticalAlignment = VerticalAlignment.CENTER
//            borderTop = BorderStyle.NONE
        }
        levelResultCellStyle.setFont(levelResultFont)

        // 컨텐츠 셀 스타일
        val contentCellStyle = workbook.createCellStyle().apply {
//            borderTop = BorderStyle.THIN
//            borderBottom = BorderStyle.THIN
//            borderLeft = BorderStyle.THIN
//            borderRight = BorderStyle.THIN
//            verticalAlignment = VerticalAlignment.CENTER
            wrapText = true
        }
        // 0행: 제목
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 7))
        sheet.getRow(0).height = 600
        sheet.getRow(0).getCell(1).setCellValue(resources.getString(R.string.Multiplex_Diagnosis_Report))
        sheet.getRow(0).getCell(1).cellStyle = topHeaderCellStyle

        // 1행: 날짜, 선로명, 전주번호
        sheet.addMergedRegion(CellRangeAddress(1, 1, 0, 1))
        sheet.addMergedRegion(CellRangeAddress(1, 1, 2, 3))
        sheet.addMergedRegion(CellRangeAddress(1, 1, 4, 5))
        sheet.getRow(1).getCell(0).cellStyle = headerCellStyle
        sheet.getRow(1).getCell(0).setCellValue(resources.getString(R.string.Date))
        sheet.getRow(1).getCell(2).cellStyle = headerCellStyle
        sheet.getRow(1).getCell(2).setCellValue(resources.getString(R.string.Line_Name))
        sheet.getRow(1).getCell(4).cellStyle = headerCellStyle
        sheet.getRow(1).getCell(4).setCellValue(resources.getString(R.string.Pole_Number))

        // 2행: 내용
        sheet.addMergedRegion(CellRangeAddress(2, 2, 0, 1))
        sheet.addMergedRegion(CellRangeAddress(2, 2, 2, 3))
        sheet.addMergedRegion(CellRangeAddress(2, 2, 4, 5))
        sheet.getRow(1).getCell(0).cellStyle = contentCellStyle
        sheet.getRow(2).getCell(0).setCellValue(reportView.dateLabel.text.toString())
        sheet.getRow(1).getCell(2).cellStyle = contentCellStyle
        sheet.getRow(2).getCell(2).setCellValue(reportView.lineNameLabel.text.toString())
        sheet.getRow(1).getCell(4).cellStyle = contentCellStyle
        sheet.getRow(2).getCell(4).setCellValue(reportView.poleNoLabel.text.toString())

        // 3행: 공백
        sheet.addMergedRegion(CellRangeAddress(3, 3, 0, 7))

        // 4행: 기자재종류, 날씨
        sheet.getRow(4).getCell(0).cellStyle = headerCellStyle
        sheet.getRow(4).getCell(0).setCellValue(resources.getString(R.string.Kind_of_Equipment))
        sheet.getRow(4).getCell(1).cellStyle = headerCellStyle
        sheet.getRow(4).getCell(1).setCellValue("1")
        sheet.getRow(4).getCell(2).cellStyle = contentCellStyle
        sheet.getRow(4).getCell(2).setCellValue(reportView.eqipmentLabel.text.toString())
        sheet.getRow(4).getCell(4).cellStyle = headerCellStyle
        sheet.getRow(4).getCell(4).setCellValue(resources.getString(R.string.Weather))
        sheet.getRow(4).getCell(5).cellStyle = contentCellStyle
        sheet.getRow(4).getCell(5).setCellValue(reportView.weatherLabel.text.toString())

        // 5행: 설비재질, 온도
        sheet.getRow(5).getCell(0).cellStyle = headerCellStyle
        sheet.getRow(5).getCell(0).setCellValue(resources.getString(R.string.Equipment_Material))
        sheet.getRow(5).getCell(1).cellStyle = headerCellStyle
        sheet.getRow(5).getCell(1).setCellValue("2")
        sheet.getRow(5).getCell(2).setCellValue(reportView.materialLabel.text.toString())
        sheet.getRow(5).getCell(4).cellStyle = headerCellStyle
        sheet.getRow(5).getCell(4).setCellValue(resources.getString(R.string.Temerature))
        sheet.getRow(5).getCell(5).cellStyle = contentCellStyle
        sheet.getRow(5).getCell(5).setCellValue(reportView.ondoLabel.text.toString())

        // 6행: 설비전압, 습도
        sheet.getRow(6).getCell(0).cellStyle = headerCellStyle
        sheet.getRow(6).getCell(0).setCellValue(resources.getString(R.string.Equipment_Voltage))
        sheet.getRow(6).getCell(1).cellStyle = headerCellStyle
        sheet.getRow(6).getCell(1).setCellValue("3")
        sheet.getRow(6).getCell(2).cellStyle = contentCellStyle
        sheet.getRow(6).getCell(2).setCellValue(reportView.voltLabel.text.toString())
        sheet.getRow(6).getCell(4).cellStyle = headerCellStyle
        sheet.getRow(6).getCell(4).setCellValue(resources.getString(R.string.Humidity))
        sheet.getRow(6).getCell(5).cellStyle = contentCellStyle
        sheet.getRow(6).getCell(5).setCellValue(reportView.humiLabel.text.toString())

        // 7행: 설비거리, GPS
        sheet.getRow(7).getCell(0).cellStyle = headerCellStyle
        sheet.getRow(7).getCell(0).setCellValue(resources.getString(R.string.Distance))
        sheet.getRow(7).getCell(1).cellStyle = headerCellStyle
        sheet.getRow(7).getCell(1).setCellValue("4")
        sheet.getRow(7).getCell(2).cellStyle = contentCellStyle
        sheet.getRow(7).getCell(2).setCellValue(reportView.distanceLabel.text.toString())
        sheet.getRow(7).getCell(4).cellStyle = headerCellStyle
        sheet.getRow(7).getCell(4).setCellValue("GPS")
        sheet.getRow(7).getCell(5).cellStyle = contentCellStyle
        sheet.getRow(7).getCell(5).setCellValue(reportView.gpsLabel.text.toString())

        // 8행: 불량유형
        sheet.getRow(8).getCell(0).cellStyle = headerCellStyle
        sheet.getRow(8).getCell(0).setCellValue(resources.getString(R.string.Kind_of_Defect))
        sheet.getRow(8).getCell(1).cellStyle = headerCellStyle
        sheet.getRow(8).getCell(1).setCellValue("5")
        sheet.getRow(8).getCell(2).cellStyle = contentCellStyle
        sheet.getRow(8).getCell(2).setCellValue(reportView.faultLabel.text.toString())

        // 9행: 공백
        sheet.addMergedRegion(CellRangeAddress(9, 9, 0, 7))

        // 10행 ~ 13행: 사진
        // 사진1
        try {
            val bitmap1 = (reportView.resultPicture.drawable as BitmapDrawable).bitmap
            val stream1 = ByteArrayOutputStream()
            bitmap1.compress(Bitmap.CompressFormat.PNG, 100, stream1)
            val imageBytes1 = stream1.toByteArray()
            val pictureIdx1 = workbook.addPicture(imageBytes1, Workbook.PICTURE_TYPE_PNG)
            val drawing1 = sheet.createDrawingPatriarch()
            val anchor1 = XSSFClientAnchor(0, 0, 0, 0, 0, 10, 3, 14)
            val picture1 = drawing1.createPicture(anchor1, pictureIdx1)
        } catch (e: Exception) {
        }


        // 사진2
        try {
            val bitmap2 = (reportView.resultPicture2.drawable as BitmapDrawable).bitmap
            val stream2 = ByteArrayOutputStream()
            bitmap2.compress(Bitmap.CompressFormat.PNG, 100, stream2)
            val imageBytes2 = stream2.toByteArray()
            val pictureIdx2 = workbook.addPicture(imageBytes2, Workbook.PICTURE_TYPE_PNG)
            val drawing2 = sheet.createDrawingPatriarch()
            val anchor2 = XSSFClientAnchor(0, 0, 0, 0, 3, 10, 6, 14)
            val picture2 = drawing2.createPicture(anchor2, pictureIdx2)
        } catch (e: Exception) {
        }

        // 14행: 공백
        sheet.addMergedRegion(CellRangeAddress(14, 14, 0, 7))

        // 15행 ~ 20행: 진단결과
        sheet.addMergedRegion(CellRangeAddress(15, 20, 0, 0))
        sheet.getRow(15).getCell(0).cellStyle = headerCellStyle
        sheet.getRow(15).getCell(0).setCellValue(resources.getString(R.string.Diagnosis_Result))

        // 15행: 진단 결과 단어
        sheet.addMergedRegion(CellRangeAddress(15, 15, 1, 7))
        sheet.getRow(15).getCell(1).cellStyle = contentCellStyle
        sheet.getRow(15).getCell(1).setCellValue(reportView.resultIcon.text.toString())

        // 16행: 결과 메세지
        sheet.addMergedRegion(CellRangeAddress(16, 16, 1, 7))
        sheet.getRow(16).getCell(1).cellStyle = contentCellStyle
        sheet.getRow(16).getCell(1).setCellValue(reportView.resultMsg.text.toString())

        // 17행: resultText1A
        sheet.getRow(17).getCell(1).setCellValue(reportView.resultIcon1.text.toString())
        sheet.addMergedRegion(CellRangeAddress(17, 17, 2, 7))
        sheet.getRow(17).getCell(2).cellStyle = contentCellStyle
        sheet.getRow(17).getCell(1).setCellValue(reportView.resultText1A.text.toString())

        // 18행: resultText1B
        sheet.addMergedRegion(CellRangeAddress(18, 18, 2, 7))
        sheet.getRow(18).getCell(2).cellStyle = contentCellStyle
        sheet.getRow(18).getCell(2).setCellValue(reportView.resultText1B.text.toString())

        // 19행: resultText2A
        sheet.getRow(19).getCell(1).setCellValue(reportView.resultIcon2.text.toString())
        sheet.addMergedRegion(CellRangeAddress(19, 19, 2, 7))
        sheet.getRow(19).getCell(2).cellStyle = contentCellStyle
        sheet.getRow(19).getCell(2).setCellValue(reportView.resultText2A.text.toString())

        // 20행: resultText2B
        sheet.addMergedRegion(CellRangeAddress(20, 20, 2, 7))
        sheet.getRow(20).getCell(2).cellStyle = contentCellStyle
        sheet.getRow(20).getCell(2).setCellValue(reportView.resultText2B.text.toString())

        try {
            val mFileOutStream = FileOutputStream(docFolder!!.absolutePath + "/" + getFileName(Consts.DOCUMENT_FOLDER, "xlsx") + ".xlsx")
            workbook.write(mFileOutStream)

            mFileOutStream.flush()
            mFileOutStream.close()

            runOnUiThread {
                val msg = getResources().getString(R.string.File_has_been_saved)
                val toast = Toast.makeText(this, msg, Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }

        } catch (e: Exception) {
            val msgStr = "Error!\n" + e.toString()
            val toast = Toast.makeText(this, msgStr, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
            toast.show()

        }
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