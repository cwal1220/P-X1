package com.inspeco.X1.LoadingView


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inspeco.X1.R
import com.inspeco.dialog.CommonDialog
import com.inspeco.ui.ItemActionListener
import com.inspeco.ui.ItemDragListener

/**
 * User_List Adapter
 */
class UserListAdapter(userList: MutableList<String>, val context: Context, private val listener: ItemDragListener) : RecyclerView.Adapter<UserListAdapter.ViewHolder>(), ItemActionListener {

    interface ItemClick {
        fun click(userName: String)
    }

    ///////////////////////////////////////////////////////////////
    // Member
    ///////////////////////////////////////////////////////////////
    private lateinit var itemClick : ItemClick
    var list = userList

    ///////////////////////////////////////////////////////////////
    // Override Func
    ///////////////////////////////////////////////////////////////
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameLabel.text = list[position]
        //Log.w("bobopro User Item CELL", "View List Item ${holder.nameLabel.text}")
//        if (position==list.size-1) {
//            holder.lineView.visibility = View.GONE
//        } else {
//            holder.lineView.visibility = View.VISIBLE
//        }

        holder.itemView.setOnClickListener {
            itemClick.click(list[position])
        }

        holder.dragHandle.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                listener.onStartDrag(holder)
            }
            false
        }
    }

    fun restoreItem(name: String, position: Int) {
        list.add(position, name)
        // notify item added by position
        notifyItemInserted(position)

    }


    override fun onItemMoved(from: Int, to: Int) {
        if (from == to) {
            return
        }
        val fromItem = list.removeAt(from)
        list.add(to, fromItem)
        notifyItemMoved(from, to)

    }

    override fun onItemSwiped(position: Int) {
//        busInfo.airportList.removeAt(position)

        // 삭제 모달
        val deleteName = list[position] + " "+ context.getResources().getString(R.string.Delete_Profile)
        val yes = context.getResources().getString(R.string.Yes)
        val no = context.getResources().getString(R.string.Cancel)

        val dialog = CommonDialog(context, "", "deleteName", yes, no)
        dialog.show()
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
        dialog.setCancelListener {
            restoreItem(deleteName, position)
        }

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        Log.w("bobopro User Item CELL", "View List Item Count ${list.size}")
        return list.size
    }

    ///////////////////////////////////////////////////////////////
    // User Func
    ///////////////////////////////////////////////////////////////
    /**
     * 리스너 등록
     */
    fun setItemClickListener(click: ItemClick) {
        itemClick = click
    }


    /**
     * 아이템 등록
     */
    fun setItems(userList: MutableList<String>) {
        list = userList
        Log.w("bobopro User Item CELL", "View List setItems Count ${list.size}")
        notifyDataSetChanged()
    }

    /**
     * View Holder Class
     */
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val nameLabel = itemView.findViewById(R.id.nameLabel) as TextView
        val dragHandle = itemView.findViewById(R.id.imageView) as ImageView
        val lineView = itemView.findViewById(R.id.bottomLine) as View

    }

}