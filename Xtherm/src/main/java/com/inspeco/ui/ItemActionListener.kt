package com.inspeco.ui

interface ItemActionListener {
    fun onItemMoved(from: Int, to: Int)
    fun onItemSwiped(position: Int)
}