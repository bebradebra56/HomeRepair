package com.homerapa.repagom.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Copies an image from a content URI (e.g. from gallery picker) into app storage
 * and returns a persistent content URI. Use this so gallery picks survive app restart.
 */
fun Context.copyImageUriToAppStorage(contentUri: Uri, filePrefix: String = "img"): String? {
    val dir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES) ?: return null
    val extension = contentResolver.getType(contentUri)?.let { type ->
        MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
    } ?: "jpg"
    val file = File(dir, "${filePrefix}_${System.currentTimeMillis()}.$extension")
    return try {
        contentResolver.openInputStream(contentUri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        FileProvider.getUriForFile(this, "$packageName.fileprovider", file).toString()
    } catch (e: Exception) {
        null
    }
}
