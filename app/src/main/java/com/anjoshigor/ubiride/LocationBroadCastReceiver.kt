package com.anjoshigor.ubiride

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.awareness.fence.FenceState

/**
 * Created by anjoshigor on 25/11/17.
 */
class LocationBroadCastReceiver : BroadcastReceiver() {


    fun handleFalse(context: Context?, key: String) {
        //TODO("Atualizar o banco de dados")
        //LANCAR NOTIFICACAO
        when (key) {
            "headphonekey" -> Toast.makeText(context, "Não está mais com o fone", Toast.LENGTH_LONG).show()
            "walkingkey" -> Toast.makeText(context, "Não está mais andando", Toast.LENGTH_LONG).show()
            "homekey" -> Toast.makeText(context, "Não está mais em casa", Toast.LENGTH_LONG).show()
            else -> {

            }
        }
    }

    fun handleTrue(context: Context?, key: String) {
        //TODO("Atualizar o banco de dados")
        //LANCAR NOTIFICACAO
        when (key) {
            "headphonekey" -> Toast.makeText(context, "Está com o fone", Toast.LENGTH_LONG).show()
            "walkingkey" -> Toast.makeText(context, "Está andando", Toast.LENGTH_LONG).show()
            "homekey" -> Toast.makeText(context, "Está em casa", Toast.LENGTH_LONG).show()
            else -> {

            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        var state: FenceState = FenceState.extract(intent)
        Log.d("Awareness", "Fence Receiver Received")


        when (state.currentState) {
            FenceState.TRUE -> handleTrue(context, state.fenceKey)
            FenceState.FALSE -> handleFalse(context, state.fenceKey)
            else -> {
                Toast.makeText(context, "NADA", Toast.LENGTH_LONG).show()
            }


        }
    }
}