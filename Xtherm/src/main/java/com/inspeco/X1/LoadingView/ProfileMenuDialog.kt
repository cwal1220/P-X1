package com.inspeco.X1.LoadingView

import android.app.Dialog
import android.content.Context
import android.net.wifi.ScanResult
import android.util.Log
import android.view.View
import android.view.Window
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.inspeco.X1.R
import com.inspeco.data.App
import com.inspeco.data.Cfg
import com.inspeco.data.Consts
import kotlinx.android.synthetic.main.d_profile_menu.*


/**
 * Wifi Select 다이얼로그
 */
class ProfileMenuDialog(context: Context) : Dialog(context) {
    private val TAG = "bobopro-ProfileMenuDialog"
    private var sel01Click: View.OnClickListener? = null
    private var sel02Click: View.OnClickListener? = null
    //private var sel03Click: View.OnClickListener? = null

    private val userList: MutableList<String> = mutableListOf<String>()
    /**
     * 초기화
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(false)
        setCancelable(true)
        setContentView(R.layout.d_profile_menu)
        val gsonObj = Gson()
        val sUserList = Cfg.load_UserList(context)
        var data1: MutableList<String> = gsonObj.fromJson(sUserList, object : TypeToken<ArrayList<String?>?>() {}.type)
        userList.clear()
        data1.forEach { item ->
            Log.d("bobopro", "load User - ${item}")
            userList.add(item)
        }

        profileSelectItem.setOnClickListener {
            val dialog = UserListDialog(context, userList)
            dialog.show()
            dialog.setUserClickListener {
                dismiss()
                sel01Click?.onClick(it)
            }

        }

        /**
         *  게스트 모드
         */
        guestModeItem.setOnClickListener {
            Cfg.userName="GUEST"
            dismiss()
            sel02Click?.onClick(it)
        }


        /**
         *  새 프로필 생성
         */
        newProfileItem.setOnClickListener {
            val dialog = NewUserDialog(context, "")
            dialog.setConfirmListener {
                Log.w(TAG, "새 유저 ${Cfg.userName}")
                userList.add(Cfg.userName)
                val gson = Gson()
                val sUserList = gson.toJson(userList)
                //Log.d("bobopro", "save User === ${sUserList}")
                Cfg.save_UserList(context, sUserList);
                //profileView.visibility = View.VISIBLE
            }
            dialog.setCancelListener {
                Log.w(TAG, "새 유저 취소")
                //profileView.visibility = View.VISIBLE
            }
            dialog.show()
            //profileView.visibility = View.GONE
            //sel03Click?.onClick(it)
        }

    }

    fun setSel01Listener(sel01: View.OnClickListener) {
        this.sel01Click = sel01
    }

    fun setSel02Listener(sel02: View.OnClickListener?) {
        this.sel02Click = sel02
    }

//    fun setSel03Listener(sel03: View.OnClickListener?) {
//        this.sel03Click = sel03
//    }


}