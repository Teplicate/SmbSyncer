package ru.teplicate.datasyncersmb.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ru.teplicate.datasyncersmb.R
import ru.teplicate.datasyncersmb.databinding.DialogSyncProgressBinding
import ru.teplicate.datasyncersmb.enums.SyncState

class SyncDialog(
    private val syncDialogListener: SyncDialogListener,
    private val totalElements: Int
) : DialogFragment() {

    companion object {
        const val TAG: String = "SYNC_PROGRESS_DIALOG"
    }

    private lateinit var binding: DialogSyncProgressBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        binding = DialogSyncProgressBinding.inflate(layoutInflater, null, false)
        val alertDialog = builder
            .setView(binding.root)
            .setMessage("Sync Progress")
            .setNegativeButton("Cancel") { _, _ ->
                syncDialogListener.onCancelSync()
            }
            .create()
        setupViews()
        return alertDialog
    }

    private fun setupViews() {
        with(binding.dialogSyncProgressBar) {
            this.progress = 0
            this.max = totalElements
        }

        binding.dialogProgressTotal.text =
            resources.getString(R.string.dialog_progress_total_txt, totalElements)
        binding.dialogProgressCurrent.text =
            resources.getString(R.string.dialog_progress_current_txt, 0)
    }

    @Synchronized
    @MainThread
    fun updateProgress() {
        val current = binding.dialogSyncProgressBar.progress + 1
        binding.dialogSyncProgressBar.setProgressCompat(
            current,
            true
        )

        binding.dialogProgressCurrent.text =
            resources.getString(R.string.dialog_progress_current_txt, current)
    }

    fun changeState(state: SyncState) {
        when (state) {
            SyncState.COPYING -> binding.txtSyncState.text =
                resources.getString(R.string.state_copying)
            SyncState.READING_FILES -> binding.txtSyncState.text =
                resources.getString(R.string.state_reading_files)
            SyncState.REMOVING -> binding.txtSyncState.text =
                resources.getString(R.string.state_removing)
        }
    }
}

interface SyncDialogListener {

    fun onCancelSync()

    fun onFinish()
}