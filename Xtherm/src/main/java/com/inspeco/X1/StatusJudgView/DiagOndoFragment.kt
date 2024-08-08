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
import com.inspeco.X1.XTerm.ByteUtil
import com.inspeco.data.Cfg
import com.inspeco.data.Consts
import com.inspeco.data.States
import com.inspeco.data.stringFromFloatAuto
import kotlinx.android.synthetic.main.fragment_diag_mix.view.*
import kotlinx.android.synthetic.main.fragment_diag_ondo.*
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.*
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.baseOndoLabel
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.equipmentLabel
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.itemVoltLabel
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.materialLabel
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.nextButton
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.ondoListLabel
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.ondoTypeLabel
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.openEquipmentButton
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.openImageButton
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.openMaterialButton
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.openOndoBaseButton
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.openOndoListButton
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.openOndoTypeButton
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.openVoltButton
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.page_layout1
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.page_layout2
import kotlinx.android.synthetic.main.fragment_diag_ondo.view.videoLabel
import java.io.File
import java.io.FileInputStream


class DiagOndoFragment() : Fragment() {
    private val TAG = "bobopro-DiagMixFragment"
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

        val view: View = inflater!!.inflate(R.layout.fragment_diag_ondo, container, false)
        mView = view
        States.diagPage = 0
        view.videoLabel.text = States.diagImageFile.fileName

        updateOndoTypeUi()
        updateVoltUi()
        view.equipmentLabel.text = States.diagEquipment.name
        updateBaseOndoUi()

        updateOndoListUi()
        updateMaterialUi()

        updatePageUI()

