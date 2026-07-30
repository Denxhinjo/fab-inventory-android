package com.denxhinjo.fabinventory.ui.common

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Creates a fresh cache file (matching res/xml/file_paths.xml) for the system Camera app to write into. */
fun createImageCaptureUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val imageFile = File(imagesDir, "capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
}
