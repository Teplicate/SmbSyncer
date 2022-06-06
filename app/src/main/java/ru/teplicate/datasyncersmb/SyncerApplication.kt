package ru.teplicate.datasyncersmb

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.teplicate.datasyncersmb.koin_module.roomModule
import ru.teplicate.datasyncersmb.koin_module.vmModule

class SyncerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SyncerApplication)
            modules(vmModule, roomModule)
        }
    }
}