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
import com.aaronsedna.hopecards.model.Translation

object VerseArtFiles {
    const val MIME_TYPE = "image/jpeg"

    /** Call file operations on Dispatchers.IO; no broad storage permission is needed. */
    suspend fun write(context: Context, artwork: VerseArtwork, uri: Uri, edition: Translation = Translation.WEB, rotation: Long? = null) {
        val file = VerseArtImages.jpeg(context, artwork, edition, rotation)
        val output = context.contentResolver.openOutputStream(uri) ?: error("Cannot open image destination")
        output.use { target -> file.inputStream().use { it.copyTo(target) } }
    }

    @RequiresApi(29)
    suspend fun saveToPhotos(context: Context, artwork: VerseArtwork, edition: Translation = Translation.WEB, rotation: Long? = null): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Hope-Cards-${artwork.id}-${edition.id}-${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, MIME_TYPE)
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Hope Cards")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Cannot create image")
        try {
            write(context, artwork, uri, edition, rotation)
            check(resolver.update(uri, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null) > 0)
            return uri
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    suspend fun shareIntent(context: Context, artwork: VerseArtwork, edition: Translation = Translation.WEB, rotation: Long? = null): Intent {
        val file = VerseArtImages.jpeg(context, artwork, edition, rotation)
        val reference = VerseArtImages.verse(context, artwork, edition).displayReference
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Shared from Hope Cards ❤️\nhttps://play.google.com/store/apps/details?id=com.aaronsedna.hopecards")
            clipData = ClipData.newUri(context.contentResolver, reference, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
