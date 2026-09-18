package com.hesabat.twopersonmessenger
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.hesabat.twopersonmessenger.databinding.ItemMessageBinding
import java.text.SimpleDateFormat
import java.util.*
class MessageAdapter(private val myId:Int, private val onLong:(Message)->Unit):RecyclerView.Adapter<MessageAdapter.VH>(){
    val items=mutableListOf<Message>()
    class VH(val b:ItemMessageBinding):RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(p:ViewGroup,v:Int)=VH(ItemMessageBinding.inflate(LayoutInflater.from(p.context),p,false))
    override fun getItemCount()=items.size
    override fun onBindViewHolder(h:VH,pos:Int){
        val m=items[pos]; val mine=m.sender_id==myId
        h.b.text.text=m.text?:""
        val st=if(!mine) "" else when { m.read_at!=null->"  ✓✓"; m.delivered_at!=null->"  ✓✓"; else->"  ✓" }
        val raw=m.created_at?:""; val tm=runCatching { val d=SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).parse(raw); SimpleDateFormat("HH:mm",Locale.getDefault()).format(d!!) }.getOrElse { raw.takeLast(8).take(5) }
        h.b.meta.text=tm+st
        h.b.meta.setTextColor(Color.parseColor(if(mine) "#72C7E8" else "#8696A0"))
        h.b.bubble.background=GradientDrawable().apply { cornerRadius=16f; setColor(Color.parseColor(if(mine) "#005C4B" else "#202C33")) }
        (h.b.bubble.layoutParams as LinearLayout.LayoutParams).apply { gravity=if(mine) Gravity.END else Gravity.START; h.b.bubble.layoutParams=this }
        h.b.bubble.setOnLongClickListener { onLong(m); true }
    }
}