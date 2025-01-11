package com.inspeco.data

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.inspeco.X1.R
import java.io.*
import java.util.*


class PLList(pl:ArrayList<DiagData>) : Serializable {
    var pls:ArrayList<DiagData> = pl
}


class CondList(cond:ArrayList<ConditionData>) : Serializable {
    var conds:ArrayList<ConditionData> = cond
}


class EquipList(equip:ArrayList<EquipmentData>) : Serializable {
    var lists:ArrayList<EquipmentData> = equip
}

class VoltList(volt:ArrayList<VoltData>) : Serializable {
    var lists:ArrayList<VoltData> = volt
}

object Ini {

    /**
     *  상태판정에 필요한 ini 파일이 있는지 체크한다. 없으면 새로 생성한다
     */

    private fun getStrFromFile(aFile: File): String {
        val fis = FileInputStream(aFile)
        val buffer = ByteArray(fis.available())
        fis.read(buffer)
        fis.close()
        return String(buffer)

    }


    var diagPLList: ArrayList<DiagData> = arrayListOf<DiagData>()
    var conditionList: ArrayList<ConditionData> = arrayListOf<ConditionData>()
    var equipmentList: ArrayList<EquipmentData> = arrayListOf<EquipmentData>()
    var diagVoltDistList: ArrayList<VoltData> = arrayListOf<VoltData>()
    var diagVoltTransList: ArrayList<VoltData> = arrayListOf<VoltData>()



    private fun loadDefaultTransVolt() {
        diagVoltTransList.clear()
        diagVoltTransList.add(VoltData( volt= 5000f, value = 1.0f))
        diagVoltTransList.add(VoltData( volt=10000f, value = 2.0f))
        diagVoltTransList.add(VoltData( volt=15000f, value = 3.0f))
        diagVoltTransList.add(VoltData( volt=9999000f, value = 4.0f))
    }

    private fun loadDefaultDistVolt() {
        diagVoltDistList.clear()
        diagVoltDistList.add(VoltData( volt= 5000f, value = 1.0f))
        diagVoltDistList.add(VoltData( volt=10000f, value = 2.0f))
        diagVoltDistList.add(VoltData( volt=15000f, value = 3.0f))
        diagVoltDistList.add(VoltData( volt=9999000f, value = 4.0f))
    }



    private fun loadDefaultPl(context: Context) {
        diagPLList.clear()
        var msg = context.getResources().getString(R.string.Reinspect_within_1_week)
        diagPLList.add(DiagData( id=1, name="PL1", value=2.0f, msg=msg))


        msg = context.getResources().getString(R.string.Reinspect_within_1_month)
        diagPLList.add(DiagData( id=2, name="PL2", value=-1.0f, msg=msg))

        msg = context.getResources().getString(R.string.Reinspect_within_3_Months)
        diagPLList.add(DiagData( id=3, name="PL3", value=-3.5f, msg=msg))

        msg = context.getResources().getString(R.string.Reinspect_Within_6_Months)
        diagPLList.add(DiagData( id=4, name="PL4", value=-6.0f, msg=msg))

        msg = context.getResources().getString(R.string.Reinspect_Within_9_Months)
        diagPLList.add(DiagData( id=5, name="PL5", value=-8.49f, msg=msg))

        msg = context.getResources().getString(R.string.Reinspect_within_1_Year)
        diagPLList.add(DiagData( id=6, name="PL6", value=-8.5f, msg=msg))


    }

    private fun loadDefaultCondition(context: Context) {
        conditionList.clear()

        var txt = context.resources.getString(R.string.Crack)
        conditionList.add(ConditionData( name=txt, value=0.315f))

        txt = context.resources.getString(R.string.Erosion)
        conditionList.add(ConditionData( name=txt, value = 0.226f))

        txt = context.resources.getString(R.string.Overheating)
        conditionList.add(ConditionData( name=txt, value = 0.200f))

        txt = context.resources.getString(R.string.Flashover)
        conditionList.add(ConditionData( name=txt, value = 0.200f))

        txt = context.resources.getString(R.string.Contamination)
        conditionList.add(ConditionData( name=txt, value = 0.045f))

        txt = context.resources.getString(R.string.Non_glass)
        conditionList.add(ConditionData( name=txt, value = 0.028f))

        txt = context.resources.getString(R.string.Damaged)
        conditionList.add(ConditionData( name=txt, value = 0.250f))

        txt = context.resources.getString(R.string.Corrosion)
        conditionList.add(ConditionData( name=txt, value = 0.153f))

        txt = context.resources.getString(R.string.Incorrect_installation)
        conditionList.add(ConditionData( name=txt, value = 0.048f))

        txt = context.resources.getString(R.string.Out_of_Position)
        conditionList.add(ConditionData( name=txt, value = 0.048f))

        txt = context.resources.getString(R.string.Foreign_substance)
        conditionList.add(ConditionData( name=txt, value = 0.028f))

        txt = context.resources.getString(R.string.Vegetation_contact)
        conditionList.add(ConditionData( name=txt, value = 0.028f))

        txt = context.resources.getString(R.string.Normal)
        conditionList.add(ConditionData( name=txt, value = 0.0f))

        txt = context.resources.getString(R.string.Carbonization)
        conditionList.add(ConditionData( name=txt, value = 0.086f))

        txt = context.resources.getString(R.string.surface_peeling)
        conditionList.add(ConditionData( name=txt, value = 0.037f))

        txt = context.resources.getString(R.string.Etc)
        conditionList.add(ConditionData( name=txt, value = 0.016f))
    }

