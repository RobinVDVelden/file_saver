package com.incrediblezayed.file_saver

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream
import java.io.FileInputStream
import java.io.FileOutputStream


private const val SAVE_FILE = 886325063

class Dialog(private val activity: Activity) : PluginRegistry.ActivityResultListener {
    private var result: MethodChannel.Result? = null
    private var bytes: ByteArray? = null
    private var fileName: String? = null
    private var cachePath: String? = null
    private val TAG = "Dialog Activity"

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != SAVE_FILE) {
            return false
        }

        if (resultCode == Activity.RESULT_OK && data?.data != null) {
            Log.d(TAG, "Starting file operation")
            completeFileOperation(data.data!!)
        } else {
            Log.d(TAG, "Activity result was null")
            result?.success(null)
            result = null
        }

        return true
    }

    fun openFileManager(
            fileName: String?,
            ext: String?,
            bytes: ByteArray?,
            type: String?,
            result: MethodChannel.Result
    ) {
        Log.d(TAG, "Opening File Manager")
        this.result = result
        this.bytes = bytes
        this.cachePath = fileName
        this.fileName = "easyperformer_export"

        val intent =
                Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_TITLE, "easyperformer_export.$ext")
        intent.putExtra(
                DocumentsContract.EXTRA_INITIAL_URI,
                Environment.getExternalStorageDirectory().path
        )
        intent.type = type
        activity.startActivityForResult(intent, SAVE_FILE)
    }

    private fun completeFileOperation(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                saveFile(uri)
                val fileUtils = FileUtils(activity)
                result?.success(fileUtils.getPath(uri));
                result = null
                //result?.success(getRealPathFromUri(activity, uri))
            } catch (e: SecurityException) {
                Log.d(TAG, "Security Exception while saving file" + e.message)

                result?.error("Security Exception", e.localizedMessage, e)
                result = null
            } catch (e: Exception) {
                Log.d(TAG, "Exception while saving file" + e.message)
                result?.error("Error", e.localizedMessage, e)
                result = null
            }
        }
    }

    private fun saveFile(uri: Uri) {
        val sourceFile = File(this.cachePath) // Assuming this.cachePath is your cache file path
        val inputStream = FileInputStream(sourceFile)

        // Create output stream to destination file
        val opStream = activity.contentResolver.openOutputStream(uri)

        // Buffer for reading/writing data
        val buffer = ByteArray(1024)
        var length: Int

        try {
            opStream?.use { output ->
                // Read from input stream and write to output stream in chunks
                while (inputStream.read(buffer).also { length = it } > 0) {
                    output.write(buffer, 0, length)
                }
            }
        } finally {
            // Close the input stream
            inputStream.close()
        }
    }
}