package com.inspeco.X1.LoadingView

import android.app.Dialog
import android.content.Context
import android.net.wifi.ScanResult
import android.util.Log

import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import com.inspeco.X1.R
import com.inspeco.data.hideKeyboard
import kotlinx.android.synthetic.main.d_wifi_select.*

/**
 * Wifi Select 다이얼로그
 */
class WifiSelectDialog(context: Context, list: MutableList<ScanResult>) : Dialog(context) {

    ///////////////////////////////////////////////////////////////
    // Member
    ///////////////////////////////////////////////////////////////

    lateinit var selectWifi : ScanResult
    private lateinit var wifiAdapter : WifiAdapter

    /**
     * 초기화
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(false)
        setCancelable(true)

        setContentView(R.layout.d_wifi_select)

        wifiAdapter = WifiAdapter(list)
        wifiAdapter.setItemClickListener(object : WifiAdapter.ItemClick {
            override fun click(scanResult: ScanResult) {
                selectWifi = scanResult

                // 인스펙코 장비에서는 비밀번호 입력 없이 바로 연결 진행
                val ssid = selectWifi.SSID.toLowerCase()
                if (ssid.contains("inspeco")) {
                    dismiss()
                    if (context is LoadingActivity) {
                        if (ssid.contains("x1")) {  // INSPECO_X1
                            context.connectWifi(selectWifi, "0123456789")
                        } else {
                            context.connectWifi(selectWifi, "1234567890")
                        }
                    }
                }
            //                else {
//                    layout_search.visibility = View.GONE
//                    layout_password.visibility = View.VISIBLE
//                }
            }
        })

        //recycler_wifi.
        // 리사이클러뷰
        with(recycler_wifi) {
            //layoutManager = LinearLayoutManager(getContext(), OrientationHelper.VERTICAL, false)
            adapter = wifiAdapter
        }

        // 닫기
        txt_close.setOnClickListener {
            //hideKeyboard(context, edt_password)
            dismiss()
            if (context is LoadingActivity) {
                context.showProfileMenuDialog()
            }

        }

//        // 패스워드 닫기
//        txt_close_password.setOnClickListener {
//
//            hideKeyboard(context, edt_password)
//
//            layout_search.visibility = View.VISIBLE
//            layout_password.visibility = View.GONE
//        }
//
//        // 비밀번호 확인
//        txt_password_confirm.setOnClickListener {
//            if (edt_password.text.isEmpty()) {
//                Toast.makeText(context, context.resources.getString(R.string.insert_password), Toast.LENGTH_SHORT).show()
//            } else {
//
//                hideKeyboard(context, edt_password)
//
//                dismiss()
//                if (context is LoadingActivity) {
//                    context.connectWifi(selectWifi, edt_password.text.toString())
//                }
//            }
//        }

        txt_refresh.setOnClickListener {
            showProgress(View.VISIBLE)
            if (context is LoadingActivity) {
                context.scanWifi()
            }
        }
    }

    /**
     * Wifi List 등록
     */
    fun setWifiList(list: MutableList<ScanResult>) {
        wifiAdapter.setItems(list)
    }

    /**
     * 프로그래스바 보이기/숨기기
     */
    fun showProgress(v: Int) {
        pb_1.visibility = v
    }

}