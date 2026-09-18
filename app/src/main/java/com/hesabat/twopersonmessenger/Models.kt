package com.hesabat.twopersonmessenger

data class LoginResponse(val ok:Boolean=false,val token:String="",val user:User?=null,val error:String?=null)
data class User(val id:Int=0,val username:String="",val display_name:String="",val avatar_url:String?=null)
data class MessageResponse(val ok:Boolean=false,val messages:List<Message> = emptyList())
data class SendResponse(val ok:Boolean=false,val message:Message?=null,val error:String?=null)
data class Message(val id:Int=0,val sender_id:Int=0,val receiver_id:Int=0,val type:String="text",val text:String?="",val created_at:String?=null,val delivered_at:String?=null,val read_at:String?=null)
