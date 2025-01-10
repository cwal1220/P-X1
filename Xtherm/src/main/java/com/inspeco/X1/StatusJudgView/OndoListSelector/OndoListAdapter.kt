package com.inspeco.X1.StatusJudgView


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inspeco.X1.R
import com.inspeco.data.States

/**
 * Ondo List Adapter
 */
class OndoListAdapter(val lists: MutableList<Float>, val context: Context) : RecyclerView.Adapter<OndoListAdapter.ViewHolder>()  {

    private var itemClick: View.OnClickListener? = null

    var list = lists

    ///////////////////////////////////////////////////////////////
    // Override Func
    ///////////////////////////////////////////////////////////////
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val aChar = 'A'+position
        holder.nameLabel.text = "POINT "+aChar+" "
                list[position]

        holder.itemView.setOnClickListener {
            States.diagSelectOndoIdx = position
            States.diagSelectOndo = list[position]
            itemClick?.onClick(it)
        }

    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * 리스너 등록
     */
    fun setItemClickListener(clickListener: View.OnClickListener?) {
        this.itemClick = clickListener
    }


    /**
     * View Holder Class
     */
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val nameLabel = itemView.findViewById(R.id.nameLabel) as TextView
    }
}