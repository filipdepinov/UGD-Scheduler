package com.ugd.scheduler.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.ugd.scheduler.R
import com.ugd.scheduler.models.Subject

class ScheduleAdapter(
    private val onItemClick: (Subject) -> Unit
) : BaseAdapter() {

    private var items = listOf<Subject>()

    fun updateData(newItems: List<Subject>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getCount() = items.size
    override fun getItem(position: Int) = items[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)

        val subject = items[position]
        view.findViewById<TextView>(R.id.tv_subject_name).text = subject.name
        view.findViewById<TextView>(R.id.tv_time).text = "${subject.startTime} - ${subject.endTime}"
        view.findViewById<TextView>(R.id.tv_room).text = "📍 ${subject.room}, ${subject.building}"

        view.setOnClickListener { onItemClick(subject) }
        return view
    }
}
