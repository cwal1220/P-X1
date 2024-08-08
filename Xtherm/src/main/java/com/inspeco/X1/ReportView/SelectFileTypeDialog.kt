package com.inspeco.X1.ReportView

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import com.inspeco.X1.R
import com.inspeco.data.Consts
import com.inspeco.data.States
import kotlinx.android.synthetic.main.d_select_filetype.*


/**
 * Select File Type 다이얼로그
 */
class SelectFileTypeDialog(context: Context) : Dialog(context) {
    private val TAG = "bobopro-SelectFileTypeDialog"
//    private var sel01Click: View.OnClickListener? = null
//    private var sel02Click: View.OnClickListener? = null
    private var saveAsClick: View.OnClickListener? = null
    private var savePdfClick: View.OnClickListener? = null
//    private var savePngClick: View.OnClickListener? = null

    /**
     * 초기화
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setContentView(R.layout.d_select_filetype)

        selectAButton.setOnClickListener {
            dismiss()
            saveAsClick?.onClick(it)
        }

        selectBButton.setOnClickListener {
            dismiss()
            savePdfClick?.onClick(it)
        }

//        selectCButton.setOnClickListener {
//            dismiss()
//            savePngClick?.onClick(it)
//        }

    }


    fun setSaveAsClickListener(clickListener: View.OnClickListener?) {
        this.saveAsClick = clickListener
    }

    fun setSavePdfClickListener(clickListener: View.OnClickListener?) {
        this.savePdfClick = clickListener
    }

//    fun setSavePngClickListener(clickListener: View.OnClickListener?) {
//        this.saveAsClick = clickListener
//    }




}