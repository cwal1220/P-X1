package com.inspeco.X1.StatusJudgView

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import com.inspeco.X1.R
import com.inspeco.data.Consts
import com.inspeco.data.States
import com.inspeco.data.stringFromFloatAuto
import kotlinx.android.synthetic.main.d_ondo_edit.*


/**
 * Ondo  다이얼로그
 */
class EditOndoDialog(context: Context, var ondo: Float) : Dialog(context) {
    private val TAG = "bobopro - Ondo Edit Dialog"
    private var saveClick: View.OnClickListener? = null

    /**
     * 초기화
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setContentView(R.layout.d_ondo_edit)

        var sDistance = stringFromFloatAuto(ondo)

        itemEdit.setText(sDistance)
        itemEdit.setSelection(sDistance.length)

        fun hideKeyboard() {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            //inputMethodManager.showSoftInput(itemEdit, 0);
            inputMethodManager.hideSoftInputFromWindow(itemEdit.windowToken, 0)
        }

        saveButton.setOnClickListener {
            if (itemEdit.text!!.isEmpty()) {
                States.diagBaseOndo = 0f
            } else {
                States.diagBaseOndo = itemEdit.text.toString().toFloat()
            }
            hideKeyboard()
            dismiss()
            saveClick?.onClick(it)
        }


    }


    fun hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //inputMethodManager.showSoftInput(itemEdit, 0);
        inputMethodManager.hideSoftInputFromWindow(itemEdit.windowToken, 0)
    }


    fun setSaveClickListener(clickListener: View.OnClickListener?) {
        this.saveClick = clickListener
    }


}