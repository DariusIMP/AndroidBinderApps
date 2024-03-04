package com.example.binder.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.TextView
import com.example.binder.api.*

class MainActivity : AppCompatActivity(), ServiceConnection {

    private var mService: BinderMessageInterface? = null

    private lateinit var textView: TextView

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
        service?.let {
            mService = BinderMessageInterface.Stub.asInterface(service)
            while (true) {
                mService!!.sendMsg(Message().apply { mPayload = "01234567" })
            }
        }
    }
}
