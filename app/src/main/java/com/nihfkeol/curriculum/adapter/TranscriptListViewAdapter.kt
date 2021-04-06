package com.nihfkeol.curriculum.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.nihfkeol.curriculum.R
import com.nihfkeol.curriculum.pojo.ItemColor
import com.nihfkeol.curriculum.pojo.Transcript

class TranscriptListViewAdapter(
    val context: Context,
    private val transcriptList: List<Transcript>
) : BaseAdapter() {

    private val itemColors: List<ItemColor>

    init {
        val textColor = context.resources.getIntArray(R.array.listViewTextColors)
        val cardColor = context.resources.getIntArray(R.array.listViewItemColors)
        itemColors = ArrayList(cardColor.size)
        for (i in cardColor.indices) {
            itemColors.add(ItemColor(textColor[i], cardColor[i]))
        }
    }

    override fun getCount(): Int {
        return transcriptList.size
    }

    override fun getItem(position: Int): Any {
        return transcriptList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: MyViewHolder
        if (convertView == null) {
            view = View.inflate(
                context,
                R.layout.item_transcript,
                null
            )
            holder = MyViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as MyViewHolder
        }

        holder.also {
            it.courseTitleTextView.text = transcriptList[position].courseName
            it.scoreTextView.text = transcriptList[position].score
            if (position != 0) {
                with(itemColors[(position - 1) % 4]) {
                    it.courseTitleTextView.setTextColor(textColor)
                    it.scoreTextView.setTextColor(textColor)
                    it.transcriptItem.setCardBackgroundColor(cardViewColor)
                }
            }
        }
        return view
    }

    private class MyViewHolder(view: View) {
        val courseTitleTextView: TextView = view.findViewById(R.id.courseTitleTextView)
        val scoreTextView: TextView = view.findViewById(R.id.scoreTextView)
        val transcriptItem: CardView = view.findViewById(R.id.transcriptItem)
    }
}