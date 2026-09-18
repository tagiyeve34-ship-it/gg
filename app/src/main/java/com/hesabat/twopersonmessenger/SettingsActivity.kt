package com.hesabat.twopersonmessenger
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hesabat.twopersonmessenger.databinding.ActivitySettingsBinding

class SettingsActivity:AppCompatActivity(){
    private lateinit var b:ActivitySettingsBinding
    override fun onCreate(s:Bundle?){
        super.onCreate(s);b=ActivitySettingsBinding.inflate(layoutInflater);setContentView(b.root)
        window.setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,android.view.WindowManager.LayoutParams.FLAG_SECURE)
        val p=getSharedPreferences("settings",0); val sec=getSharedPreferences("security",0)
        b.msgNotif.isChecked=p.getBoolean("msg",true);b.callSound.isChecked=p.getBoolean("call",true);b.readReceipt.isChecked=p.getBoolean("read",true)
        val save={p.edit().putBoolean("msg",b.msgNotif.isChecked).putBoolean("call",b.callSound.isChecked).putBoolean("read",b.readReceipt.isChecked).apply()}
        b.msgNotif.setOnCheckedChangeListener{_,_->save()};b.callSound.setOnCheckedChangeListener{_,_->save()};b.readReceipt.setOnCheckedChangeListener{_,_->save()}
        val current=sec.getLong("auto_clear_ms",0L)
        val values=longArrayOf(0,60_000,5*60_000,15*60_000,30*60_000,60*60_000)
        val labels=arrayOf("Söndürülüb","1 dəqiqə","5 dəqiqə","15 dəqiqə","30 dəqiqə","1 saat")
        b.autoClear.setSelection(values.indexOf(current).coerceAtLeast(0))
        b.autoClear.onItemSelectedListener=object:android.widget.AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent:android.widget.AdapterView<*>?){}
            override fun onItemSelected(parent:android.widget.AdapterView<*>?,view:android.view.View?,position:Int,id:Long){
                sec.edit().putLong("auto_clear_ms",values[position]).apply { if (values[position] == 0L) putLong("background_at",0L) }.apply()
            }
        }
        b.logoutBtn.setOnClickListener{
            Api.post("logout.php",emptyMap<String,String>(),Session.token(this)){_,_->}
            Session.clear(this)
            startActivity(Intent(this,DocumentActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK));finishAffinity()
        }
    }
}