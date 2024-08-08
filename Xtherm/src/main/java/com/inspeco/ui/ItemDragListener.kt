package com.inspeco.ui

import androidx.recyclerview.widget.RecyclerView

interface ItemDragListener {
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
    fun onClickEdit(viewHolder: RecyclerView.ViewHolder)

}