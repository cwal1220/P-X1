package com.inspeco.X1.SettingView

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import com.inspeco.X1.R
import com.inspeco.X1.StatusJudgView.EditDistanceDialog
import com.inspeco.X1.StatusJudgView.EditOndoDialog
import com.inspeco.data.Cfg
import com.inspeco.data.Consts
import com.inspeco.data.States
import com.inspeco.dialog.Cam3OndoDialog
import com.inspeco.dialog.CamOndoSettingDialog
import com.inspeco.dialog.PaletteDialog
import kotlinx.android.synthetic.main.activity_set_alram.*
import kotlinx.android.synthetic.main.fragment_set_cam2.*
import kotlinx.android.synthetic.main.fragment_set_cam3.*


class SetCam3Fragment : Fragment() {

    private var closeClick: View.OnClickListener? = null
    private var arrangeClick: View.OnClickListener? = null
    private var ondoSelectClick: View.OnClickListener? = null
    private var distanceClick: View.OnClickListener? = null
    private var ondoModeClick: View.OnClickListener? = null
    private var isHideClose = false
    private lateinit var mContext: Context

    fun setContext(aContext: Context) {
        mContext = aContext
    }


    fun hideCloseButton() {
        isHideClose = true
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {


        return inflater.inflate(R.layout.fragment_set_cam3, container, false)
    }


    override fun onResume() {
        super.onResume()

        if (isHideClose) {
            fset_cam3_close.visibility = View.GONE
        }

        set_cam3_item1.setOnClickListener {
            val yes = getResources().getString(R.string.Yes)
            val no = getResources().getString(R.string.No)
            val dialog = CamOndoSettingDialog(mContext, Consts.CAM_MIX, yes, no)
            dialog.setOkListener() {

            }
            dialog.show()
        }

        fset_cam3_close.setOnClickListener {
            closeClick?.onClick(it)
        }

        set_cam3_item1b.setOnClickListener {
            ondoSelectClick?.onClick(it)
        }

        set_cam3_item4.setOnClickListener {
            val dialog = EditDistanceDialog(mContext, Cfg.p1_dist)
            dialog.setSaveClickListener {
                Cfg.p1_dist = States.dialogFloat
                Cfg.save_p1(mContext)
                this.distanceClick?.onClick(it)
            }
            dialog.show()
        }


        set_cam3_paletteItem.setOnClickListener {
            val dialog = PaletteDialog(mContext)
            dialog.setSel01Listener() {
                Cfg.cam3_colorMode = Consts.PaletteRainbow
                Cfg.save_cam3Ondo(mContext)
            }
            dialog.setSel02Listener() {
                Cfg.cam3_colorMode = Consts.PaletteAmber
                Cfg.save_cam3Ondo(mContext)
            }
            dialog.setSel03Listener() {
                Cfg.cam3_colorMode = Consts.PaletteWhite
                Cfg.save_cam3Ondo(mContext)
            }

            dialog.show()
        }

        set_cam3_ondoItem.setOnClickListener {
            val save = getResources().getString(R.string.Save)
            val dialog = SlideOndoDialog(mContext, Cfg.ondo_offSet, "°", save);
            dialog.setOkListener() {
                Cfg.ondo_offSet = States.dialogFloat
                Cfg.save_cam2Ondo(mContext)
            }
            dialog.show()
        }

        set_cam3_arrangeItem.setOnClickListener {
            arrangeClick?.onClick(it)
        }

        set_cam3_ondoMode.setOnClickListener {
            val a1 = getResources().getString(R.string.Normal_mode) +"( -20℃ ~ 120℃ )"
            val a2 = getResources().getString(R.string.Ext_Mode)+"( 0℃ ~ 600℃ )"

            val dialog = SelectAb2Dialog(mContext, a1, a2 )
            dialog.setSel01Listener {
                Cfg.ondo_extMode = false
                Cfg.save_cam3Ondo(mContext)
                this.ondoModeClick?.onClick(it)
            }
            dialog.setSel02Listener {
                Cfg.ondo_extMode = true
                Cfg.save_cam3Ondo(mContext)
                this.ondoModeClick?.onClick(it)
            }
            dialog.show()
        }

    }


    fun setCloseListener(onClick: View.OnClickListener) {
        this.closeClick = onClick
    }

    fun setOndoSelectListener(onClick: View.OnClickListener) {
        this.ondoSelectClick = onClick
    }

    fun setArrangeListener(onClick: View.OnClickListener) {
        this.arrangeClick = onClick
    }

    fun setDistanceListener(onClick: View.OnClickListener) {
        this.distanceClick = onClick
    }

    fun setOndoModeListener(onClick: View.OnClickListener) {
        this.ondoModeClick = onClick
    }


}