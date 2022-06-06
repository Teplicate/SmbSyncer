package ru.teplicate.datasyncersmb.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class SmbInfo(
    val login: String,
    val password: String,
    val directory: String,
    val address: String
) : Parcelable