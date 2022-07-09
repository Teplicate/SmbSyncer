package ru.teplicate.core.interactors

import ru.teplicate.core.data.CheckConnectionRepository
import ru.teplicate.core.data.ScanRepository

class ScanNetworkUseCase(private val repository: ScanRepository) {
    suspend operator fun invoke() = repository.scan()
}

class CheckConnectionUseCase(private val checkConnectionRepository: CheckConnectionRepository) {
    suspend operator fun invoke() = checkConnectionRepository.checkConnection()
}