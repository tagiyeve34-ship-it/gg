package com.hesabat.twopersonmessenger
import android.content.Context

object Session {
    private const val P="session"
    fun save(c:Context,t:String,uid:Int,username:String=""){
        c.getSharedPreferences(P,0).edit()
            .putString("token",t).putInt("uid",uid).putString("username",username).apply()
    }
    fun token(c:Context)=c.getSharedPreferences(P,0).getString("token","")?:""
    fun uid(c:Context)=c.getSharedPreferences(P,0).getInt("uid",0)
    fun username(c:Context)=c.getSharedPreferences(P,0).getString("username","")?:""
    fun clear(c:Context)=c.getSharedPreferences(P,0).edit().clear().apply()
}