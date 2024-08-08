package com.inspeco.X1.StatusJudgView


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inspeco.X1.R
import com.inspeco.data.EquipmentData
import com.inspeco.data.States

/**
 * Video Adapter
 */
class EquipmentListAdapter(val lists: MutableList<EquipmentData>, val context: Context) : RecyclerView.Adapter<EquipmentListAdapter.ViewHolder>()  {

    private var itemClick: View.OnClickListener? = null

    var list = lists

    ///////////////////////////////////////////////////////////////
    // Override Func
    ///////////////////////////////////////////////////////////////
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameLabel.text = list[position].name+list[position].subName
        //Log.w("bobopro User Item CELL", "View List Item ${holder.nameLabel.text}")

        // 파일 패스
        //Glide.with(context).load(File(list[position].imgName)).into(holder.resultPicture)

        val resID: Int = context.getResources().getIdentifier("${list[position].imgName}", "drawable", context.getPackageName())
        //holder.resultPicture.setImageResource(R.drawable.item_equip2_02_02)
        holder.resultPicture.setImageResource(resID)
        Log.w("bobopro", "Item ${list[position].imgName}")

        holder.itemView.setOnClickListener {
            States.diagEquipment = list[position].copy()
            itemClick?.onClick(it)
        }

    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_equipment, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        //Log.w("bobopro Video Item CELL", "View List Item Count ${list.size}")
        return list.size
    }

    ///////////////////////////////////////////////////////////////
    // Video Func
    ///////////////////////////////////////////////////////////////
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
        val resultPicture = itemView.findViewById(R.id.resultPicture) as ImageView
    }
}