package com.inspeco.X1.StatusJudgView

import android.app.Dialog
import android.content.Context
import android.os.Environment

import android.view.View
import android.view.Window
import androidx.recyclerview.widget.*
import com.inspeco.X1.R
import com.inspeco.data.*
import com.inspeco.ui.ItemDragListener
import com.inspeco.ui.ItemTouchHelperCallback
import kotlinx.android.synthetic.main.d_select_ondo_list.*
import java.io.File
import java.util.*

/**
 * Ondo List 다이얼로그
 */
class OndoListDialog(context: Context) : Dialog(context) {

    private val TAG = "bobopro- EquipmentList Dialog"
    private lateinit var ondoListAdapter : OndoListAdapter
    private var saveClick: View.OnClickListener? = null
    private var oldIndex = 0

    var list = mutableListOf<Float>()

    /**
     * 기자재 선택 Dialog 초기화
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        //setCanceledOnTouchOutside(false)
        setCancelable(true)
        setContentView(R.layout.d_select_ondo_list)

        list.clear()

//        States.diagOndoList.clear()
//        States.diagOndoList.add(10.0f)
//        States.diagOndoList.add(20.2f)
//        States.diagOndoList.add(30.0f)
//        States.diagOndoList.add(40.5f)
//
        // 리스트 추가
        States.diagOndoList.forEach {
            list.add(it)
        }


        if (list.size>0) {
            oldIndex = 0
            States.diagSelectOndoIdx = 0
            States.diagSelectOndo = list[0]
            val sOndo = stringFromFloatAuto( States.diagSelectOndo)
            itemEdit.setText(sOndo)
            itemEdit.setSelection(sOndo.length)
        }


        ondoListAdapter = OndoListAdapter(list, context )
        ondoListAdapter.setItemClickListener {
            //States.diagImageFile = it
            if (States.diagSelectOndoIdx != oldIndex) {
                if (itemEdit.text!!.isEmpty()) {
                    list[oldIndex] = 0f
                } else {
                    list[oldIndex] = itemEdit.text.toString().toFloat()
                }
                oldIndex = States.diagSelectOndoIdx
            }
            val sOndo = stringFromFloatAuto( States.diagSelectOndo)
            itemEdit.setText(sOndo)
            itemEdit.setSelection(sOndo.length)
        }


        saveButton.setOnClickListener {
            if (itemEdit.text!!.isEmpty()) {
                list[oldIndex] = 0f
            } else {
                list[oldIndex] = itemEdit.text.toString().toFloat()
            }

            States.diagOndoList.clear()
            list.forEach {
                States.diagOndoList.add(it)
            }


            dismiss()
            saveClick?.onClick(it)
        }

        listView.adapter = ondoListAdapter


    }


    fun setSaveClickListener(itemClick: View.OnClickListener) {
        this.saveClick = itemClick
    }



}