        // 다음 설정,   결과 확인...
        view.nextButton.setOnClickListener {

            if (States.diagPage==0) {
                States.diagPage = 1
            } else if (States.diagPage==1) {
                States.diagPage = 1
                var isOk = true
                if (States.diagImageFile.fileName == "") isOk = false
                if (States.diagOndoType == 0) isOk = false
                if (States.diagFacility == 0) isOk = false
                if (States.diagVolt == 0f) isOk = false
                if (States.diagEquipment.name == "") isOk = false
                if (States.diagBaseOndo == 0f) isOk = false
                if (States.diagMaterial.name=="") isOk = false

                if (isOk) {
                    var intent = Intent(mContext, ResultOndoActivity::class.java)
                    startActivity(intent)
                } else {
                    val msg = mContext.getResources().getString(R.string.Please_complete_all_diagnostic_items)
                    val toast = Toast.makeText(mContext,msg, Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                    toast.show()
                }
            }
            updatePageUI()
        }


        view.openImageButton.setOnClickListener{
            openImage()
        }

        view.openOndoTypeButton.setOnClickListener{
            val dialog = SelectABDialog(mContext, States.diagOndoType)
            dialog.setSaveClickListener {
                updateOndoTypeUi()
            }
            dialog.show()
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
                    if (States.diagEquipment.baseOndo>0f) {
                        States.diagBaseOndo = States.diagEquipment.baseOndo
                    } else {
                        States.diagBaseOndo =  States.diagWaveInfo.ondo
                    }
                    updateBaseOndoUi()
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



        view.openOndoListButton.setOnClickListener{
            // 3상 비교법일때 지정 가능
            if (States.diagOndoType == Consts.Diag_3Sang) {

                if (States.diagOndoList.size>0) {
                    val dialog = OndoListDialog(mContext)
                    dialog.setSaveClickListener {
                        updateOndoListUi()
                    }
                    dialog.show()
                } else {
                    val msg = mContext.getResources().getString(R.string.There_is_no_set_point_temperature)
                    val toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                    toast.show()
                }

            } else {
                val msg = mContext.getResources().getString(R.string.Please_select_3_phase)
                val toast = Toast.makeText(mContext,msg, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }


        }



        view.openOndoBaseButton.setOnClickListener {
            val dialog = EditOndoDialog(mContext, States.diagBaseOndo)
            dialog.setSaveClickListener() {
                updateBaseOndoUi()
            }
            dialog.show()
        }


        return view
    }


    private fun updateMaterialUi() {
        mView.materialLabel.text = States.diagMaterial.name
    }

    private fun updateOndoListUi() {
        var sText = ""
        var idx = 0
        var aChar = 'A'

        States.diagOndoList.forEach {
            aChar = 'A' + idx
            sText += aChar + " " + stringFromFloatAuto( Cfg.getOndoFC(it) ) + "°"+Cfg.p1_cGiho+"   "
            if ((idx % 3) == 2) {
                sText += "\n"
            }
            idx += 1
        }
        mView.ondoListLabel.text = sText
    }


    private fun updateBaseOndoUi() {
        if (States.diagBaseOndo>0f) {
            mView.baseOndoLabel.text = stringFromFloatAuto(Cfg.getOndoFC( States.diagBaseOndo)) + "°"+ Cfg.p1_cGiho
        } else {
            mView.baseOndoLabel.text = ""
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

    private fun updateOndoTypeUi() {
        val msg1 = mContext.getResources().getString(R.string.three_Phase_Comparison_method)
        val msg2 = mContext.getResources().getString(R.string.TEMP_pattern_method)

        when (States.diagOndoType) {
            Consts.Diag_3Sang -> mView.ondoTypeLabel.text = msg1
            Consts.Diag_OndoPattern -> mView.ondoTypeLabel.text = msg2
            else -> mView.ondoTypeLabel.text = ""
        }
    }


    /**
     * 이미지 선택창 열기
     */
    private fun openImage() {
        val dialog = ImageListDialog(mContext)

        if (dialog.fileList.size > 0) {
            val windowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val params: ViewGroup.LayoutParams? = dialog?.window?.attributes

            params?.width = size.x - 100
            params?.height = size.y - 50
            dialog.window?.attributes = params as WindowManager.LayoutParams

            dialog.show()

            dialog.setVideoClickListener {
                videoLabel.text = States.diagImageFile.fileName
                val file = File(States.diagImageFile.filePath)
                val fis = FileInputStream(file)
                val buffer = ByteArray(fis.available())
                fis.read(buffer)
                fis.close()
                val bufSize = buffer.size
                val checkEtx = ByteUtil.getInt(buffer, bufSize-4 )
                if (checkEtx==65538) {
                    States.diagTargetOndo = ByteUtil.getFloat(buffer, bufSize-28 )
                    val ondo1 = ByteUtil.getFloat(buffer, bufSize-8 )
                    val touchCount = ByteUtil.getInt(buffer, bufSize-32 )

                    States.diagLati  = ByteUtil.getFloat(buffer, bufSize-20 )
                    States.diagLongi = ByteUtil.getFloat(buffer, bufSize-16 )
                    States.diagOndo = ByteUtil.getFloat(buffer, bufSize-8 )
                    States.diagHumi = ByteUtil.getFloat(buffer, bufSize-12 )

                    States.diagOndoList.clear()
                    if ((touchCount>0) || (touchCount<=10) ) {
                        var bufIndex = bufSize - 36;
                        for (i in 0 until touchCount) {
                            val ondo = ByteUtil.getFloat(buffer, bufIndex )
                            bufIndex -= 4;
                            States.diagOndoList.add(0, ondo)
                            Log.d(TAG, "ondo, ${ondo}")
                        }
                    }

                    updateOndoListUi()

                    Log.d(TAG, "Load size:${bufSize}, touch Count:${touchCount}, etx:${checkEtx}, diagTargetOndo:${States.diagTargetOndo}, ondo:${ondo1}")
                    Log.d(TAG, "lati:${States.diagLati}, longi:${States.diagLongi}")
                } else {
                    val toast = Toast.makeText(mContext,"File Ver Check Error.", Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                    toast.show()

                }


            }

        } else {
            val msg = mContext.getResources().getString(R.string.File_has_been_saved)
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



    fun updatePageUI() {
//        if (States.diagPage == 0) {
//            mView.page_layout1.visibility = View.VISIBLE
//            mView.page_layout2.visibility = View.GONE
//            val msg = mContext.getResources().getString(R.string.Next_Settings)
//            mView.nextButton.text = msg
//        } else {
            mView.page_layout1.visibility = View.VISIBLE
            mView.page_layout2.visibility = View.VISIBLE
            val msg = mContext.getResources().getString(R.string.Check_result)
            mView.nextButton.text = msg
//        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                DiagOndoFragment().apply {
                }
    }
}