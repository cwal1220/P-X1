package com.inspeco.X1.StatusJudgView

import android.app.Dialog
import android.content.Context

import android.view.View
import android.view.Window
import androidx.recyclerview.widget.*
import com.inspeco.X1.R
import com.inspeco.data.*
import kotlinx.android.synthetic.main.d_video_list.*

/**
 * Equipment List 다이얼로그
 */
class FaultListDialog(context: Context) : Dialog(context) {

    private val TAG = "bobopro- EquipmentList Dialog"
    private lateinit var faultListAdapter : FaultListAdapter
    private var itemClick: View.OnClickListener? = null

    private lateinit var path:String
    var list = mutableListOf<ConditionData>()


    /**
     * 기자재 선택 Dialog 초기화
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        //setCanceledOnTouchOutside(false)
        setCancelable(true)
        setContentView(R.layout.d_select_fault_type)

        // 리스트 추가
        Ini.conditionList.forEach {
            list.add(it)
        }

        faultListAdapter = FaultListAdapter(list, context )
        faultListAdapter.setItemClickListener {
                //States.diagImageFile = it
                dismiss()
                itemClick?.onClick(null)

        }

        listView.adapter = faultListAdapter

        listView.apply {
            layoutManager = GridLayoutManager(context, 3)
        }

    }


    override fun onStart() {
        super.onStart()
    }


    fun setItemClickListener(itemClick: View.OnClickListener) {
        this.itemClick = itemClick
    }




}