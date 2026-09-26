package com.aaronsedna.hopecards.data

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.aaronsedna.hopecards.model.VerseArtOrder
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.Verse
import com.aaronsedna.hopecards.model.VerseArtEligibility
import com.aaronsedna.hopecards.model.VerseArtwork
import com.aaronsedna.hopecards.model.VerseArtCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/** One cancellable render queue; no jobs, timers or activity references are retained. */
object VerseArtImages {
    private const val VERSION = "artistic-v15-selected-background-rotation"
    private const val MEMORY_BYTES = 8 * 1024 * 1024
    private const val DISK_BYTES = 32L * 1024 * 1024
    private val mutex = Mutex()
    private val memory = object : LruCache<String, Bitmap>(MEMORY_BYTES) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount
        // Compose can still be drawing an evicted image; never recycle a published bitmap.
    }
    private var renderer: VerseArtRenderer? = null
    private var verses: VerseRepository? = null

    /** Once per new gallery visit, on IO. No clock, timer, service, or background refresh. */
    suspend fun nextRotation(context: Context): Long = mutex.withLock {
        withContext(Dispatchers.IO) {
            val preferences = context.applicationContext.getSharedPreferences("verse-art-rotation", Context.MODE_PRIVATE)
            val previous = preferences.getLong("visit", kotlin.random.Random.nextLong(1_000_000))
            val next = if (previous == Long.MAX_VALUE) 0 else previous + 1
            preferences.edit().putLong("visit", next).apply()
            next
        }
    }

    private fun initialize(context: Context) {
        if (renderer != null) return
        val app = context.applicationContext
        renderer = VerseArtRenderer(app)
        verses = VerseRepository(app)
        app.registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit
            override fun onLowMemory() { memory.evictAll() }
            override fun onTrimMemory(level: Int) { if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) memory.evictAll() }
        })
    }

    suspend fun verse(context: Context, art: VerseArtwork, edition: Translation): Verse = mutex.withLock {
        withContext(Dispatchers.IO) { initialize(context); checkNotNull(verses).byId(art.verseId, edition)
            ?: error("Verse not available: ${art.verseId}/${edition.id}") }
    }

    data class Gallery(val references: Map<String, String>, val scenes: Map<String, VerseArtOrder.Scene>) {
        fun ordered(artworks: List<VerseArtwork>) = VerseArtOrder.arrange(artworks.filter { it.id in references }, scenes)
    }

    suspend fun references(context: Context, edition: Translation): Map<String, String> = gallery(context, edition).references

    suspend fun gallery(context: Context, edition: Translation, rotation: Long? = null): Gallery = mutex.withLock {
        withContext(Dispatchers.IO) {
            initialize(context)
            val references = linkedMapOf<String, String>()
            val scenes = linkedMapOf<String, VerseArtOrder.Scene>()
            for (art in VerseArtCatalog.artworks) {
                val verse = checkNotNull(checkNotNull(verses).byId(art.verseId, edition))
                if (VerseArtEligibility.includes(verse)) {
                    val background = checkNotNull(renderer).design(verse, rotation).background
                    references[art.id] = verse.displayReference
                    scenes[art.id] = VerseArtOrder.Scene(background, checkNotNull(renderer).family(background))
                }
            }
            Gallery(references, scenes)
        }
    }

    private fun key(verse: Verse, size: Int, background: String): String {
        val input = "$VERSION|${verse.id}|${verse.edition.id}|${verse.text}|${verse.displayReference}|$background|$size"
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    @Suppress("DEPRECATION")
    suspend fun image(context: Context, art: VerseArtwork, edition: Translation, size: Int, rotation: Long? = null): Bitmap = mutex.withLock {
        require(size == 360 || size == 1080)
        withContext(Dispatchers.IO) {
            initialize(context)
            val verse = checkNotNull(checkNotNull(verses).byId(art.verseId, edition))
            val design = checkNotNull(renderer).design(verse, rotation)
            val key = key(verse, size, design.background)
            memory.get(key)?.let { return@withContext it }
            currentCoroutineContext().ensureActive()
            val directory = File(context.cacheDir, "verse-art-rendered").apply { mkdirs() }
            val cached = File(directory, "$key.webp")
            if (cached.exists()) {
                val decoded = BitmapFactory.decodeFile(cached.path)
                if (decoded != null && decoded.width == size && decoded.height == size) {
                    cached.setLastModified(System.currentTimeMillis()); memory.put(key, decoded)
                    return@withContext decoded
                }
                decoded?.recycle(); cached.delete()
            }
            val bitmap = withContext(Dispatchers.Default) { checkNotNull(renderer).render(verse, size, design) }
            try {
                currentCoroutineContext().ensureActive()
                atomicWrite(cached) { file -> file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.WEBP, 88, it)) } }
                trim(directory, cached)
                memory.put(key, bitmap)
                bitmap
            } catch (error: Throwable) { bitmap.recycle(); throw error }
        }
    }

    suspend fun jpeg(context: Context, art: VerseArtwork, edition: Translation, rotation: Long? = null): File {
        val verse = verse(context, art, edition)
        val id = key(verse, 1080, checkNotNull(renderer).design(verse, rotation).background)
        val directory = File(context.cacheDir, "shared/verse-art").apply { mkdirs() }
        val file = File(directory, "hope-cards-${art.verseId}-${edition.id}-$id.jpg")
        val bitmap = image(context, art, edition, 1080, rotation)
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                currentCoroutineContext().ensureActive()
                if (!file.exists()) atomicWrite(file) { target -> target.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.JPEG, 93, it)) } }
                file.setLastModified(System.currentTimeMillis())
                trim(directory, file)
                file
            }
        }
    }

    private fun atomicWrite(file: File, write: (File) -> Unit) {
        val temporary = File.createTempFile("art-", ".tmp", file.parentFile)
        try { write(temporary); check(temporary.renameTo(file)) { "Cannot cache artwork" } }
        finally { temporary.delete() }
    }

    private fun trim(directory: File, keep: File) {
        val files = directory.listFiles()?.filter { it.isFile }?.sortedBy { it.lastModified() }.orEmpty()
        var bytes = files.sumOf { it.length() }
        for (file in files) {
            if (bytes <= DISK_BYTES) break
            if (file != keep) { val size = file.length(); if (file.delete()) bytes -= size }
        }
    }

    internal fun memoryBytes() = memory.size()
    internal fun clearMemory() = memory.evictAll()
}
