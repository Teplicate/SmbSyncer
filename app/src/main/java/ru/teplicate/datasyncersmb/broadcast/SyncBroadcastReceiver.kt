package ru.teplicate.datasyncersmb.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.teplicate.datasyncersmb.service.SyncServiceForeground

class SyncBroadcastReceiver(private val syncServiceForeground: SyncServiceForeground) :
    BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        syncServiceForeground.stopSelf()
    }
}