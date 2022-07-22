package ru.teplicate.datasyncersmb.presentation.fragment.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.*
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ru.teplicate.datasyncersmb.R
import ru.teplicate.datasyncersmb.databinding.DialogSyncProgressBinding
import ru.teplicate.datasyncersmb.enums.SyncState

class SyncDialog(
    private val syncDialogListener: SyncDialogListener
) : DialogFragment() {

    companion object {
        const val TAG: String = "SYNC_PROGRESS_DIALOG"
    }

    private lateinit var binding: DialogSyncProgressBinding
    private var syncHandler: SyncHandler? = null
    private lateinit var d: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        runHandlerThread()
        val builder = AlertDialog.Builder(requireContext())
        binding = DialogSyncProgressBinding.inflate(layoutInflater, null, false)
        d = builder
            .setView(binding.root)
            .setMessage("Sync Progress")
            .setNegativeButton("Hide", onNegativeButtonClickListener)
            .create()

        syncDialogListener.onDialogInitialized()

        return d
    }

    private val onNegativeButtonClickListener = DialogInterface.OnClickListener { _, _ ->
        syncHandler?.removeCallbacksAndMessages(null)
        syncDialogListener.onHideDialog()
        dismiss()
    }

    fun createSyncHandlerMessenger() = Messenger(syncHandler!!)

    inner class SyncHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val total = msg.arg1
            val current = msg.arg2
            val what = msg.what


            if (what == SyncState.STARTING.ordinal) {
                setupProgBar(total)
            } else {
                updateProgress(current)
            }

            changeState(SyncState.values()[what])
        }
    }

    private fun runHandlerThread() {
        HandlerThread("SyncDialogThread")
            .apply {
                start()
                syncHandler = SyncHandler(Looper.getMainLooper())
            }
    }

    fun setupProgBar(totalElements: Int) {
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
    fun updateProgress(current: Int) {
        binding.dialogSyncProgressBar.setProgressCompat(
            current,
            true
        )

        binding.dialogProgressCurrent.text =
            resources.getString(R.string.dialog_progress_current_txt, current)
    }

    fun changeState(state: SyncState) {
        when (state) {
            SyncState.STARTING -> binding.txtSyncState.text =
                resources.getString(R.string.state_copying)
            SyncState.READING_FILES -> binding.txtSyncState.text =
                resources.getString(R.string.state_reading_files)
            SyncState.REMOVING -> binding.txtSyncState.text =
                resources.getString(R.string.state_removing)
            SyncState.COMPLETE -> {
                d.setButton(AlertDialog.BUTTON_NEGATIVE, resources.getString(R.string.dialog_close), onNegativeButtonClickListener)
                binding.txtSyncState.text = resources.getString(R.string.state_complete)
                binding.txtSyncState.text = resources.getString(R.string.state_complete)
            }
        }
    }
}

interface SyncDialogListener {

    fun onDialogInitialized()

    fun onHideDialog()

    fun onFinish()
}