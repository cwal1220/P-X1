package com.inspeco.data


import java.io.Serializable


data class EquipmentData (var id:Int=0,
                          var name:String = "",
                          var subName:String = "",
                          var eType:Int = 0,
                          var imgName:String = "",
                          var hasSub:Boolean = false,
                          var baseOndo:Float = 0f,
                          var faultType:Float = 0f,
                          var faultType0:Float = 0f,
                          var rfRate:Float = 0f,
                          var value:Float = 0f,
) : Serializable



data class ConditionData (var id:Int=0,
                          var name:String = "",
                          var value:Float = 0f,
) : Serializable

data class MaterialData (var id:Int=0,
                      var name:String = "",
                      var value:Float = 0f,
) : Serializable


data class VoltData (var id:Int=0,
                          var volt:Float = 0f,
                          var value:Float = 0f,
)


data class DiagData (var id:Int=0,
                   var name:String = "",
                     var msg:String = "",
                   var value:Float = 0f,
)

data class WaveFileInfo (
        var realDb:Float = 0f,
        var humi:Float = 0f,
        var ondo:Float = 0f,
)


data class DiagMixResultData (
                     var pl:Int = 0,
                     var on3:Int = 0,
                     var on4:Int = 0,
                     var result:Int = 0,
)



class UDR(
        var diagType: Int,
        var ondoPattern:Int,
        var waveData: FileData,
        var imageData1: FileData,
        var waveOndo:Float,
        var waveHumi:Float,
        var distance: Float,
        var lati: Float,
        var longi: Float,
        var equipment: String,
        var material:String,
        var faultTypeStr:String,
        var volt:Float,
        var ondoA:Float,
        var ondoB:Float,
        var ondoStr1: String,
        var ondoStr2: String,
        var ondoStr3: String,

        var realDb:Float,
        var detectionDB : Float,
        var correctionDB : Float ,
        var effectiveDB: Float ,
        var level: String,
        var mixResultIndex: Int,
        var waveResultIndex: Int,
        var ondoResultIndex: Int,

        ) : Serializable




class Report(
        var udr: UDR,

        var imageData2: FileData,
        var reportDate: String,
        var reportLineName: String,
        var reportPoleNo: String,
        var reportWeather: String,
        var reportMemo: String,

) : Serializable