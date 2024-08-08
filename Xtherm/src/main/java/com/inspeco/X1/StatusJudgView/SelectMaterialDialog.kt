package com.inspeco.X1.StatusJudgView

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import com.inspeco.X1.R
import com.inspeco.data.Consts
import com.inspeco.data.MaterialData
import com.inspeco.data.States
import kotlinx.android.synthetic.main.d_select_abcd.*


/**
 * Select Material 다이얼로그
 */
class SelectMaterialDialog(context: Context) : Dialog(context) {
    private val TAG = "bobopro-SelectABDialog"
    private var selItemClick: View.OnClickListener? = null


    /**
     * 초기화
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setContentView(R.layout.d_select_abcd)

        var msg = context.getResources().getString(R.string.Ceramic)
        selectAButton.text = msg

        msg = context.getResources().getString(R.string.Polymer)
        selectBButton.text = msg

        msg = context.getResources().getString(R.string.Glass)
        selectCButton.text = msg

        msg = context.getResources().getString(R.string.Etc)
        selectDButton.text = msg

        selectAButton.setOnClickListener {
            val msg = context.getResources().getString(R.string.Ceramic)
            States.diagMaterial = MaterialData(Consts.Diag_MaterialCeramic, msg, 0f)
            dismiss()
            selItemClick?.onClick(it)
        }

        selectBButton.setOnClickListener {
            val msg = context.getResources().getString(R.string.Polymer)
            States.diagMaterial = MaterialData(Consts.Diag_MaterialPolymer, msg, 0f)
            dismiss()
            selItemClick?.onClick(it)
        }

        selectCButton.setOnClickListener {
            val msg = context.getResources().getString(R.string.Glass)
            States.diagMaterial = MaterialData(Consts.Diag_MaterialGlass,msg, 0f)
            dismiss()
            selItemClick?.onClick(it)
        }

        selectDButton.setOnClickListener {
            val msg = context.getResources().getString(R.string.Etc)
            States.diagMaterial = MaterialData(Consts.Diag_MaterialEtc,msg, 0f)
            dismiss()
            selItemClick?.onClick(it)
        }



    }



    fun setSelectItemListener(clickListener: View.OnClickListener?) {
        this.selItemClick = clickListener
    }


//    fun setSel01Listener(sel01: View.OnClickListener) {
//        this.sel01Click = sel01
//    }
//
//    fun setSel02Listener(sel02: View.OnClickListener?) {
//        this.sel02Click = sel02
//    }


}