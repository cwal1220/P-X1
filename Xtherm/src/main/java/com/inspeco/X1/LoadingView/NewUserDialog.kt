package com.inspeco.X1.LoadingView

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import com.inspeco.X1.R
import com.inspeco.data.Cfg
import kotlinx.android.synthetic.main.d_distance_edit.*
import kotlinx.android.synthetic.main.d_new_user.*
import kotlinx.android.synthetic.main.d_new_user.itemEdit


/**
 * 기본 다이얼로그
 */class NewUserDialog(context: Context, title: String) : Dialog(context) {

    private var confirmClick: View.OnClickListener? = null
    private var cancelClick: View.OnClickListener? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(false)
        setCancelable(true)

        setContentView(R.layout.d_new_user)
        itemEdit.setText(title)
        itemEdit.setSelection(title.length);

        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(itemEdit, 0);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


        confirmButton.setOnClickListener {

            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            Cfg.userName = itemEdit.text.toString()
            this.dismiss()

            confirmClick?.onClick(it)
        }

        cancelButton.setOnClickListener {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            this.dismiss()
            cancelClick?.onClick(it)
        }

    }

    /**
     * ok 리스너 등록
     */
    fun setConfirmListener(okClick: View.OnClickListener) {
        this.confirmClick = okClick
    }

    /**
     * no 리스너 등록
     */
    fun setCancelListener(cancelClick: View.OnClickListener?) {
        this.cancelClick = cancelClick
    }
}