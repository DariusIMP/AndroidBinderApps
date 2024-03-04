package com.example.binder.server

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.annotation.BinderThread
import androidx.annotation.Nullable
import com.example.binder.api.*

class MessageService : Service() {

    companion object {
        private val TAG = MessageService::javaClass.name
        const val ACTION_BIND_LOCAL = "com.example.binder.server.BIND_LOCAL"
        const val ACTION_BIND_REMOTE = "com.example.binder.server.BIND_REMOTE"
        private const val NANOS_TO_SEC = 1_000_000_000L
    }

    private val mMeasurementListeners = mutableListOf<MeasurementListener>()

    private val mLocalService = LocalService()
    private val mRemoteService = RemoteService()

    private val n = 50000L
    private var batchCount = 0
    private var count = 0
    private var startTimestampNs: Long = 0
    private var globalStartTimestampNs: Long = 0

    private fun listener() {
        if (count == 0) {
            startTimestampNs = System.nanoTime()
            if (globalStartTimestampNs == 0L) {
                globalStartTimestampNs = startTimestampNs
            }
            count++
            return
        }
        if (count < n) {
            count++
            return
        }
        val stop = System.nanoTime()
        val msgs = n * NANOS_TO_SEC / (stop - startTimestampNs)
        mMeasurementListeners.forEach {
            it.onMeasurementObtained(msgs)
        }
        Log.i(TAG, "$msgs msgs/sec")
        batchCount++
        count = 0
    }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return when (intent.action) {
            ACTION_BIND_REMOTE -> mRemoteService
            ACTION_BIND_LOCAL -> mLocalService
            else -> null
        }
    }

    inner class LocalService : Binder() {
        fun registerMeasurementListener(listener: MeasurementListener) {
            mMeasurementListeners.add(listener)
        }

        fun unregisterMeasurementListener(listener: MeasurementListener) {
            mMeasurementListeners.remove(listener)
        }
    }

    inner class RemoteService : BinderMessageInterface.Stub() {

        @BinderThread
        override fun sendMsg(message: Message) {
            listener()
        }
    }

    interface MeasurementListener {
        fun onMeasurementObtained(measurement: Long)
    }
}
