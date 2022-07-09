package ru.teplicate.datasyncersmb

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTest
import org.koin.test.inject
import ru.teplicate.datasyncersmb.framework.database.AppDatabase
import ru.teplicate.datasyncersmb.framework.database.dao.SynchronizationUnitDAO
import ru.teplicate.datasyncersmb.framework.database.entity.SmbConnection

import ru.teplicate.datasyncersmb.framework.database.entity.SynchronizationUnitEntity
import ru.teplicate.datasyncersmb.framework.koin_module.roomTestModule

@RunWith(AndroidJUnit4::class)

class RoomTest : AutoCloseKoinTest() {


    private val appDatabase: AppDatabase by inject()
    private val syncUnitDao: SynchronizationUnitDAO by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    @Before
    fun initRoomBefore() {
        loadKoinModules(roomTestModule)
    }

    @After
    fun cleanUpAfter() {
        scope.cancel()
        appDatabase.close()
        getKoin().close()
    }

    @Test
    fun saveSyncUnit() {
        val syncUnit = SynchronizationUnitEntity(
            name = "Test Unit",
            synchronizationInfo = SynchronizationInfo(
                null, null
            ),
            smbConnection = SmbConnection(
                address = "127.0.0.1",
                user = "guest",
                password = null,
                sharedDirectory = "dir"
            ),
            contentUri = "test:/content/",
            synchronizationOptions = emptyList()
        )

        syncUnitDao.saveSyncUnit(syncUnit)


        runBlocking {
            syncUnitDao.readAllSyncUnitsAsFlow().take(1)
                .collect { e ->
                    assert(e.size == 1)
                    assert(e.first().name == syncUnit.name)
                }
        }

    }
}