    private fun loadDefaultEquipment(context: Context) {
        equipmentList.clear()

        var txt = ""
        var tx2 = ""

        txt = context.resources.getString(R.string.Sheath_wire)
        equipmentList.add(EquipmentData( eType=1, name=txt, imgName = "item_equip_01",  rfRate=0.943f ))

        txt = context.resources.getString(R.string.Suspension_Insulator)
        equipmentList.add(EquipmentData( eType=1, name=txt, imgName = "item_equip_02",  rfRate=0.83f, value = 0.105f ))

        txt = context.resources.getString(R.string.Line_Post_Insulator)
        equipmentList.add(EquipmentData( eType=1, name=txt, imgName = "item_equip_03",  rfRate=0.86f, value = 0.297f ))

        txt = context.resources.getString(R.string.Lightning_Arrestor)
        equipmentList.add(EquipmentData( eType=1, name=txt, imgName = "item_equip_04",  rfRate=0.852f ))

        txt = context.resources.getString(R.string.Cut_Out_Switch)
        equipmentList.add(EquipmentData( eType=1, name=txt, imgName = "item_equip_05",  rfRate=0.86f, value = 0.091f ))

        txt = context.resources.getString(R.string.Steel_Crossarm)
        equipmentList.add(EquipmentData( eType=1, name=txt, imgName = "item_equip_06",  rfRate=0.3f ))

        txt = context.resources.getString(R.string.Covers)
        equipmentList.add(EquipmentData( eType=1, name=txt, imgName = "item_equip_07",  rfRate=0.945f ))

        txt = context.resources.getString(R.string.Polymer)
        equipmentList.add(EquipmentData( eType=1, name=txt, imgName = "item_equip_08",  rfRate=0.94f ))

        txt = context.resources.getString(R.string.MCCB)
        tx2 = context.resources.getString(R.string.Terminal_part)
        equipmentList.add(EquipmentData( id=1, eType=2, name=txt, subName = tx2, imgName = "item_equip2_01_00", baseOndo = 60f, rfRate=0.945f ))
//        EquipmentData( id=2, eType=2, name="커버나이프 스위치", subName = "단자부", imgName = "", baseOndo = 50f, rfRate=0.8f,value = 0.07f ))
//        EquipmentData( id=2, eType=2, name="커버나이프 스위치", subName = "개폐접촉부", imgName = "", baseOndo = 50f, rfRate=0.8f,value = 0.07f ))
//        EquipmentData( id=2, eType=2, name="커버나이프 스위치", subName = "퓨즈나사머리부", imgName = "", baseOndo = 60f, rfRate=0.8f,value = 0.07f ))

        txt = context.resources.getString(R.string.Power_Fuse)
        tx2 = context.resources.getString(R.string.Connection)
        equipmentList.add(EquipmentData( id=3, eType=2, name=txt, imgName = "item_equip2_02_01", baseOndo = 75f, rfRate=0.8f ))

        txt = context.resources.getString(R.string.Power_Fuse)
        tx2 = context.resources.getString(R.string.Contact_Part)
        equipmentList.add(EquipmentData( id=3, eType=2, name=txt, subName = "", imgName = "item_equip2_02_02", baseOndo = 80f, rfRate=0.8f ))

        txt = context.resources.getString(R.string.Power_Fuse)
        tx2 = context.resources.getString(R.string.Mechanical_Structural_Part)
        equipmentList.add(EquipmentData( id=3, eType=2, name=txt, subName = "", imgName = "item_equip2_02_03", baseOndo = 90f, rfRate=0.9f ))

        txt = context.resources.getString(R.string.Instrument_Transformer)
        tx2 = context.resources.getString(R.string.Terminal_part)
        equipmentList.add(EquipmentData( id=4, eType=2, name=txt, subName = "", imgName = "item_equip2_03_01", baseOndo = 75f, rfRate=0.9f ))

        txt = context.resources.getString(R.string.Instrument_Transformer)
        tx2 = context.resources.getString(R.string.Main_Body)
        equipmentList.add(EquipmentData( id=4, eType=2, name=txt, subName = "", imgName = "item_equip2_03_02", baseOndo = 95f, rfRate=0.6f ))

        txt = context.resources.getString(R.string.Transformer_Surface_TEMP)
        equipmentList.add(EquipmentData( id=5, eType=2, name=txt, subName = "", imgName = "item_equip2_04_00", baseOndo = 95f, rfRate=0.6f, value = 0.045f))

        txt = context.resources.getString(R.string.MOLD_Transformer)
        tx2 = context.resources.getString(R.string.Iron_core)
        equipmentList.add(EquipmentData( id=6, eType=2, name=txt, subName = "", imgName = "item_equip2_05_01", baseOndo = 120f, rfRate=0.95f, value = 0.045f ))

        txt = context.resources.getString(R.string.MOLD_Transformer)
        tx2 = context.resources.getString(R.string.Epoxy_Surface_Type_B)
        equipmentList.add(EquipmentData( id=6, eType=2, name=txt, subName = "", imgName = "item_equip2_05_02", baseOndo = 80f, rfRate=0.9f,value = 0.045f  ))

        txt = context.resources.getString(R.string.Cables)
        tx2 = context.resources.getString(R.string.Sheath_wire)
        equipmentList.add(EquipmentData( id=7, eType=2, name=txt, subName = " IV", imgName = "item_equip2_06_01", baseOndo = 60f, rfRate=0.943f, value = 0.297f ))

        txt = context.resources.getString(R.string.Cables)
        tx2 = context.resources.getString(R.string.Sheath_wire)
        equipmentList.add(EquipmentData( id=7, eType=2, name=txt, subName = " HIV", imgName = "item_equip2_06_02", baseOndo = 75f, rfRate=0.943f, value = 0.297f ))

        txt = context.resources.getString(R.string.Cables)
        tx2 = context.resources.getString(R.string.Sheath_wire)
        equipmentList.add(EquipmentData( id=7, eType=2, name=txt, subName = " EV", imgName = "item_equip2_06_03", baseOndo = 75f, rfRate=0.943f, value = 0.297f ))

        txt = context.resources.getString(R.string.Cables)
        tx2 = context.resources.getString(R.string.Sheath_wire)
        equipmentList.add(EquipmentData( id=7, eType=2, name=txt, subName = " CV", imgName = "item_equip2_06_04", baseOndo = 90f, rfRate=0.943f, value = 0.297f ))

        txt = context.resources.getString(R.string.Cables)
        tx2 = context.resources.getString(R.string.Sheath_wire)
        equipmentList.add(EquipmentData( id=7, eType=2, name=txt, subName = " VVF", imgName = "item_equip2_06_05", baseOndo = 60f, rfRate=0.943f, value = 0.297f ))

        txt = context.resources.getString(R.string.Condensor)
        tx2 = context.resources.getString(R.string.Terminal_part)
        equipmentList.add(EquipmentData( id=7, eType=2, name=txt, subName = "", imgName = "item_equip2_07_01", baseOndo = 75f, rfRate=0.94f ))

        txt = context.resources.getString(R.string.Condensor)
        tx2 = context.resources.getString(R.string.Main_Body)
        equipmentList.add(EquipmentData( id=6, eType=2, name=txt, subName = "", imgName = "item_equip2_07_02", baseOndo = 65f, rfRate=0.25f ))

    }



