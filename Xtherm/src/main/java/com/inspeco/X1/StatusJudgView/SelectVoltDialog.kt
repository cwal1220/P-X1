package com.inspeco.X1.StatusJudgView

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import com.inspeco.X1.R
import com.inspeco.data.Consts
import com.inspeco.data.States
import com.inspeco.data.stringFromFloatAuto
import kotlinx.android.synthetic.main.d_distance_edit.*
import kotlinx.android.synthetic.main.d_new_user.*
import kotlinx.android.synthetic.main.d_select_volt.*
import kotlinx.android.synthetic.main.d_select_volt.itemEdit
import kotlinx.android.synthetic.main.d_select_volt.saveButton


/**
 * Select Volt 다이얼로그
 */
class SelectVoltDialog(context: Context, var diagFacility: Int, diagVolt: Float) : Dialog(context) {
    private val TAG = "bobopro-SelectVolt Dialog"
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
        setContentView(R.layout.d_select_volt)

        if (diagFacility == 0) {
            diagFacility = Consts.Diag_FacilitySend
        }

        var sVolt = stringFromFloatAuto(diagVolt)
        itemEdit.setText(sVolt)
        itemEdit.setSelection(sVolt.length);
        States.diagVolt = itemEdit.text.toString().toFloat()



        setButtonColor()

        selectAButton.setOnClickListener {
            diagFacility = Consts.Diag_FacilitySend
            setButtonColor()
        }

        selectBButton.setOnClickListener {
            diagFacility = Consts.Diag_FacilitySupply
            setButtonColor()
        }

        selectCButton.setOnClickListener {
            diagFacility = Consts.Diag_FacilityTrans
            setButtonColor()
        }

        saveButton.setOnClickListener {
            States.diagFacility=diagFacility
            States.diagVolt = itemEdit.text.toString().toFloat()
            hideKeyboard()
            dismiss()
            saveClick?.onClick(it)
        }

        selectVolt0Button.setOnClickListener {
            hideKeyboard()
            if (diagFacility == Consts.Diag_FacilitySend ) {
                itemEdit.setText("4")
            } else {
                itemEdit.setText("33")
            }
        }
        selectVolt1Button.setOnClickListener {
            hideKeyboard()
            if (diagFacility == Consts.Diag_FacilitySend ) {
                itemEdit.setText("11")
            } else {
                itemEdit.setText("66")
            }
        }
        selectVolt2Button.setOnClickListener {
            hideKeyboard()
            if (diagFacility == Consts.Diag_FacilitySend ) {
                itemEdit.setText("22.9")
            } else {
                itemEdit.setText("110")
            }
        }
        selectVolt3Button.setOnClickListener {
            hideKeyboard()
            if (diagFacility == Consts.Diag_FacilitySend) {
                itemEdit.setText("33")
            } else {
                itemEdit.setText("150")
            }
        }
        selectVolt4Button.setOnClickListener {
            hideKeyboard()
            itemEdit.setText("175")
        }
        selectVolt5Button.setOnClickListener {
            hideKeyboard()
            itemEdit.setText("765")
        }

    }

    fun hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //inputMethodManager.showSoftInput(itemEdit, 0);
        inputMethodManager.hideSoftInputFromWindow(itemEdit.windowToken, 0)
    }

    private fun setButtonColor() {
        hideKeyboard()
        if (diagFacility == Consts.Diag_FacilitySend) {
            selectAButton.setBackgroundResource(R.drawable.shape_next_yellow_button)
            selectBButton.setBackgroundResource(R.drawable.shape_next_grey_button)
            selectCButton.setBackgroundResource(R.drawable.shape_next_grey_button)
            selectVolt4Button.visibility = View.GONE
            selectVolt5Button.visibility = View.GONE
            selectVolt0Button.text = "4kV"
            selectVolt1Button.text = "11kV"
            selectVolt2Button.text = "22.9kV"
            selectVolt3Button.text = "33kV"

        } else if (diagFacility == Consts.Diag_FacilitySupply) {
            selectAButton.setBackgroundResource(R.drawable.shape_next_grey_button)
            selectBButton.setBackgroundResource(R.drawable.shape_next_yellow_button)
            selectCButton.setBackgroundResource(R.drawable.shape_next_grey_button)
            selectVolt4Button.visibility = View.VISIBLE
            selectVolt5Button.visibility = View.VISIBLE
            selectVolt0Button.text = "33kV"
            selectVolt1Button.text = "66kV"
            selectVolt2Button.text = "110kV"
            selectVolt3Button.text = "150kV"
            selectVolt4Button.text = "175kV"
            selectVolt5Button.text = "765kV"
        } else {
            selectAButton.setBackgroundResource(R.drawable.shape_next_grey_button)
            selectBButton.setBackgroundResource(R.drawable.shape_next_grey_button)
            selectCButton.setBackgroundResource(R.drawable.shape_next_yellow_button)
            selectVolt4Button.visibility = View.VISIBLE
            selectVolt5Button.visibility = View.VISIBLE
            selectVolt0Button.text = "33kV"
            selectVolt1Button.text = "66kV"
            selectVolt2Button.text = "110kV"
            selectVolt3Button.text = "150kV"
            selectVolt4Button.text = "175kV"
            selectVolt5Button.text = "765kV"
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