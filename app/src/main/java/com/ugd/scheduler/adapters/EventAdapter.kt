package com.ugd.scheduler.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.ugd.scheduler.R
import com.ugd.scheduler.models.CalendarEvent

class EventAdapter(
    private val onDeleteClick: (CalendarEvent) -> Unit
) : BaseAdapter() {

    private var items = listOf<CalendarEvent>()

    fun updateData(newItems: List<CalendarEvent>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getCount() = items.size
    override fun getItem(position: Int) = items[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)

        val event = items[position]

        val typeIcon = when(event.type) {
            "exam" -> "📝"
            "colloquium" -> "📋"
            "deadline" -> "⏰"
            else -> "📌"
        }
        val typeName = when(event.type) {
            "exam" -> "Испит"
            "colloquium" -> "Колоквиум"
            "deadline" -> "Рок"
            else -> "Белешка"
        }

        view.findViewById<TextView>(R.id.tv_event_title).text = "$typeIcon ${event.title}"
        view.findViewById<TextView>(R.id.tv_event_type).text = typeName
        view.findViewById<TextView>(R.id.tv_event_subject).text =
            if (event.subjectName.isNotEmpty()) event.subjectName else "Без предмет"
        view.findViewById<TextView>(R.id.tv_event_time).text =
            if (event.time.isNotEmpty()) "🕐 ${event.time}" else ""

        view.findViewById<ImageButton>(R.id.btn_delete).setOnClickListener {
            onDeleteClick(event)
        }

        return view
    }
}
