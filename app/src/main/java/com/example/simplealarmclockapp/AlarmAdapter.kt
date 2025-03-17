package com.example.simplealarmclockapp
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simplealarmclockapp.R

class AlarmAdapter(
    private val alarms: MutableList<AlarmItem>,
    private val onDeleteClickListener: (Int) -> Unit,
    private val onSwitchChangeListener: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(R.id.alarmTimeTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.alarmMessageTextView)
        val activeSwitch: Switch = itemView.findViewById(R.id.alarmActiveSwitch)
        val deleteButton: View = itemView.findViewById(R.id.deleteAlarmButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.timeTextView.text = alarm.time
        holder.messageTextView.text = alarm.message
        holder.activeSwitch.isChecked = alarm.isActive


        holder.activeSwitch.setOnCheckedChangeListener { _, isChecked ->
            onSwitchChangeListener(position, isChecked)
        }


        holder.deleteButton.setOnClickListener {
            onDeleteClickListener(position)
        }
    }

    override fun getItemCount() = alarms.size

    fun addAlarm(alarm: AlarmItem) {
        alarms.add(alarm)
        notifyItemInserted(alarms.size - 1)
    }

    fun removeAlarm(position: Int) {
        alarms.removeAt(position)
        notifyItemRemoved(position)
    }
}