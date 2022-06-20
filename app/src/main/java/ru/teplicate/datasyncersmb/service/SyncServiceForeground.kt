package ru.teplicate.datasyncersmb.service

import android.app.*
import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.teplicate.datasyncersmb.R
import ru.teplicate.datasyncersmb.broadcast.SyncBroadcastReceiver
import ru.teplicate.datasyncersmb.database.entity.SynchronizationUnit
import ru.teplicate.datasyncersmb.manager.SyncManager
import ru.teplicate.datasyncersmb.smb.SmbProcessor

const val NOTIFICATION_ID = 1111
const val NOTIFICATION_CHANNEL_ID = "channel"
const val SYNC_UNIT_KEY = "SyncUnitKey"
const val CANCEL_ACTION = "ACTION_CANCEL"

class SyncServiceForeground(
) : Service(), KoinComponent {

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    private var notificationBuilder: NotificationCompat.Builder? = null
    private val notificationManager by lazy { this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager }
    private var totalSyncElements: Int = 0
    private var totalSynced = 0
    private val syncManager: SyncManager by inject()
    private val broadcastReceiver: SyncBroadcastReceiver by lazy { SyncBroadcastReceiver(this) }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            val syncHandler = syncHandler()
            val sUnit = msg.obj as SynchronizationUnit
            syncManager.syncContentFromDirectory(sUnit, syncHandler)
            stopSelf(msg.arg1)
        }
    }

    private fun syncHandler() = object : SmbProcessor.SyncEventHandler() {
        override fun processedWithException(
            docFile: DocumentFile,
            syncUnit: SynchronizationUnit
        ) {
            //write fail to db
            //process exception
        }

        override fun successfulUploadFile(docFile: DocumentFile) {
            totalSynced++
            notifyFileUpload(totalSynced)
        }

        override fun onStartSync(totalElements: Int) {
            totalSyncElements = totalElements
            notifyFileUpload(0)
        }

        override fun onSyncComplete(syncUnit: SynchronizationUnit) {
            notifyFileUpload(totalSyncElements)
        }
    }

    private fun notifyFileUpload(current: Int) {
        notificationBuilder?.setProgress(totalSyncElements, current, false)
        notificationManager.notify(
            NOTIFICATION_ID,
            requireNotNull(notificationBuilder).build()
        )
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onCreate() {
        super.onCreate()
        registerBroadcast()
        createNotificationChannel()

        HandlerThread("", Process.THREAD_PRIORITY_FOREGROUND)
            .apply {
                start()
                serviceLooper = looper
                serviceHandler = ServiceHandler(looper)
            }
    }

    private fun registerBroadcast() {
        val intentFilter = IntentFilter(CANCEL_ACTION)
        registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun unregisterBroadcast() {
        unregisterReceiver(broadcastReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        val syncUnit = intent?.extras?.getParcelable(SYNC_UNIT_KEY) as SynchronizationUnit?
        requireNotNull(syncUnit)

        with(serviceHandler as ServiceHandler) {
            val message = this.obtainMessage()
            message.arg1 = startId
            message.obj = syncUnit

            this.sendMessage(message)
        }

        return START_REDELIVER_INTENT
    }


    private fun buildNotification(): Notification {
        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cached)
            .setContentTitle(getText(R.string.sync_notification_title))
            .addAction(R.drawable.ic_close_fill, "Cancel", createCancelPendingIntent())
            .setContentText("")

        return requireNotNull(notificationBuilder).build()
    }

    private fun createCancelPendingIntent(): PendingIntent {
        val cancelIntent = Intent(CANCEL_ACTION)

        return PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_NONE
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system

            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcast()
    }
}