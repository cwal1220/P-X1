package com.inspeco.X1.StatusJudgView

import android.app.Dialog
import android.content.Context
import android.os.Environment

import android.view.View
import android.view.Window
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.inspeco.X1.R
import com.inspeco.data.*
import com.inspeco.ui.ItemDragListener
import com.inspeco.ui.ItemTouchHelperCallback
import kotlinx.android.synthetic.main.d_wave_list.*
import java.io.File
import java.util.*

/**
 * Wave List 다이얼로그
 */
class FileListDialog(context: Context, folder: String) : Dialog(context), ItemDragListener {

    private val TAG = "bobopro-WaveListDialog"
    private var itemTouchHelper: ItemTouchHelper
    private var fileListAdapter : FileListAdapter
    private var fileClick: View.OnClickListener? = null

    private lateinit var path:String
    private lateinit var mContext: Context
    public var fileList = mutableListOf<FileData>()



    /**
     * 초기화
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        //setCanceledOnTouchOutside(false)
        setCancelable(true)

        setContentView(R.layout.d_wave_list)

        path = Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/" + folder

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

            if (folder == Consts.AUDIO_RECORDER_FOLDER) {
                // 오디오 파일일 경우 temp 파일이 있으면 삭제
                val index = newFiles.indexOf(File(path + "/" + Consts.AUDIO_RECORDER_TEMP_FILE))
                if (index != -1) {
                    deleteAudioTempFile()
                    newFiles.removeAt(index)
                }
            }

            Collections.sort(newFiles, Collections.reverseOrder())


            // 리스트 추가
            for (i in 0 until newFiles.size) {
                val file = FileData()
                file.fileName = newFiles[i].name
                file.filePath = newFiles[i].path

                //if (type == Const.SEL_TYPE_PIC_WAVEFORM) {
                //    if (newFiles[i].name.contains(Const.PREFIX_F)) {
                //        fileList.add(file)
                //    }
                fileList.add(file)

            }
        }

        fileListAdapter = FileListAdapter(fileList, context,this )
        fileListAdapter.setItemClickListener(object : FileListAdapter.ItemClick {
            override fun click(fileItem: FileData) {
                States.diagFileData = fileItem
                dismiss()
                fileClick?.onClick(null)
            }
        })

        listView.adapter = fileListAdapter
        itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(fileListAdapter))
        itemTouchHelper.attachToRecyclerView(listView)

    }


    override fun onStart() {
        super.onStart()


    }

    fun setFileClickListener(userClick: View.OnClickListener) {
        this.fileClick = userClick
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onClickEdit(viewHolder: RecyclerView.ViewHolder) {
        //  val airportCode = busInfo.airportList[viewHolder.adapterPosition].sCode
    }


}