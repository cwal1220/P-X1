package com.inspeco.X1.SettingView

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.inspeco.X1.R
import com.inspeco.data.Cfg
import com.inspeco.data.States
import com.inspeco.dialog.CommonDialog
import kotlinx.android.synthetic.main.fragment_set_base.*


class SetBaseFragment : Fragment() {


    private lateinit var mContext: Context

    fun setContext(aContext: Context) {
        mContext = aContext
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {

        return inflater.inflate(R.layout.fragment_set_base, container, false)
    }


    override fun onResume() {
        super.onResume()

        set_base_item1.setOnClickListener {
            var intent = Intent(mContext, SetAlramActivity::class.java)
            startActivity(intent)
        }

        set_base_item3.setOnClickListener {

            val a1 = getResources().getString(R.string.YYMMDD)
            val a2 = getResources().getString(R.string.MMDDYY)
            val a3 = getResources().getString(R.string.DDMMYY)

            val dialog = SelectAbcDialog(mContext, a1, a2, a3)
            dialog.setSel01Listener {
                Cfg.p1_dateStr = "yyyyMMdd"
                Cfg.p1_dateStr2 = "yyyy-MM-dd"
                Cfg.save_p1(mContext)
            }
            dialog.setSel02Listener {
                Cfg.p1_dateStr = "MMddyyyy"
                Cfg.p1_dateStr2 = "MM-dd-yyyy"
                Cfg.save_p1(mContext)
            }
            dialog.setSel03Listener {
                Cfg.p1_dateStr = "ddMMyyyy"
                Cfg.p1_dateStr2 = "dd-MM-yyyy"
                Cfg.save_p1(mContext)
            }
            dialog.show()
        }

        set_base_item4.setOnClickListener {
            val dialog = EditSecDialog(mContext, Cfg.p1_recTimeWave, "10")
            dialog.setSaveClickListener() {
                Cfg.p1_recTimeWave = States.dialogInt
                Cfg.save_p1(mContext)
            }
            dialog.show()
        }

        set_base_item5.setOnClickListener {
            val dialog = EditSecDialog(mContext, Cfg.p1_recTimeMovie, "30")
            dialog.setSaveClickListener() {
                Cfg.p1_recTimeMovie = States.dialogInt
                Cfg.save_p1(mContext)
            }
            dialog.show()
        }

        set_base_item6.setOnClickListener {

            val a1 = getResources().getString(R.string.Celsius)
            val a2 = getResources().getString(R.string.Fahrenheit)

            val dialog = SelectAb2Dialog(mContext, a1, a2)
            dialog.setSel01Listener {
                Cfg.p1_isC = true
                Cfg.p1_cGiho = "C"
                Cfg.save_p1(mContext)
            }
            dialog.setSel02Listener {
                Cfg.p1_isC = false
                Cfg.p1_cGiho = "F"
                Cfg.save_p1(mContext)
            }
            dialog.show()
        }

        set_base_item7.setOnClickListener {
            val a1 = getResources().getString(R.string.Active)
            val b1 = getResources().getString(R.string.Inactive)
            val dialog = SelectAb2Dialog(mContext, a1, b1)

            dialog.setSel01Listener {
                Cfg.p1_isMainWaveShow = true
                Cfg.save_p1(mContext)
            }
            dialog.setSel02Listener {
                Cfg.p1_isMainWaveShow = false
                Cfg.save_p1(mContext)
            }
            dialog.show()
        }


        set_base_item8.setOnClickListener {
            val msg = getResources().getString(R.string.Are_you_sure_you_want_to_reset)
            val yes = getResources().getString(R.string.Yes)
            val no = getResources().getString(R.string.No)

            val dialog = CommonDialog(mContext, "", msg, yes, no)
            dialog.show()
            dialog.setOkListener {
                // 삭제...
                Cfg.clear_p1Config()
                Cfg.save_p1(mContext)
                Cfg.save_cam2Ondo(mContext)
                Cfg.save_cam3Ondo(mContext)
            }
        }


    }




}