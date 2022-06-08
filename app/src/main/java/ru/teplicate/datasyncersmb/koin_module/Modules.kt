package ru.teplicate.datasyncersmb.koin_module

import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.teplicate.datasyncersmb.content_processor.ContentProcessor
import ru.teplicate.datasyncersmb.database.AppDatabase
import ru.teplicate.datasyncersmb.database.repository.SyncUnitRepository
import ru.teplicate.datasyncersmb.fragment.home_fragment.view_model.HomeViewModel
import ru.teplicate.datasyncersmb.fragment.scan_fragment.view_model.ScanNetworkViewModel
import ru.teplicate.datasyncersmb.fragment.setup_sync_fragment.view_model.SetupSyncViewModel
import ru.teplicate.datasyncersmb.fragment.shared_files_fragment.view_model.SharedFilesViewModel
import ru.teplicate.datasyncersmb.manager.DownloadManager
import ru.teplicate.datasyncersmb.manager.SyncManager
import ru.teplicate.datasyncersmb.network_scanner.NetworkScanner
import ru.teplicate.datasyncersmb.smb.SmbProcessor
import ru.teplicate.datasyncersmb.util.PermissionChecker

@JvmField
val vmModule = module {

    single { PermissionChecker() }
    single { NetworkScanner() }
    single { SmbProcessor() }
    single { ContentProcessor() }
    single { SyncUnitRepository(get()) }
    single { SyncManager(androidApplication(), get(), get()) }
    single { DownloadManager(androidApplication(), get()) }

    viewModel { ScanNetworkViewModel(get(), get()) }
    viewModel { SetupSyncViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { SharedFilesViewModel(get()) }
}


val roomModule = module {

    fun syncUnitDao(db: AppDatabase) = db.syncUnitDao()

    single { AppDatabase.getInstance(androidApplication()) }
    single { syncUnitDao(get()) }
}

val roomTestModule = module {
    single {
        Room.inMemoryDatabaseBuilder(androidApplication(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
    // In-Memory database config
}
