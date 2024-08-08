package com.inspeco.X1.LoadingView

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.util.Log

import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.inspeco.X1.R
import com.inspeco.data.Cfg
import com.inspeco.data.hideKeyboard
import com.inspeco.ui.ItemDragListener
import com.inspeco.ui.ItemTouchHelperCallback
import kotlinx.android.synthetic.main.d_user_list.*

/**
 * User List 다이얼로그
 */
class UserListDialog(context: Context, list: MutableList<String>) : Dialog(context), ItemDragListener {

    private var itemTouchHelper: ItemTouchHelper
    private var userListAdapter : UserListAdapter
    private var userClick: View.OnClickListener? = null

    /**
     * 초기화
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        //setCanceledOnTouchOutside(false)
        setCancelable(true)

        setContentView(R.layout.d_user_list)

        userListAdapter = UserListAdapter(list, context,this )
        userListAdapter.setItemClickListener(object : UserListAdapter.ItemClick {
            override fun click(userName: String) {
                Cfg.userName = userName
                Log.d("bobopro", "$userName" )
                dismiss()
                userClick?.onClick(null)
            }
        })

        recycler_list.adapter = userListAdapter
        itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(userListAdapter))
        itemTouchHelper.attachToRecyclerView(recycler_list)


    }

    fun setUserClickListener(userClick: View.OnClickListener) {
        this.userClick = userClick
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onClickEdit(viewHolder: RecyclerView.ViewHolder) {
//        val airportCode = busInfo.airportList[viewHolder.adapterPosition].sCode
    }


}