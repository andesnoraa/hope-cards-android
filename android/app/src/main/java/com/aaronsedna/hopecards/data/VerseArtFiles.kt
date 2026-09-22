package com.aaronsedna.hopecards.data

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.aaronsedna.hopecards.model.VerseArtwork
import java.io.File

object VerseArtFiles {
    const val MIME_TYPE = "image/webp"

    /** Call file operations on Dispatchers.IO; no broad storage permission is needed. */
    fun write(context: Context, artwork: VerseArtwork, uri: Uri) {
        val output = context.contentResolver.openOutputStream(uri) ?: error("Cannot open image destination")
        output.use { target -> context.assets.open(artwork.assetPath).use { it.copyTo(target) } }
    }

    @RequiresApi(29)
    fun saveToPhotos(context: Context, artwork: VerseArtwork): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Hope-Cards-${artwork.id}-${System.currentTimeMillis()}.webp")
            put(MediaStore.Images.Media.MIME_TYPE, MIME_TYPE)
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Hope Cards")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Cannot create image")
        try {
            write(context, artwork, uri)
            check(resolver.update(uri, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null) > 0)
            return uri
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    fun shareIntent(context: Context, artwork: VerseArtwork): Intent {
        val directory = File(context.cacheDir, "shared/verse-art").apply { mkdirs() }
        val file = File(directory, "hope-cards-${artwork.id}.webp")
        // Refresh the cached export after artwork is replaced by an app update.
        val temporary = File.createTempFile("art-", ".webp", directory)
        try {
            context.assets.open(artwork.assetPath).use { source ->
                temporary.outputStream().use { source.copyTo(it) }
            }
            check(temporary.renameTo(file)) { "Cannot prepare image" }
        } finally {
            temporary.delete()
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "${artwork.text}\n${artwork.reference} · WEB\nShared from Hope Cards")
            clipData = ClipData.newUri(context.contentResolver, artwork.reference, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
