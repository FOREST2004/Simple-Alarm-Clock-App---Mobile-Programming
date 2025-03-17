

package com.example.simplealarmclockapp

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.provider.AlarmClock


import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var selectedTimeTextView: TextView
    private lateinit var alarmMessageEditText: EditText
    private lateinit var setAlarmButton: Button
    private lateinit var alarmsRecyclerView: RecyclerView
    private lateinit var alarmAdapter: AlarmAdapter
    private val alarmsList = mutableListOf<AlarmItem>()
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        selectedTimeTextView = findViewById(R.id.selectedTimeTextView)
        alarmMessageEditText = findViewById(R.id.alarmMessageEditText)
        setAlarmButton = findViewById(R.id.setAlarmButton)
        alarmsRecyclerView = findViewById(R.id.alarmsRecyclerView)


        alarmAdapter = AlarmAdapter(
            alarmsList,
            onDeleteClickListener = { position -> deleteAlarm(position) },
            onSwitchChangeListener = { position, isActive -> updateAlarmStatus(position, isActive) }
        )
        alarmsRecyclerView.layoutManager = LinearLayoutManager(this)
        alarmsRecyclerView.adapter = alarmAdapter


        selectedTimeTextView.setOnClickListener { showTimePicker() }
        setAlarmButton.setOnClickListener { checkAndSetAlarm() }


        updateTimeDisplay()
    }

    private fun showTimePicker() {
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                updateTimeDisplay()
            },
            currentHour,
            currentMinute,
            true
        ).show()
    }

    private fun updateTimeDisplay() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        selectedTimeTextView.text = sdf.format(calendar.time)
    }

    private fun checkAndSetAlarm() {
        // Kiểm tra quyền đặt alarm cho Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    this,
                    "Please grant alarm permission in Settings",
                    Toast.LENGTH_LONG
                ).show()

                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return
            }
        }


        setAlarm()
    }

    private fun setAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = selectedTimeTextView.text.toString()
        val message = alarmMessageEditText.text.toString().ifEmpty { "Time's up!" }

        // Tạo Intent cho BroadcastReceiver
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("MESSAGE", message)
        }

        // Tạo PendingIntent
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val alarmTime = calendar.timeInMillis
            val currentTime = System.currentTimeMillis()

            if (alarmTime <= currentTime) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }


            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            val newAlarm = AlarmItem(time, message)
            alarmAdapter.addAlarm(newAlarm)


            alarmMessageEditText.text.clear()


            Toast.makeText(
                this,
                "Alarm set for $time",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                "Unable to set alarm. Please check permissions.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun deleteAlarm(position: Int) {
        alarmAdapter.removeAlarm(position)
        Toast.makeText(this, "Alarm deleted", Toast.LENGTH_SHORT).show()
    }

    private fun updateAlarmStatus(position: Int, isActive: Boolean) {

        val updatedAlarm = alarmsList[position].copy(isActive = isActive)
        alarmsList[position] = updatedAlarm
        Toast.makeText(
            this,
            if (isActive) "Alarm activated" else "Alarm deactivated",
            Toast.LENGTH_SHORT
        ).show()
    }
}