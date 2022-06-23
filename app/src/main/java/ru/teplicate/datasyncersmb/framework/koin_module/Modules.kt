package ru.teplicate.datasyncersmb.framework.koin_module

import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.teplicate.core.data.FileProviderDatasource
import ru.teplicate.core.data.FileProviderRepository
import ru.teplicate.core.data.SyncUnitDataSource
import ru.teplicate.core.data.SyncUnitRepository
import ru.teplicate.core.interactors.CreateSyncUnit
import ru.teplicate.core.interactors.DeleteSyncUnit
import ru.teplicate.core.interactors.ReadSyncUnits
import ru.teplicate.core.interactors.UpdateSyncUnit
import ru.teplicate.datasyncersmb.content_processor.ContentProcessor
import ru.teplicate.datasyncersmb.framework.FileProviderDataSourceImpl
import ru.teplicate.datasyncersmb.presentation.fragment.home_fragment.view_model.HomeViewModel
import ru.teplicate.datasyncersmb.presentation.fragment.scan_fragment.view_model.ScanNetworkViewModel
import ru.teplicate.datasyncersmb.presentation.fragment.setup_sync_fragment.view_model.SetupSyncViewModel
import ru.teplicate.datasyncersmb.presentation.fragment.shared_files_fragment.view_model.SharedFilesViewModel
import ru.teplicate.datasyncersmb.framework.SyncUnitDatasourceImpl
import ru.teplicate.datasyncersmb.framework.SyncUnitInteractor
import ru.teplicate.datasyncersmb.framework.database.AppDatabase
import ru.teplicate.datasyncersmb.manager.DownloadManager
import ru.teplicate.datasyncersmb.manager.SyncManager
import ru.teplicate.datasyncersmb.network_scanner.NetworkScanner
import ru.teplicate.datasyncersmb.smb.SmbProcessor
import ru.teplicate.datasyncersmb.util.PermissionChecker

val dataModule = module {
    single { SyncUnitDatasourceImpl(get()) } bind SyncUnitDataSource::class
    single { SyncUnitRepository(get()) }

    single { FileProviderDataSourceImpl(androidContext()) } bind FileProviderDatasource::class
    single { FileProviderRepository(get()) }
}

val interactorModule = module {
    single { CreateSyncUnit(get()) }
    single { UpdateSyncUnit(get()) }
    single { DeleteSyncUnit(get()) }
    single { ReadSyncUnits(get()) }

    single { SyncUnitInteractor(get(), get(), get(), get()) }
}

val vmModule = module {

    single { PermissionChecker() }
    single { NetworkScanner() }
    single { SmbProcessor() }
    single { ContentProcessor() }
    single { SyncManager(androidApplication(), get(), get()) }
    single { DownloadManager(androidApplication(), get()) }

    viewModel { ScanNetworkViewModel(get(), get()) }
    viewModel { SetupSyncViewModel(get()) }
    viewModel { HomeViewModel(get()) }
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
