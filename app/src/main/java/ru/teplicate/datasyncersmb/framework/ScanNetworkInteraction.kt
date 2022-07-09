package ru.teplicate.datasyncersmb.framework

import ru.teplicate.core.interactors.CheckConnectionUseCase
import ru.teplicate.core.interactors.ScanNetworkUseCase

data class ScanNetworkInteraction(
    val scanNetworkUseCase: ScanNetworkUseCase,
    val testConnectionUseCase: CheckConnectionUseCase
)