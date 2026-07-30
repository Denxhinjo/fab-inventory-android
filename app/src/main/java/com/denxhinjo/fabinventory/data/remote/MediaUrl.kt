package com.denxhinjo.fabinventory.data.remote

import com.denxhinjo.fabinventory.BuildConfig

/**
 * Mirrors frontend/src/services/api.ts::mediaUrl -- an image_url from the
 * backend is either an absolute Cloudinary URL (used as-is) or a legacy
 * relative /media/... path that needs the backend's own origin prefixed.
 */
fun resolveMediaUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    return if (path.startsWith("http://") || path.startsWith("https://")) {
        path
    } else {
        BuildConfig.DEFAULT_BASE_URL.trimEnd('/') + path
    }
}
