package com.inspeco.X1.StatusJudgView

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import com.inspeco.X1.R
import com.inspeco.data.Consts
import com.inspeco.data.States
import com.inspeco.data.stringFromFloatAuto
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.*
import kotlinx.android.synthetic.main.fragment_diag_wave.view.*
import kotlinx.android.synthetic.main.fragment_diag_wave.view.equipmentLabel
import kotlinx.android.synthetic.main.fragment_diag_wave.view.itemVoltLabel
import kotlinx.android.synthetic.main.fragment_diag_wave.view.materialLabel
import kotlinx.android.synthetic.main.fragment_diag_wave.view.nextButton
import kotlinx.android.synthetic.main.fragment_diag_wave.view.openEquipmentButton
import kotlinx.android.synthetic.main.fragment_diag_wave.view.openMaterialButton
import kotlinx.android.synthetic.main.fragment_diag_wave.view.openVoltButton


class DiagWaveFragment() : Fragment() {
    private val TAG = "bobopro-DiagWaveFragment"
    private lateinit var mContext: Context

    private lateinit var mView: View
    /**
     * 초음파 진단 Fragment Create
     */
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        Log.d(TAG, "onCreateView")

        val view: View = inflater!!.inflate(R.layout.fragment_diag_wave, container, false)
        mView = view

        view.waveLabel.text = States.diagFileData.fileName

        updateVoltUi()
        view.equipmentLabel.text = States.diagEquipment.name
        view.faultTypeLabel.text= States.diagFaulty.name

        updateDistanceUi()
        updateMaterialUi()

        // 다음 설정,   결과 확인...
        view.nextButton.setOnClickListener {
            var isOk = true
            if (States.diagFileData.fileName=="") isOk = false
            if (States.diagFacility==0) isOk = false
            if (States.diagVolt==0f) isOk = false
            if (States.diagEquipment.name=="") isOk = false
            if (States.diagMaterial.name=="") isOk = false
            if (States.diagFaulty.name=="") isOk = false
            if (States.diagDistance==0f) isOk = false

            if (isOk) {
                Log.d(TAG, "결과 액티비티")
                var intent = Intent(mContext, ResultWaveActivity::class.java)
                startActivity(intent)
            } else {
                val msg = mContext.getResources().getString(R.string.Please_complete_all_diagnostic_items)
                val toast = Toast.makeText(mContext,msg, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }
        }

        // 웨이브 오픈 클릭
        view.openWaveButton.setOnClickListener{
            openWave()
        }


        view.openVoltButton.setOnClickListener{
            val dialog = SelectVoltDialog(mContext, States.diagFacility, States.diagVolt)
            dialog.setSaveClickListener {
                updateVoltUi()
            }
            dialog.show()
        }


        view.openEquipmentButton.setOnClickListener{
            if (States.diagFacility==0) {
                val msg = mContext.getResources().getString(R.string.Please_select_equipment_first)
                val toast = Toast.makeText(mContext,msg, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            } else {
                val dialog = EquipmentListDialog(mContext)
                dialog.setItemClickListener {
                    mView.equipmentLabel.text = States.diagEquipment.name
                }
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
            }
        }




        view.openMaterialButton.setOnClickListener {
            val dialog = SelectMaterialDialog(mContext)
            dialog.setSelectItemListener() {
                updateMaterialUi()
            }
            dialog.show()
        }


        view.openFaultTypeButton.setOnClickListener {
            val dialog = FaultListDialog(mContext)
            dialog.setItemClickListener() {
                mView.faultTypeLabel.text= States.diagFaulty.name
            }
            dialog.show()
        }



        view.openDistanceButton.setOnClickListener {
            val dialog = EditDistanceDialog(mContext, States.diagDistance)
            dialog.setSaveClickListener() {
                States.diagDistance = States.dialogFloat
                updateDistanceUi()

            }
            dialog.show()
        }

        return view
    }


    private fun updateMaterialUi() {
        mView.materialLabel.text = States.diagMaterial.name
    }




    private fun updateDistanceUi() {
        if (States.diagDistance>0f) {
            mView.distanceLabel.text = stringFromFloatAuto(States.diagDistance) + "m"
        } else {
            mView.distanceLabel.text = ""
        }
    }



    private fun updateVoltUi() {
        if (States.diagVolt>0) {
            val sVolt = stringFromFloatAuto(States.diagVolt) + "kV"
            val trans =  mContext.getResources().getString(R.string.Power_Transmission)
            val dist =  mContext.getResources().getString(R.string.Power_Distribution)
            val subs =  mContext.getResources().getString(R.string.Substation)


            when (States.diagFacility) {
                Consts.Diag_FacilitySupply -> mView.itemVoltLabel.text = trans + " " + sVolt
                Consts.Diag_FacilitySend -> mView.itemVoltLabel.text = dist + " " + sVolt
                Consts.Diag_FacilityTrans -> mView.itemVoltLabel.text = subs + " " + sVolt
                else -> mView.itemVoltLabel.text = ""
            }
        } else {
            mView.itemVoltLabel.text = ""
        }
    }



    /**
     * 웨이브 선택창 열기
     */
    private fun openWave() {
        val dialog = FileListDialog(mContext, Consts.AUDIO_RECORDER_FOLDER)

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
                mView.waveLabel.text = States.diagFileData.fileName
            }
        } else {
            val msg = mContext.getResources().getString(R.string.no_file_msg)
            val toast = Toast.makeText(mContext,msg, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
            toast.show()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        //Log.d(TAG, "onAttatched")
        mContext = context

    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                DiagWaveFragment().apply {
                }
    }

}