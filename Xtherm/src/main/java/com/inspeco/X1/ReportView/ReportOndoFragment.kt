package com.inspeco.X1.ReportView

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import com.inspeco.X1.R
import com.inspeco.X1.StatusJudgView.FileListDialog
import com.inspeco.data.Consts
import com.inspeco.data.States
import kotlinx.android.synthetic.main.fragment_report_ondo.view.*


class ReportOndoFragment() : Fragment() {
    private val TAG = "bobopro-ReportOndoFragment"
    private lateinit var mContext: Context

    private lateinit var mView: View
    /**
     * 복합진단 Fragment Create
     */
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        Log.d(TAG, "onCreateView")

        val view: View = inflater!!.inflate(R.layout.fragment_report_ondo, container, false)
        mView = view

        view.openUdrButton.setOnClickListener{
            val dialog = FileListDialog(mContext, Consts.UDR_TEMP_FOLDER)
            if (dialog.fileList.size > 0) {
                val windowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = windowManager.defaultDisplay
                val size = Point()
                //Log.d(TAG, "width ${size.x}")
                display.getSize(size)
                val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
                params?.width = size.x - 100
                params?.height = size.y - 50
                dialog.window?.attributes = params as WindowManager.LayoutParams
                dialog.show()

                dialog.setFileClickListener {
                    States.isReportFile = false
                    var intent = Intent(mContext, ReportOndoResultActivity::class.java)
                    startActivity(intent)
                    // mView.waveLabel.text = States.diagWaveFile.fileName
                }
            } else {
                val msg = mContext.getResources().getString(R.string.no_file_msg)
                val toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }
        }


        view.openReportButton.setOnClickListener {
            val dialog = FileListDialog(mContext, Consts.REPORT_TEMP_FOLDER)
            if (dialog.fileList.size > 0) {
                val windowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = windowManager.defaultDisplay
                val size = Point()
                //Log.d(TAG, "width ${size.x}")
                display.getSize(size)
                val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
                params?.width = size.x - 100
                params?.height = size.y - 50
                dialog.window?.attributes = params as WindowManager.LayoutParams
                dialog.show()

                dialog.setFileClickListener {
                    States.isReportFile = true
                    var intent = Intent(mContext, ReportOndoResultActivity::class.java)
                    startActivity(intent)
                }
            } else {
                val msg = mContext.getResources().getString(R.string.no_file_msg)
                val toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }
        }



        return view
    }




    override fun onAttach(context: Context) {
        super.onAttach(context)
        //Log.d(TAG, "onAttatched")
        mContext = context



    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ReportOndoFragment().apply {
                }
    }
}