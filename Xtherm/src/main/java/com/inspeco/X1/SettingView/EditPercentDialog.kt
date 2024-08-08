package com.inspeco.X1.SettingView

import android.app.Dialog
import android.content.Context
import android.text.InputFilter
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import com.inspeco.X1.R
import com.inspeco.data.States
import com.inspeco.data.stringFromFloatAuto
import com.inspeco.ui.InputFilterMinMax
import kotlinx.android.synthetic.main.d_percent_edit.*


/**
 * Edit % 다이얼로그
 */
class EditPercentDialog(context: Context, var percent: Int) : Dialog(context) {
    private val TAG = "bobopro- % Edit Dialog"
    private var saveClick: View.OnClickListener? = null



    /**
     * 초기화
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setContentView(R.layout.d_percent_edit)

        var sPer = percent.toString()

        itemEdit.filters = arrayOf<InputFilter>(InputFilterMinMax("0", "100"))
        itemEdit.setText(sPer)
        itemEdit.setSelection(sPer.length);
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(itemEdit, 0);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        saveButton.setOnClickListener {
            if (itemEdit.text!!.isEmpty()) {
                States.dialogInt = 0
            } else {
                States.dialogInt = itemEdit.text.toString().toInt()
            }
            hideKeyboard()
            dismiss()
            saveClick?.onClick(it)
        }


    }


    fun hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(itemEdit.windowToken, 0)
    }


    fun setSaveClickListener(clickListener: View.OnClickListener?) {
        this.saveClick = clickListener
    }


}