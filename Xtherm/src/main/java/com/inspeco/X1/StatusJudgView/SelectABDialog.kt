package com.inspeco.X1.StatusJudgView

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import com.inspeco.X1.R
import com.inspeco.data.Consts
import com.inspeco.data.States
import kotlinx.android.synthetic.main.d_select_ab.*


/**
 * Select AB 다이얼로그
 */
class SelectABDialog(context: Context, var inspectOndoType: Int) : Dialog(context) {
    private val TAG = "bobopro-SelectABDialog"
//    private var sel01Click: View.OnClickListener? = null
//    private var sel02Click: View.OnClickListener? = null
    private var saveClick: View.OnClickListener? = null

    private val userList: MutableList<String> = mutableListOf<String>()
    /**
     * 초기화
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setContentView(R.layout.d_select_ab)

        setButtonColor()

        if (inspectOndoType==0) {
            inspectOndoType = Consts.Diag_3Sang
        }

        selectAButton.setOnClickListener {
            inspectOndoType = Consts.Diag_3Sang
            setButtonColor()
        }

        selectBButton.setOnClickListener {
            inspectOndoType = Consts.Diag_OndoPattern
            setButtonColor()
        }

        saveButton.setOnClickListener {
            States.diagOndoType=inspectOndoType
            dismiss()
            saveClick?.onClick(it)
        }

    }

    private fun setButtonColor() {
        if (inspectOndoType == Consts.Diag_3Sang) {
            selectAButton.setBackgroundResource(R.drawable.shape_next_yellow_button)
            selectBButton.setBackgroundResource(R.drawable.shape_next_grey_button)
        } else {
            selectAButton.setBackgroundResource(R.drawable.shape_next_grey_button)
            selectBButton.setBackgroundResource(R.drawable.shape_next_yellow_button)
        }
    }

    fun setSaveClickListener(clickListener: View.OnClickListener?) {
        this.saveClick = clickListener
    }


//    fun setSel01Listener(sel01: View.OnClickListener) {
//        this.sel01Click = sel01
//    }
//
//    fun setSel02Listener(sel02: View.OnClickListener?) {
//        this.sel02Click = sel02
//    }


}