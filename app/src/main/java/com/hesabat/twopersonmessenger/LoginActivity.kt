package com.hesabat.twopersonmessenger
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.hesabat.twopersonmessenger.databinding.ActivityLoginBinding

class LoginActivity:AppCompatActivity(){
    private lateinit var b:ActivityLoginBinding
    override fun onCreate(s:Bundle?){
        super.onCreate(s); b=ActivityLoginBinding.inflate(layoutInflater); setContentView(b.root)
        b.username.setText(Session.username(this))
        b.loginBtn.setOnClickListener{
            val u=b.username.text.toString().trim(); val p=b.pin.text.toString()
            if(u.isBlank()||p.isBlank()){b.error.text="İstifadəçi adı və PIN yaz";return@setOnClickListener}
            b.loginBtn.isEnabled=false
            Api.post("login.php",mapOf("username" to u,"pin" to p)){ok,raw->
                runOnUiThread{
                    b.loginBtn.isEnabled=true
                    if(ok){
                        val r=Gson().fromJson(raw,LoginResponse::class.java)
                        Session.save(this,r.token,r.user?.id?:0,u)
                        startActivity(Intent(this,ChatActivity::class.java)); finish()
                    } else b.error.text="Giriş alınmadı"
                }
            }
        }
    }
}