package com.inspeco.X1.StatusJudgView


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inspeco.X1.R
import com.inspeco.data.FileData
import com.inspeco.dialog.CommonDialog
import com.inspeco.ui.ItemActionListener
import com.inspeco.ui.ItemDragListener

/**
 * wave Adapter
 */
class FileListAdapter(val fileList:MutableList<FileData>, val context: Context, private val listener: ItemDragListener) : RecyclerView.Adapter<FileListAdapter.ViewHolder>(), ItemActionListener {

    interface ItemClick {
        fun click(fileItem: FileData)
    }

    ///////////////////////////////////////////////////////////////
    // Member
    ///////////////////////////////////////////////////////////////
    private lateinit var itemClick : ItemClick
    var list = fileList

    ///////////////////////////////////////////////////////////////
    // Override Func
    ///////////////////////////////////////////////////////////////
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameLabel.text = list[position].fileName
        //Log.w("bobopro User Item CELL", "View List Item ${holder.nameLabel.text}")
//        if (position==list.size-1) {
//            holder.lineView.visibility = View.GONE
//        } else {
//            holder.lineView.visibility = View.VISIBLE
//        }

        holder.itemView.setOnClickListener {
            itemClick.click(list[position])
        }

//        holder.dragHandle.setOnTouchListener { _, event ->
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                listener.onStartDrag(holder)
//            }
//            false
//        }
    }

    fun restoreItem(aFileItem: FileData, position: Int) {
        list.add(position, aFileItem)
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
        val deleteName = list[position]
        val msg = context.getResources().getString(R.string.Delete_File)
        val yes = context.getResources().getString(R.string.Yes)
        val no = context.getResources().getString(R.string.No)

        val dialog = CommonDialog(context, "", "${deleteName.fileName}, "+msg, yes, no)
        dialog.show()
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
        dialog.setCancelListener {
            restoreItem(deleteName, position)
        }

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_wave, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        //Log.w("bobopro Wave Item CELL", "View List Item Count ${list.size}")
        return list.size
    }

    ///////////////////////////////////////////////////////////////
    // wave Func
    ///////////////////////////////////////////////////////////////
    /**
     * 리스너 등록
     */
    fun setItemClickListener(click: ItemClick) {
        itemClick = click
    }



    /**
     * View Holder Class
     */
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val nameLabel = itemView.findViewById(R.id.nameLabel) as TextView
        //val dragHandle = itemView.findViewById(R.id.dragHandle) as ImageView
        //val lineView = itemView.findViewById(R.id.bottomLine) as View

    }
}