package com.inspeco.X1.ReportView

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.inspeco.X1.R
import com.inspeco.X1.StatusJudgView.ImageListDialog
import com.inspeco.data.*
import kotlinx.android.synthetic.main.activity_report_result_mix.view.dateLabel
import kotlinx.android.synthetic.main.activity_report_result_ondo.*
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFClientAnchor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.time.LocalDate

class ReportOndoResultActivity : AppCompatActivity() {


    private var udr:UDR? = null
    private var report:Report? = null
    private var docFolder: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_result_ondo)

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

        // 열화상 진단에서는 열화로 고정   faultLabel.text = udr!!.faultTypeStr
        faultLabel.text = getResources().getString(R.string.Defective_content_Coment)


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
            resultIcon.text = States.reportOndoResult.name
            resultMsg.text = States.reportOndoResult.msg
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
            resultText1A.text = udr!!.ondoStr1
            resultText1B.text = udr!!.ondoStr2 +"    "+udr!!.ondoStr3
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
            val dialog = InputDialog(this,msg, States.reportWeather)
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

            if (saveRepFile(report!!, Consts.REPORT_TEMP_FOLDER)) {
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

                        if (saveRepAsFile(report!!, Consts.REPORT_TEMP_FOLDER, States.dialogString)) {
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
        sheet.defaultRowHeight = 600
        sheet.setColumnWidth(0, 2750)
        sheet.setColumnWidth(1, 2200)
        sheet.setColumnWidth(2, 2750)
        sheet.setColumnWidth(3, 2200)
        sheet.setColumnWidth(4, 2750)
        sheet.setColumnWidth(5, 2200)
        sheet.setColumnWidth(6, 2750)
        sheet.setColumnWidth(7, 2200)

        // Row와 Cell 생성
        val numRows = 28
        val numCols = 8
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
        // 제목
        sheet.addMergedRegion(CellRangeAddress(0, 0, 1, 6))
        sheet.getRow(0).height = 600
        sheet.getRow(0).getCell(1).setCellValue(resources.getString(R.string.Thermal_Image_Diagnosis_Report))
        sheet.getRow(0).getCell(1).cellStyle = topHeaderCellStyle

        // 선로명 , 전주번호, 라인, 전압
        sheet.getRow(1).height = 600
        sheet.getRow(1).getCell(0).setCellValue(reportView.dateLabel.text.toString())
//        sheet.getRow(1).getCell(0).cellStyle = headerCellStyle
//        sheet.getRow(1).getCell(1).setCellValue(edt_line_name.text.toString())
//        sheet.getRow(1).getCell(1).cellStyle = contentCellStyle
//        sheet.getRow(1).getCell(2).setCellValue(resources.getString(R.string.pole_no))
//        sheet.getRow(1).getCell(2).cellStyle = headerCellStyle
//        sheet.getRow(1).getCell(3).setCellValue(edt_pole_no.text.toString())
//        sheet.getRow(1).getCell(3).cellStyle = contentCellStyle
//        sheet.getRow(1).getCell(4).setCellValue(resources.getString(R.string.line))
//        sheet.getRow(1).getCell(4).cellStyle = headerCellStyle
//        sheet.getRow(1).getCell(5).setCellValue(txt_line.text.toString())
//        sheet.getRow(1).getCell(5).cellStyle = contentCellStyle
//        sheet.getRow(1).getCell(6).setCellValue(resources.getString(R.string.kv))
//        sheet.getRow(1).getCell(6).cellStyle = headerCellStyle
//        sheet.getRow(1).getCell(7).setCellValue(edt_kv.text.toString())
//        sheet.getRow(1).getCell(7).cellStyle = contentCellStyle
//
//        // 측량거리, 온도, 습도, real db
//        sheet.getRow(2).height = 600
//        sheet.getRow(2).getCell(0).setCellValue(resources.getString(R.string.distance))
//        sheet.getRow(2).getCell(0).cellStyle = headerCellStyle
//        sheet.getRow(2).getCell(1).setCellValue(txt_distance.text.toString())
//        sheet.getRow(2).getCell(1).cellStyle = contentCellStyle
//        sheet.getRow(2).getCell(2).setCellValue(resources.getString(R.string.temperature))
//        sheet.getRow(2).getCell(2).cellStyle = headerCellStyle
//        sheet.getRow(2).getCell(3).setCellValue(txt_temperature.text.toString())
//        sheet.getRow(2).getCell(3).cellStyle = contentCellStyle
//        sheet.getRow(2).getCell(4).setCellValue(resources.getString(R.string.humidity))
//        sheet.getRow(2).getCell(4).cellStyle = headerCellStyle
//        sheet.getRow(2).getCell(5).setCellValue(txt_humidity.text.toString())
//        sheet.getRow(2).getCell(5).cellStyle = contentCellStyle
//        sheet.getRow(2).getCell(6).setCellValue(resources.getString(R.string.real_db))
//        sheet.getRow(2).getCell(6).cellStyle = headerCellStyle
//        sheet.getRow(2).getCell(7).setCellValue(txt_real_db.text.toString())
//        sheet.getRow(2).getCell(7).cellStyle = contentCellStyle
//
//        // 기자재 종류, 불량 유형
//        sheet.addMergedRegion(CellRangeAddress(3, 3, 1, 3))
//        sheet.addMergedRegion(CellRangeAddress(3, 3, 5, 7))
//        sheet.getRow(3).height = 600
//        sheet.getRow(3).getCell(0).setCellValue(resources.getString(R.string.kinds_of_equipment))
//        sheet.getRow(3).getCell(0).cellStyle = headerCellStyle
//        sheet.getRow(3).getCell(1).setCellValue(txt_equipment.text.toString())
//        sheet.getRow(3).getCell(1).cellStyle = contentCellStyle
//        sheet.getRow(3).getCell(4).setCellValue(resources.getString(R.string.conditions_of_equipment))
//        sheet.getRow(3).getCell(4).cellStyle = headerCellStyle
//        sheet.getRow(3).getCell(5).setCellValue(txt_condigion.text.toString())
//        sheet.getRow(3).getCell(5).cellStyle = contentCellStyle
//
//        // 불량유형
//        sheet.addMergedRegion(CellRangeAddress(4, 4, 1, 7))
//        sheet.getRow(4).height = 1200
//        sheet.getRow(4).getCell(0).setCellValue(resources.getString(R.string.bad_content))
//        sheet.getRow(4).getCell(0).cellStyle = headerCellStyle
//        sheet.getRow(4).getCell(1).setCellValue(txt_bad_content.text.toString())
//        sheet.getRow(4).getCell(1).cellStyle = contentCellStyle
//
//        // 웨이브폼 제목
//        sheet.addMergedRegion(CellRangeAddress(5, 5, 0, 7))
//        sheet.getRow(5).height = 450
//        sheet.getRow(5).getCell(0).setCellValue("Waveform")
//        sheet.getRow(5).getCell(0).cellStyle = headerCellStyle
//
//        // 웨이브폼 이미지
//        sheet.addMergedRegion(CellRangeAddress(6, 6, 0, 7))
//        sheet.getRow(6).height = 1200
//
//        if (!TextUtils.isEmpty(detectionScreenPhotoPath) && File(detectionScreenPhotoPath).exists()) {
//            // 이미지 파일 읽어들이기
//            val inputStream: InputStream = FileInputStream(detectionScreenPhotoPath)
//
//            val bytes = IOUtils.toByteArray(inputStream)
//            val pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG)
//
//            // 이미지를 넣을 위치 설정
//            val drawing = sheet.createDrawingPatriarch()
//            val anchor = XSSFClientAnchor(0, 0, 0, 0, 0, 6, 8, 7)
//            val picture = drawing.createPicture(anchor, pictureIdx)
//        }
//
//        // 레벨 제목
//        sheet.addMergedRegion(CellRangeAddress(7, 7, 0, 7))
//        sheet.getRow(7).height = 450
//        sheet.getRow(7).getCell(0).setCellValue(resources.getString(R.string.priority_level))
//        sheet.getRow(7).getCell(0).cellStyle = headerCellStyle
//
//        // 레벨 내용
//        sheet.addMergedRegion(CellRangeAddress(8, 8, 0, 7))
//        sheet.getRow(8).height = 600
//        sheet.getRow(8).getCell(0).setCellValue("Priority Level [ " + txt_level.text.toString() + " ]")
//        sheet.getRow(8).getCell(0).cellStyle = headerCellStyle
//        sheet.addMergedRegion(CellRangeAddress(9, 9, 0, 7))
//        sheet.getRow(9).height = 600
//        sheet.getRow(9).getCell(0).setCellValue(txt_result.text.toString())
//        sheet.getRow(9).getCell(0).cellStyle = levelResultCellStyle
//
//        // 전주 전경사진 제목
//        sheet.addMergedRegion(CellRangeAddress(10, 10, 0, 3))
//        sheet.getRow(10).height = 450
//        sheet.getRow(10).getCell(0).setCellValue("Photo of Pole") // R.string.detection_screen
//        sheet.getRow(10).getCell(0).cellStyle = headerCellStyle
//
//        // 기자재 제목
//        sheet.addMergedRegion(CellRangeAddress(10, 10, 4, 7))
//        sheet.getRow(10).getCell(4).setCellValue("Photo of Equipment")
//        sheet.getRow(10).getCell(4).cellStyle = headerCellStyle
//
//        // 전주 전경사진
//        sheet.addMergedRegion(CellRangeAddress(11, 26, 0, 3))
//        if (!TextUtils.isEmpty(polePhotoPath) && File(polePhotoPath).exists()) {
//            // 이미지 파일 읽어들이기
//            val bitmap = (img_pole.drawable as BitmapDrawable).bitmap
//            val stream = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//            val bytes = stream.toByteArray()
//            val pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG)
//
//            // 이미지를 넣을 위치 설정
//            val drawing = sheet.createDrawingPatriarch()
//            val anchor = XSSFClientAnchor(0, 0, 0, 0, 0, 11, 4, 27)
//            val picture = drawing.createPicture(anchor, pictureIdx)
//        }
//
//        // 기자재 사진
//        sheet.addMergedRegion(CellRangeAddress(11, 26, 4, 7))
//        if (!TextUtils.isEmpty(equipmentPhotoPath) && File(equipmentPhotoPath).exists()) {
//            // 이미지 파일 읽어들이기
//            val bitmap = (img_equipment.drawable as BitmapDrawable).bitmap
//            val stream = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//            val bytes = stream.toByteArray()
//
//            val pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG)
//
//            // 이미지를 넣을 위치 설정
//            val drawing = sheet.createDrawingPatriarch()
//            val anchor = XSSFClientAnchor(0, 0, 0, 0, 4, 11, 8, 27)
//            val picture = drawing.createPicture(anchor, pictureIdx)
//        }

        sheet.addMergedRegion(CellRangeAddress(27, 27, 0, 7))
        val onlyDate: LocalDate = LocalDate.now()
        sheet.getRow(27).height = 400
        sheet.getRow(27).getCell(0).setCellValue(onlyDate.toString())
        sheet.getRow(27).getCell(0).cellStyle = headerCellStyle


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
            val toast = Toast.makeText(this,"파일이 없습니다.", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
            toast.show()
        }
    }



}