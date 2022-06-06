package ru.teplicate.datasyncersmb.util

import android.content.Context
import android.content.pm.PackageManager
import java.security.Permission

class PermissionChecker {

    fun checkIfPermissionGranted(context: Context, permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}