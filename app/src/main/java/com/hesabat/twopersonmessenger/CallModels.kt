package com.hesabat.twopersonmessenger

data class CallStartResponse(val ok:Boolean=false, val call_uuid:String="", val call_id:Long=0)
data class IncomingCall(val id:Long=0, val call_uuid:String="", val caller_id:Int=0, val call_type:String="audio", val started_at:String="")
data class IncomingResponse(val ok:Boolean=false, val call:IncomingCall?=null)
data class CallSignal(val id:Long=0, val call_uuid:String="", val signal_type:String="", val payload:Any?=null)
data class SignalResponse(val ok:Boolean=false, val signals:List<CallSignal> = emptyList())
data class IceServerDto(val urls:List<String> = emptyList(), val username:String?=null, val credential:String?=null)
data class IceConfigResponse(val ok:Boolean=false, val iceServers:List<IceServerDto> = emptyList())