    private fun loadPlData(context: Context) {
        val plFile = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/" + Consts.DIAGNOSIS_PL)
        if (true) {
            // 파일이 없으면 생성한다

            loadDefaultPl(context)
            var pllist = PLList(diagPLList)

            val gson = Gson()
            val json = gson.toJson(pllist, PLList::class.java)

            val buf = BufferedWriter(FileWriter(plFile, false))
            buf.append(json)
            buf.close()
        } else {
            try {
                val str = getStrFromFile(plFile)
                val pl = Gson().fromJson(str, PLList::class.java)
                diagPLList.clear()
                for (i in 0 until pl.pls.size) {
                    diagPLList.add(pl.pls[i])
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadDefaultCondition(context)
            }
        }

        States.diagResult4List.clear()
        var msg = context.getResources().getString(R.string.Reinspect_within_1_Year)
        var ma = context.getResources().getString(R.string.Normal)
        States.diagResult4List.add(DiagData( id=1, name=ma, msg=msg))

        ma = context.getResources().getString(R.string.Possibility_of_deterioration)
        msg = context.getResources().getString(R.string.Reinspect_within_1_month)
        States.diagResult4List.add(DiagData( id=2, name=ma, msg=msg))

        ma = context.getResources().getString(R.string.Subsequent_Defects)
        msg = context.getResources().getString(R.string.Reinspect_within_1_week)
        States.diagResult4List.add(DiagData( id=3, name=ma, msg=msg))

        ma = context.getResources().getString(R.string.Faulty)
        msg = context.getResources().getString(R.string.Repair_Immediately)
        States.diagResult4List.add(DiagData( id=4, name=ma, msg=msg))

        States.diagResult3List.clear()
        ma = context.getResources().getString(R.string.Normal)
        msg = context.getResources().getString(R.string.Reinspect_within_1_Year)
        States.diagResult3List.add(DiagData( id=1, name=ma, msg=msg))

        ma = context.getResources().getString(R.string.Caution)
        msg = context.getResources().getString(R.string.Reinspect_within_3_Months)
        States.diagResult3List.add(DiagData( id=2, name=ma, msg=msg))

        ma = context.getResources().getString(R.string.Faulty)
        msg = context.getResources().getString(R.string.Repair_Immediately)
        States.diagResult3List.add(DiagData( id=3, name=ma, msg=msg))
    }


    private fun loadConditionData(context: Context) {
        val hFile = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/" + Consts.DIAGNOSIS_COND)
        if (true) {
            // 파일이 없으면 생성한다

            loadDefaultCondition(context)
            var condlist = CondList(conditionList)

            val gson = Gson()
            val json = gson.toJson(condlist, CondList::class.java)

            val buf = BufferedWriter(FileWriter(hFile, false))
            buf.append(json)
            buf.close()
        } else {
            try {
                val str = getStrFromFile(hFile)
                val cond = Gson().fromJson(str, CondList::class.java)
                conditionList.clear()
                for (i in 0 until cond.conds.size) {
                    conditionList.add(cond.conds[i])
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadDefaultCondition(context)
            }
        }
    }




    private fun loadEquipData(context: Context) {
        val hFile = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/" + Consts.DIAGNOSIS_EQUIP)
        if (true) {
            // 파일이 없으면 생성한다

            loadDefaultEquipment(context)
            var equiplist = EquipList(equipmentList)

            val gson = Gson()
            val json = gson.toJson(equiplist, EquipList::class.java)

            val buf = BufferedWriter(FileWriter(hFile, false))
            buf.append(json)
            buf.close()
        } else {
            try {
                val str = getStrFromFile(hFile)
                val equip = Gson().fromJson(str, EquipList::class.java)
                equipmentList.clear()
                for (i in 0 until equip.lists.size) {
                    equipmentList.add(equip.lists[i])
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadDefaultEquipment(context)
            }
        }
    }



    private fun loadDistVoltData() {
        val hFile = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/" + Consts.DIAGNOSIS_DVOLT)
        if (true) {
            // 파일이 없으면 생성한다

            loadDefaultDistVolt()
            var voltlist = VoltList(diagVoltDistList)

            val gson = Gson()
            val json = gson.toJson(voltlist, VoltList::class.java)

            val buf = BufferedWriter(FileWriter(hFile, false))
            buf.append(json)
            buf.close()
        } else {
            try {
                val str = getStrFromFile(hFile)
                val volt = Gson().fromJson(str, VoltList::class.java)
                diagVoltDistList.clear()
                for (i in 0 until volt.lists.size) {
                    diagVoltDistList.add(volt.lists[i])
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadDefaultDistVolt()
            }
        }
    }



    private fun loadTransVoltData() {
        val hFile = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/" + Consts.DIAGNOSIS_TVOLT)
        if (true) {
            // 파일이 없으면 생성한다

            loadDefaultTransVolt()
            var voltlist = VoltList(diagVoltTransList)

            val gson = Gson()
            val json = gson.toJson(voltlist, VoltList::class.java)

            val buf = BufferedWriter(FileWriter(hFile, false))
            buf.append(json)
            buf.close()
        } else {
            try {
                val str = getStrFromFile(hFile)
                val volt = Gson().fromJson(str, VoltList::class.java)
                diagVoltTransList.clear()
                for (i in 0 until volt.lists.size) {
                    diagVoltTransList.add(volt.lists[i])
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadDefaultTransVolt()
            }
        }
    }


    fun checkIniFile(context: Context) {

        Log.d("bobopro","Check Ini File...")
        // 루트 폴더 체크
        val folder = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/")
        if (!folder.exists()) {
            folder.mkdir()
        }

        loadPlData(context)
        loadConditionData(context)
        loadEquipData(context)

        loadDistVoltData()
        loadTransVoltData()

    }
}