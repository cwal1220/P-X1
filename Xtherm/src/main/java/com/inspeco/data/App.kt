package com.inspeco.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences


class MySharedPreferences(context: Context) {

    val PREFS_NAME = "X1"
    val PREF_KEY_USERS= "Users"

    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, 0)

//    fun setV(key:String, value:String?){
//        prefs.edit().putString(key,value).apply()
//    }
//
//    fun getV(key:String):String?{
//        return prefs.getString(key,"")
//    }

    fun saveUsers(value:String?){
        prefs.edit().putString(PREF_KEY_USERS,value).apply()
    }

    fun loadUsers():String?{
        return prefs.getString(PREF_KEY_USERS,"{[]}")
    }



//    var myEditText: String?
//        get() = prefs.getString(PREF_KEY_MY_EDITTEXT, "")
//        set(value) = prefs.edit().putString(PREF_KEY_MY_EDITTEXT, value).apply()

}

class App : Application() {

    companion object {
        lateinit var prefs : MySharedPreferences
    }

    override fun onCreate() {
        super.onCreate()
    }

    public fun initPrefs() {
        prefs = MySharedPreferences(applicationContext)
    }
}