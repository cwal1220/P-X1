package com.inspeco.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Grid Divider Decoration
 */
class GridDividerDecoration(context: Context, inset:Int) : RecyclerView.ItemDecoration() {

    ///////////////////////////////////////////////////////////////
    // Member
    ///////////////////////////////////////////////////////////////
//    private val ATTRS = intArrayOf(android.R.attr.listDivider)

  //  val a = context.obtainStyledAttributes(ATTRS)
    private val mInsets: Int = inset
//    private val mDivider: Drawable = context.resources.getDrawable(R.drawable.recycler_line_transparent)

    // 초기화
    init {
    //    a.recycle()
    }

    ///////////////////////////////////////////////////////////////
    // Override Func
    ///////////////////////////////////////////////////////////////
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawVertical(c, parent)
        drawHorizontal(c, parent)
    }

    /** Draw dividers at each expected grid interval  */
    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        if (parent.childCount == 0) return

        val childCount = parent.childCount

//        for (i in 0 until childCount) {
//            val child = parent.getChildAt(i)
//            val params = child.layoutParams as RecyclerView.LayoutParams
//
//            val left = child.left - params.leftMargin - mInsets
//            val right = child.right + params.rightMargin + mInsets
//            val top = child.bottom + params.bottomMargin + mInsets
//            val bottom = top + mDivider.intrinsicHeight
//            mDivider.setBounds(left, top, right, bottom)
//            mDivider.draw(c)
//        }
    }

    /** Draw dividers to the right of each child view  */
    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount

//        for (i in 0 until childCount) {
//            val child = parent.getChildAt(i)
//            val params = child.layoutParams as RecyclerView.LayoutParams
//
//            val left = child.right + params.rightMargin + mInsets
//            val right = left + mDivider.intrinsicWidth
//            val top = child.top - params.topMargin - mInsets
//            val bottom = child.bottom + params.bottomMargin + mInsets
//            mDivider.setBounds(left, top, right, bottom)
//            mDivider.draw(c)
//        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        //We can supply forced insets for each item view here in the Rect
        outRect.set(mInsets, mInsets, mInsets, mInsets)
    }
}