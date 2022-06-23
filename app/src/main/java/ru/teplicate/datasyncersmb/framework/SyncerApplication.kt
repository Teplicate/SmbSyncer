package ru.teplicate.datasyncersmb.framework

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.teplicate.datasyncersmb.framework.koin_module.dataModule
import ru.teplicate.datasyncersmb.framework.koin_module.interactorModule
import ru.teplicate.datasyncersmb.framework.koin_module.roomModule
import ru.teplicate.datasyncersmb.framework.koin_module.vmModule

class SyncerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SyncerApplication)
            modules(vmModule, roomModule, dataModule, interactorModule)
        }
    }
}