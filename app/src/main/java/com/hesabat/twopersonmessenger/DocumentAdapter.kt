package com.hesabat.twopersonmessenger
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import com.hesabat.twopersonmessenger.databinding.ItemDocumentBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentAdapter(private val items:List<DocumentFile>,private val click:(DocumentFile)->Unit):RecyclerView.Adapter<DocumentAdapter.H>(){
    class H(val b:ItemDocumentBinding):RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(p:ViewGroup,v:Int)=H(ItemDocumentBinding.inflate(LayoutInflater.from(p.context),p,false))
    override fun getItemCount()=items.size
    override fun onBindViewHolder(h:H,pos:Int){
        val d=items[pos]; h.b.name.text=d.name?:"PDF"
        val kb=(d.length()/1024).coerceAtLeast(1)
        val date=if(d.lastModified()>0)SimpleDateFormat("dd.MM.yyyy",Locale.getDefault()).format(Date(d.lastModified())) else ""
        h.b.meta.text="$kb KB  •  $date"; h.b.root.setOnClickListener{click(d)}
    }
}