package com.inspeco.X1.SettingView

import android.app.Dialog
import android.content.Context
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.inspeco.X1.R
import com.inspeco.data.States
import com.inspeco.data.stringFromFloatAuto
import com.inspeco.ui.InputFilterMinMax
import kotlinx.android.synthetic.main.d_sec_edit.*


/**
 * Edit sec 다이얼로그
 */
class EditSecDialog(context: Context, value: Int, maxStr: String) : Dialog(context) {
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
        setContentView(R.layout.d_sec_edit)

        var sPer = value.toString()
        var maxInt = maxStr.toInt()

        itemEdit.filters = arrayOf<InputFilter>(InputFilterMinMax("0", "999"))
        itemEdit.setText(sPer)
        itemEdit.setSelection(sPer.length);
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(itemEdit, 0);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        saveButton.setOnClickListener {
            if (itemEdit.text!!.isEmpty()) {
                States.dialogInt = 1
            } else {
                States.dialogInt = itemEdit.text.toString().toInt()
            }
            if (States.dialogInt>maxInt) {
                val toast = Toast.makeText(context,"최대 시간은 "+maxStr+"초 입니다.", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            } else {
                hideKeyboard()
                dismiss()
                saveClick?.onClick(it)
            }
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