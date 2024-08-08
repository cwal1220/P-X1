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
import kotlinx.android.synthetic.main.d_video_list.*
import java.io.File
import java.util.*

/**
 * Equipment List 다이얼로그
 */
class EquipmentListDialog(context: Context) : Dialog(context) {

    private val TAG = "bobopro- EquipmentList Dialog"
    private lateinit var equipmentListAdapter : EquipmentListAdapter
    private var itemClick: View.OnClickListener? = null

    private lateinit var path:String
    var list = mutableListOf<EquipmentData>()


    /**
     * 기자재 선택 Dialog 초기화
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        //setCanceledOnTouchOutside(false)
        setCancelable(true)
        setContentView(R.layout.d_video_list)

        // 리스트 추가
        Ini.equipmentList.forEach {
            if ( (States.diagFacility==Consts.Diag_FacilitySupply) or  (States.diagFacility==Consts.Diag_FacilitySend) ) {
                if (it.eType==1) {
                    list.add(it)
                }
            } else if (States.diagFacility==Consts.Diag_FacilityTrans){
                if (it.eType==2) {
                    list.add(it)
                }
            }


        }

        equipmentListAdapter = EquipmentListAdapter(list, context )
        equipmentListAdapter.setItemClickListener {
                //States.diagImageFile = it
                dismiss()
                itemClick?.onClick(null)

        }

        listView.adapter = equipmentListAdapter

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