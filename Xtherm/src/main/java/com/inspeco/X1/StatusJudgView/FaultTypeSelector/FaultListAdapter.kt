package com.inspeco.X1.StatusJudgView


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inspeco.X1.R
import com.inspeco.data.ConditionData
import com.inspeco.data.States

/**
 * FaultType  Adapter
 */
class FaultListAdapter(val lists:MutableList<ConditionData>, val context: Context) : RecyclerView.Adapter<FaultListAdapter.ViewHolder>()  {

    private var itemClick: View.OnClickListener? = null

    var list = lists

    ///////////////////////////////////////////////////////////////
    // Override Func
    ///////////////////////////////////////////////////////////////
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameLabel.text = list[position].name
        //Log.w("bobopro User Item CELL", "View List Item ${holder.nameLabel.text}")

        // 파일 패스
        //Glide.with(context).load(File(list[position].imgName)).into(holder.imageView)

        holder.itemView.setOnClickListener {
            States.diagFaulty = list[position].copy()
            itemClick?.onClick(it)
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_grid, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

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