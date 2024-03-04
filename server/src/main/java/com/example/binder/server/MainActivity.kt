package com.example.binder.server

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.TextView
import com.example.binder.server.MessageService.LocalService

class MainActivity : AppCompatActivity(), ServiceConnection, MessageService.MeasurementListener {

    private var mService: LocalService? = null
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.measurements_list)
        val bindIntent = Intent(this, MessageService::class.java).apply {
            action = MessageService.ACTION_BIND_LOCAL
        }
        bindService(bindIntent, this, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        mService?.unregisterMeasurementListener(this)
    }

    override fun onServiceDisconnected(name: ComponentName) = Unit

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        mService = service as LocalService
        mService?.registerMeasurementListener(this)
    }

    override fun onMeasurementObtained(measurement: Long) {

        runOnUiThread {
            textView.append("${String.format("%.3f", measurement)} msgs/s\n")
        }
    }
}
