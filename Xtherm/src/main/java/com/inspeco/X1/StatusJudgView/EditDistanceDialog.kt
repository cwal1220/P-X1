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
import kotlinx.android.synthetic.main.d_distance_edit.*
import kotlinx.android.synthetic.main.d_distance_edit.itemEdit
import kotlinx.android.synthetic.main.d_new_user.*


/**
 * Edit Distance 다이얼로그
 */
class EditDistanceDialog(context: Context, var distance: Float) : Dialog(context) {
    private val TAG = "bobopro- Distance Edit Dialog"
    private var saveClick: View.OnClickListener? = null

    /**
     * 초기화
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setContentView(R.layout.d_distance_edit)

        var sDistance = stringFromFloatAuto(distance)

        itemEdit.setText(sDistance)
        itemEdit.setSelection(sDistance.length);
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(itemEdit, 0);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


        saveButton.setOnClickListener {
            if (itemEdit.text!!.isEmpty()) {
                States.dialogFloat = 0f
            } else {
                States.dialogFloat = itemEdit.text.toString().toFloat()
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