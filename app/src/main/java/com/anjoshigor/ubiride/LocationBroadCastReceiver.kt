package com.anjoshigor.ubiride

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.android.gms.awareness.fence.FenceState

/**
 * Created by anjoshigor on 25/11/17.
 */
class LocationBroadCastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        var state: FenceState = FenceState.extract(intent)
        Log.d("Awareness", "Fence Receiver Received")

        if (TextUtils.equals(state.fenceKey, "myKey")) {
            when (state.currentState) {
                FenceState.TRUE -> Toast.makeText(context, "CHEGOU", Toast.LENGTH_LONG).show()
                FenceState.FALSE -> Toast.makeText(context, "SAIU", Toast.LENGTH_LONG).show()
                else -> {
                    Toast.makeText(context, "NADA", Toast.LENGTH_LONG).show()
                }
            }
        } else {

        }
    }
}