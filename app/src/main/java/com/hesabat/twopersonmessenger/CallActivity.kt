package com.hesabat.twopersonmessenger

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.hesabat.twopersonmessenger.databinding.ActivityCallBinding
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class CallActivity : AppCompatActivity() {
    private lateinit var b:ActivityCallBinding
    private val h=Handler(Looper.getMainLooper())
    private var rtc:WebRtcClient?=null
    private var callUuid=""
    private var incoming=false
    private var video=false
    private var lastSignal=0L
    private var muted=false
    private var speaker=false
    private var cameraOn=true
    private var started=false
    private val poll=object:Runnable{override fun run(){pollSignals();h.postDelayed(this,700)}}
    private val permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ if(it.values.all{v->v}) boot() else {Toast.makeText(this,"Mikrofon/kamera icazəsi lazımdır",Toast.LENGTH_LONG).show();finish()} }

    override fun onCreate(s:Bundle?){super.onCreate(s);b=ActivityCallBinding.inflate(layoutInflater);setContentView(b.root)
        window.setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,android.view.WindowManager.LayoutParams.FLAG_SECURE)
        incoming=intent.getBooleanExtra("incoming",false); video=intent.getBooleanExtra("video",false); callUuid=intent.getStringExtra("call_uuid")?:""
        b.callType.text=if(video)"Video zəng" else "Səsli zəng"; b.videoContainer.visibility=if(video)View.VISIBLE else View.GONE
        b.acceptBtn.visibility=if(incoming)View.VISIBLE else View.GONE; b.rejectBtn.visibility=if(incoming)View.VISIBLE else View.GONE
        b.controls.visibility=if(incoming)View.GONE else View.VISIBLE
        b.acceptBtn.setOnClickListener{b.acceptBtn.visibility=View.GONE;b.rejectBtn.visibility=View.GONE;b.controls.visibility=View.VISIBLE; acceptIncoming()}
        b.rejectBtn.setOnClickListener{signal("reject",null);finish()}; b.endBtn.setOnClickListener{signal("hangup",null);finish()}
        b.micBtn.setOnClickListener{muted=!muted;rtc?.mute(muted);b.micBtn.text=if(muted)"Mikrofon aç" else "Mikrofon"}
        b.speakerBtn.setOnClickListener{speaker=!speaker;(getSystemService(Context.AUDIO_SERVICE) as AudioManager).isSpeakerphoneOn=speaker;b.speakerBtn.text=if(speaker)"Səs: açıq" else "Səs"}
        b.cameraBtn.setOnClickListener{cameraOn=!cameraOn;rtc?.camera(cameraOn);b.cameraBtn.text=if(cameraOn)"Kamera" else "Kamera aç"}
        b.switchBtn.setOnClickListener{rtc?.switchCamera()}
        ensurePermissions()
    }
    private fun ensurePermissions(){val ps=mutableListOf(Manifest.permission.RECORD_AUDIO);if(video)ps+=Manifest.permission.CAMERA;val miss=ps.filter{ContextCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED};if(miss.isEmpty())boot() else permissionLauncher.launch(miss.toTypedArray())}
    private fun boot(){Api.get("ice_config.php",Session.token(this)){ok,raw->val ice=if(ok)runCatching{Gson().fromJson(raw,IceConfigResponse::class.java).iceServers}.getOrDefault(emptyList()) else emptyList();runOnUiThread{try { rtc=WebRtcClient(this,video,ice,{sendIce(it)},{st->runOnUiThread{b.callStatus.text=when(st){"CONNECTED"->"Qoşuldu";"DISCONNECTED"->"Bağlantı kəsildi";"FAILED"->"Bağlantı alınmadı";else->st}}},{ track -> if(video) runOnUiThread { track.addSink(b.remoteVideo) } }); if(video){rtc?.attachLocal(b.localVideo); rtc?.attachRemote(b.remoteVideo)}; h.post(poll); if(!incoming)startOutgoing() else b.callStatus.text="Gələn zəng" } catch(e:Throwable) { b.callStatus.text="Zəng modulu başladıla bilmədi"; Toast.makeText(this,"Zəng modulu başladıla bilmədi",Toast.LENGTH_LONG).show(); h.postDelayed({finish()},1200) }}}}
    private fun startOutgoing(){if(callUuid.isBlank()){Api.post("call_start.php",mapOf("call_type" to if(video)"video" else "audio"),Session.token(this)){ok,raw->if(ok){val r=Gson().fromJson(raw,CallStartResponse::class.java);callUuid=r.call_uuid;runOnUiThread{b.callStatus.text="Zəng gedir…"};createOffer()}else runOnUiThread{Toast.makeText(this,"Zəng başladıla bilmədi",Toast.LENGTH_SHORT).show();finish()}}}else createOffer()}
    private fun createOffer(){rtc?.createOffer{signal("offer",mapOf("sdp" to it.description))}}
    private fun acceptIncoming(){b.callStatus.text="Qoşulur…"; /* offer poll tərəfindən qəbul ediləcək */}
    private fun sendIce(c:IceCandidate){if(callUuid.isNotBlank())signal("ice",mapOf("sdpMid" to c.sdpMid,"sdpMLineIndex" to c.sdpMLineIndex,"candidate" to c.sdp))}
    private fun signal(type:String,payload:Any?){if(callUuid.isBlank())return;Api.post("call_signal.php",mapOf("call_uuid" to callUuid,"signal_type" to type,"payload" to payload),Session.token(this)){_,_->}}
    private fun pollSignals(){if(callUuid.isBlank())return;Api.get("call_poll.php?after_id=$lastSignal",Session.token(this)){ok,raw->if(!ok)return@get;val r=runCatching{Gson().fromJson(raw,SignalResponse::class.java)}.getOrNull()?:return@get;r.signals.filter{it.call_uuid==callUuid}.forEach{s->lastSignal=maxOf(lastSignal,s.id);handleSignal(s)}}}
    private fun handleSignal(s:CallSignal){when(s.signal_type){"offer"->{if(!incoming)return; val m=s.payload as? LinkedTreeMap<*,*> ?: return;val sdp=m["sdp"]?.toString()?:return;rtc?.setRemote("offer",sdp){rtc?.createAnswer{a->signal("answer",mapOf("sdp" to a.description));runOnUiThread{b.callStatus.text="Qoşulur…"}}}};"answer"->{val m=s.payload as? LinkedTreeMap<*,*> ?: return;val sdp=m["sdp"]?.toString()?:return;rtc?.setRemote("answer",sdp){runOnUiThread{if(video){rtc?.refreshRemote(b.remoteVideo)};b.callStatus.text="Qoşulur…"}}};"ice"->{val m=s.payload as? LinkedTreeMap<*,*> ?: return;val cand=m["candidate"]?.toString()?:return;val mid=m["sdpMid"]?.toString();val idx=(m["sdpMLineIndex"] as? Number)?.toInt()?:0;rtc?.addIce(mid,idx,cand);if(video)runOnUiThread{rtc?.refreshRemote(b.remoteVideo)}};"hangup","reject"->runOnUiThread{b.callStatus.text=if(s.signal_type=="reject")"Zəng rədd edildi" else "Zəng bitdi";h.postDelayed({finish()},500)}}}
    override fun onDestroy(){h.removeCallbacks(poll);rtc?.close();super.onDestroy()}
}
