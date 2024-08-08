package com.inspeco.X1.StatusJudgView

import android.app.Dialog
import android.content.Context
import android.os.Environment

import android.view.View
import android.view.Window
import androidx.recyclerview.widget.*
import com.inspeco.X1.R
import com.inspeco.data.*
import com.inspeco.ui.ItemDragListener
import com.inspeco.ui.ItemTouchHelperCallback
import kotlinx.android.synthetic.main.d_video_list.*
import java.io.File
import java.util.*

/**
 * Image List 다이얼로그
 */
class ImageListDialog(context: Context) : Dialog(context), ItemDragListener {

    private val TAG = "bobopro-ImageListDialog"
    private var itemTouchHelper: ItemTouchHelper
    private lateinit var imageListAdapter : ImageListAdapter
    private var videoClick: View.OnClickListener? = null

    private lateinit var path:String
    var fileList = mutableListOf<FileData>()


    /**
     * 이미지 선택 Dialog 초기화
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        //setCanceledOnTouchOutside(false)
        setCancelable(true)
        setContentView(R.layout.d_video_list)
        //path = Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/" + Consts.SCREEN_SHOT_FOLDER
        path = Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/" + Consts.SCREEN_SHOT_FOLDER

        val dir = File(path)
        fileList.clear()

        // 폴더가 없으면 생성
        if (!dir.exists()) {
            dir.mkdir()
        }
        val files = dir.listFiles()
        if (files != null) {
            val newFiles = arrayListOf<File>()
            newFiles.addAll(files)

            Collections.sort(newFiles, Collections.reverseOrder())

            // 리스트 추가
            for (i in 0 until newFiles.size) {
                val file = FileData()
                file.fileName = newFiles[i].name
                file.filePath = newFiles[i].path
                fileList.add(file)
            }
        }

        imageListAdapter = ImageListAdapter(fileList, context,this )
        imageListAdapter.setItemClickListener(object : ImageListAdapter.ItemClick {
            override fun click(fileItem: FileData) {
                States.diagImageFile = fileItem
                dismiss()
                videoClick?.onClick(null)
            }
        })

        listView.adapter = imageListAdapter

        listView.apply {
            layoutManager = GridLayoutManager(context, 3)
        }
        itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(imageListAdapter))
        itemTouchHelper.attachToRecyclerView(listView)

    }


    override fun onStart() {
        super.onStart()
    }


    fun setVideoClickListener(itemClick: View.OnClickListener) {
        this.videoClick = itemClick
    }


    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }


    override fun onClickEdit(viewHolder: RecyclerView.ViewHolder) {
        //  val airportCode = busInfo.airportList[viewHolder.adapterPosition].sCode
    }


}