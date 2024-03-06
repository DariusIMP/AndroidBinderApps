package com.example.binder.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import com.example.binder.api.*

class MainActivity : AppCompatActivity(), ServiceConnection {

    companion object {
        private val TAG = MainActivity::javaClass.name
    }

    private var mService: BinderMessageInterface? = null

    private lateinit var textView: TextView

    private var startTimestampNs: Long = 0
    private var stopTimestampNs: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.text_view)

        val bindIntent = Intent("com.example.binder.server.BIND_REMOTE").apply {
            setPackage("com.example.binder.server")
        }
        bindService(bindIntent, this, Context.BIND_AUTO_CREATE)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        textView.append("Service disconnected...\n")
        mService = null
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder?) {
        textView.append("Sending measurements...\n")
        var measurements = mutableListOf<Long>()

        service?.let {
            mService = BinderMessageInterface.Stub.asInterface(service)
            var count = 0
            var outerTimeNs = System.nanoTime()
            while (true) {
                startTimestampNs = System.nanoTime()
                val _reply = mService!!.sendMsg("01234567")
                stopTimestampNs = System.nanoTime()
                measurements.add(stopTimestampNs - startTimestampNs)
                count++
                if (count == 1000) {
                    val avg = String.format("%.3f", measurements.average() / 1_000_000)
                    val measAmount = String.format("%.3f", count / ((stopTimestampNs - outerTimeNs).toDouble() / 1_000_000_000))
                    Log.i(TAG, "Ping = $avg ms | Throughput: $measAmount msgs/sec")
                    measurements = mutableListOf()
                    count = 0
                    outerTimeNs = System.nanoTime()
                }
            }
        }
    }
}
