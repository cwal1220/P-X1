package com.inspeco.X1.HomeView

import android.content.Context
import android.content.Intent
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.inspeco.X1.R
import com.inspeco.X1.StatusJudgView.ImageListDialog
import com.inspeco.X1.StatusJudgView.FileListDialog
import com.inspeco.data.Consts
import com.inspeco.data.States
import kotlinx.android.synthetic.main.activity_share.*
import java.io.File

class ShareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
    }

    override fun onResume() {
        super.onResume()

        share_item1.setOnClickListener {
            val dialog = FileListDialog(this, Consts.AUDIO_RECORDER_FOLDER)

            if (dialog.fileList.size > 0) {
                val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "*/*"
                    i.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    i.putExtra( Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                            this,
                            "com.inspeco.X1",
                            File(States.diagFileData.filePath)))
                    val chooser = Intent.createChooser(i, "Share")
                    startActivity(chooser)
                }
            } else {
                val msg = getResources().getString(R.string.no_file_msg)
                val toast = Toast.makeText(this,msg, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }
        }


        share_item2.setOnClickListener {
            val dialog = ImageListDialog(this)

            if (dialog.fileList.size > 0) {
                val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = windowManager.defaultDisplay
                val size = Point()
                display.getSize(size)
                val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
                params?.width = size.x - 100
                params?.height = size.y - 50
                dialog.window?.attributes = params as WindowManager.LayoutParams
                dialog.show()
                dialog.setVideoClickListener {
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "*/*"
                    i.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    i.putExtra( Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                            this,
                            "com.inspeco.X1",
                            File(States.diagImageFile.filePath)))
                    val chooser = Intent.createChooser(i, "Share")
                    startActivity(chooser)
                }

            } else {
                val msg = getResources().getString(R.string.no_file_msg)
                val toast = Toast.makeText(this,msg, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }
        }


        share_item3.setOnClickListener {
            val dialog = FileListDialog(this, Consts.VIDEO_FOLDER)

            if (dialog.fileList.size > 0) {
                val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = windowManager.defaultDisplay
                val size = Point()
                display.getSize(size)
                val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
                params?.width = size.x - 100
                params?.height = size.y - 50
                dialog.window?.attributes = params as WindowManager.LayoutParams
                dialog.show()
                dialog.setFileClickListener {
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "*/*"
                    i.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    i.putExtra( Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                            this,
                            "com.inspeco.X1",
                            File(States.diagFileData.filePath)))
                    val chooser = Intent.createChooser(i, "Share")
                    startActivity(chooser)
                }

            } else {
                val msg = getResources().getString(R.string.no_file_msg)
                val toast = Toast.makeText(this,msg, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }
        }


        share_item4.setOnClickListener {
            val dialog = FileListDialog(this, Consts.DOCUMENT_FOLDER)

            if (dialog.fileList.size > 0) {
                val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = windowManager.defaultDisplay
                val size = Point()
                display.getSize(size)
                val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
                params?.width = size.x - 100
                params?.height = size.y - 50
                dialog.window?.attributes = params as WindowManager.LayoutParams
                dialog.show()
                dialog.setFileClickListener {
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "*/*"
                    i.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    i.putExtra( Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                            this,
                            "com.inspeco.X1",
                            File(States.diagFileData.filePath)))
                    val chooser = Intent.createChooser(i, "Share")
                    startActivity(chooser)
                }

            } else {
                val msg = getResources().getString(R.string.no_file_msg)
                val toast = Toast.makeText(this,msg, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }
        }

        share_item5.setOnClickListener {
            val dialog = FileListDialog(this, Consts.DOCUMENT_FOLDER)

            if (dialog.fileList.size > 0) {
                val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = windowManager.defaultDisplay
                val size = Point()
                display.getSize(size)
                val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
                params?.width = size.x - 100
                params?.height = size.y - 50
                dialog.window?.attributes = params as WindowManager.LayoutParams
                dialog.show()
                dialog.setFileClickListener {
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "*/*"
                    i.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    i.putExtra( Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                            this,
                            "com.inspeco.X1",
                            File(States.diagFileData.filePath)))
                    val chooser = Intent.createChooser(i, "Share")
                    startActivity(chooser)
                }

            } else {
                val msg = getResources().getString(R.string.no_file_msg)
                val toast = Toast.makeText(this,msg, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }
        }
    }





}