package com.hesabat.twopersonmessenger

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import com.hesabat.twopersonmessenger.databinding.ActivityDocumentBinding

class DocumentActivity:AppCompatActivity(){
    private lateinit var b:ActivityDocumentBinding
    private val items=mutableListOf<DocumentFile>()
    private var lastTap=0L

    private val treePicker=registerForActivityResult(ActivityResultContracts.OpenDocumentTree()){uri->
        if(uri!=null){
            contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)
            getSharedPreferences("docs",0).edit().putString("tree",uri.toString()).apply()
            loadDocs()
        }
    }

    override fun onCreate(s:Bundle?){
        super.onCreate(s); b=ActivityDocumentBinding.inflate(layoutInflater); setContentView(b.root)
        window.setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,android.view.WindowManager.LayoutParams.FLAG_SECURE)
        b.list.layoutManager=LinearLayoutManager(this)
        b.list.adapter=DocumentAdapter(items){openDoc(it)}
        b.title.setOnClickListener{
            val now=SystemClock.elapsedRealtime()
            if(now-lastTap<500){ lastTap=0; openHidden() } else lastTap=now
        }
        b.chooseFolder.setOnClickListener{treePicker.launch(null)}
        loadDocs()
    }

    override fun onResume() {
        super.onResume()
        getSharedPreferences("security",0).edit().putBoolean("return_to_documents",false).apply()
    }

    private fun openHidden(){
        if(Session.token(this).isBlank()) startActivity(Intent(this,LoginActivity::class.java))
        else startActivity(Intent(this,ChatActivity::class.java))
    }

    private fun loadDocs(){
        items.clear()
        val s=getSharedPreferences("docs",0).getString("tree",null)
        if(s==null){
            b.empty.visibility=View.VISIBLE
            b.empty.text="Telefon yaddaşından PDF-ləri göstərmək üçün qovluq seçin."
            b.list.adapter?.notifyDataSetChanged(); return
        }
        try{
            val root=DocumentFile.fromTreeUri(this,Uri.parse(s))
            if(root!=null) scan(root,0)
            items.sortByDescending{it.lastModified()}
            b.empty.visibility=if(items.isEmpty())View.VISIBLE else View.GONE
            if(items.isEmpty())b.empty.text="Seçilən qovluqda PDF tapılmadı."
            b.list.adapter?.notifyDataSetChanged()
        }catch(e:Exception){
            b.empty.visibility=View.VISIBLE;b.empty.text="Qovluğa giriş mümkün olmadı. Yenidən seçin."
        }
    }
    private fun scan(dir:DocumentFile,depth:Int){
        if(depth>5)return
        dir.listFiles().forEach{
            if(it.isDirectory) scan(it,depth+1)
            else if(it.name?.lowercase()?.endsWith(".pdf")==true || it.type=="application/pdf") items.add(it)
        }
    }
    private fun openDoc(d:DocumentFile){
        try{
            val i=Intent(Intent.ACTION_VIEW).setDataAndType(d.uri,"application/pdf")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(i)
        }catch(e:Exception){Toast.makeText(this,"PDF açmaq üçün uyğun proqram tapılmadı",Toast.LENGTH_SHORT).show()}
    }
}