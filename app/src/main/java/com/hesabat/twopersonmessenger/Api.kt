package com.hesabat.twopersonmessenger

import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object Api {
    const val BASE_URL = "https://hesabat.site/wp/api/"
    private val client = OkHttpClient()
    private val gson = Gson()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    fun post(path:String, body:Any, token:String?=null, cb:(Boolean,String)->Unit){
        val rb=gson.toJson(body).toRequestBody(jsonType)
        val b=Request.Builder().url(BASE_URL+path).post(rb)
        if(!token.isNullOrBlank()) b.header("Authorization","Bearer $token")
        client.newCall(b.build()).enqueue(object:Callback{override fun onFailure(call:Call,e:IOException)=cb(false,e.message?:"NETWORK_ERROR"); override fun onResponse(call:Call,response:Response){response.use{cb(it.isSuccessful,it.body?.string().orEmpty())}}})
    }
    fun get(path:String, token:String, cb:(Boolean,String)->Unit){
        val r=Request.Builder().url(BASE_URL+path).header("Authorization","Bearer $token").get().build()
        client.newCall(r).enqueue(object:Callback{override fun onFailure(call:Call,e:IOException)=cb(false,e.message?:"NETWORK_ERROR"); override fun onResponse(call:Call,response:Response){response.use{cb(it.isSuccessful,it.body?.string().orEmpty())}}})
    }
}
