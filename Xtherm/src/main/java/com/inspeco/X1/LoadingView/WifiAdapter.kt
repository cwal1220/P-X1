package com.inspeco.X1.LoadingView

import android.net.wifi.ScanResult
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inspeco.X1.R

/**
 * Wifi Adapter
 */
class WifiAdapter(wifiList: MutableList<ScanResult>) : RecyclerView.Adapter<WifiAdapter.ViewHolder>() {

    /**
     * 아이텤 클릭 인터페이스
     */
    interface ItemClick {
        fun click(scanResult: ScanResult)
    }

    ///////////////////////////////////////////////////////////////
    // Member
    ///////////////////////////////////////////////////////////////
    private lateinit var itemClick : ItemClick
    var list = wifiList

    ///////////////////////////////////////////////////////////////
    // Override Func
    ///////////////////////////////////////////////////////////////
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtName.text = list[position].SSID
        Log.w("bobopro CELL", "View List Item ${holder.txtName.text}")
        holder.itemView.setOnClickListener {
            itemClick.click(list[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_wifi, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        Log.w("bobopro CELL", "View List Item Count ${list.size}")
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
    fun setItems(wifiList: MutableList<ScanResult>) {
        list = wifiList
        Log.w("bobopro CELL", "View List setItems Count ${list.size}")
        notifyDataSetChanged()
    }

    /**
     * View Holder Class
     */
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val txtName = itemView.findViewById(R.id.nameLabel) as TextView
    }
}