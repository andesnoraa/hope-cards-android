package com.aaronsedna.hopecards.ui.screens

import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.data.VerseArtFiles
import com.aaronsedna.hopecards.data.VerseArtImages
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.Verse
import com.aaronsedna.hopecards.ui.LocalAppTranslation
import com.aaronsedna.hopecards.model.VerseArtCatalog
import com.aaronsedna.hopecards.model.VerseArtwork
import com.aaronsedna.hopecards.ui.appString
import com.aaronsedna.hopecards.ui.appQuantityString
import com.aaronsedna.hopecards.ui.components.AppIcon
import com.aaronsedna.hopecards.ui.components.AppIconGlyph
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.Poppins
import com.aaronsedna.hopecards.ui.theme.SourceSerif
import com.aaronsedna.hopecards.ui.theme.NotoSansMalayalam
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VerseArtScreen(
    categoryId: String?,
    artworkId: String?,
    favorites: Set<String>,
    onCategory: (String) -> Unit,
    onArtwork: (String) -> Unit,
    onFavorite: (VerseArtwork) -> Unit,
    onNotice: (String) -> Unit,
) {
    val colors = LocalHopeColors.current
    val edition = LocalAppTranslation.current
    val context = LocalContext.current.applicationContext
    val gallery by produceState<VerseArtImages.Gallery?>(null, edition) {
        value = null
        value = VerseArtImages.gallery(context, edition)
    }
    val available = gallery?.references.orEmpty()
    val artwork = VerseArtCatalog.artwork(artworkId)?.takeIf { it.id in available }
    val categoryScroll = rememberLazyListState()
    val galleryScroll = rememberSaveable(categoryId, saver = LazyGridState.Saver) { LazyGridState() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when {
            gallery == null -> CircularProgressIndicator(Modifier.padding(32.dp))
            artwork != null -> VerseArtDetail(artwork, artwork.verseId in favorites, { onFavorite(artwork) }, onNotice)
            categoryId == null -> Column(Modifier.widthIn(max = 760.dp).fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { onCategory(VerseArtCatalog.ALL) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("art-browse-all"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.buttonBackground, contentColor = colors.buttonText)) {
                        Text(appString(R.string.art_browse_all), fontFamily = Poppins, fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                    OutlinedButton(onClick = { onCategory(VerseArtCatalog.SAVED) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("art-browse-saved"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.textSecondary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.text)) {
                        Text(appString(R.string.art_saved), fontFamily = Poppins, fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
                LazyColumn(
                    Modifier.fillMaxWidth().weight(1f),
                    state = categoryScroll,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    item {
                        Text(appString(R.string.art_choose_category), fontFamily = Poppins, fontSize = 13.sp,
                            color = colors.textSecondary, modifier = Modifier.padding(bottom = 10.dp))
                    }
                    items(VerseArtCatalog.categories, key = { it.id }) { category ->
                        Column {
                            Row(
                                Modifier.fillMaxWidth().testTag("art-category-${category.id}")
                                    .clickable(role = Role.Button) { onCategory(category.id) }.padding(vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                VerseArtImage(VerseArtCatalog.artwork(category.coverArtworkId.takeIf { it in available }
                                    ?: category.artworkIds.first { it in available })!!,
                                    thumbnail = true, modifier = Modifier.size(60.dp), description = null)
                                Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                                    Text(category.title, color = colors.text, fontFamily = Poppins,
                                        fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    Text(category.description, color = colors.textSecondary, fontFamily = Poppins,
                                        fontSize = 12.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 4.dp))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(category.artworkIds.count { it in available }.toString(), color = colors.textSecondary,
                                        fontFamily = Poppins, fontSize = 12.sp)
                                    AppIcon(AppIconGlyph.ChevronForward, null, colors.textTertiary, size = 18.dp)
                                }
                            }
                            HorizontalDivider(color = colors.divider)
                        }
                    }
                }
            }
            else -> {
                val artworks = remember(gallery, categoryId, favorites) {
                    gallery?.ordered(VerseArtCatalog.inCategory(categoryId, favorites)).orEmpty()
                }
                Column(Modifier.widthIn(max = 760.dp).fillMaxSize()) {
                    Text(appQuantityString(R.plurals.art_count, artworks.size, artworks.size), fontFamily = Poppins,
                        color = colors.textTertiary, fontSize = 13.sp, modifier = Modifier.padding(20.dp))
                    if (artworks.isEmpty()) {
                        Text(appString(R.string.art_saved_empty), fontFamily = Poppins,
                            color = colors.textSecondary, modifier = Modifier.padding(horizontal = 20.dp))
                    }
                    LazyVerticalGrid(GridCells.Adaptive(150.dp), state = galleryScroll, modifier = Modifier.testTag("art-gallery"), contentPadding = PaddingValues(20.dp, 0.dp, 20.dp, 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(artworks, key = { it.id }) { art ->
                            val reference = available[art.id].orEmpty()
                            Column {
                                VerseArtImage(art, thumbnail = true,
                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).testTag("art-open-${art.id}")
                                        .clickable(role = Role.Button) { onArtwork(art.id) }, description = reference)
                                Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(reference, color = colors.textSecondary,
                                        fontFamily = if (edition == Translation.MAL1910) NotoSansMalayalam else Poppins,
                                        fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    ArtFavoriteButton(art.verseId in favorites, { onFavorite(art) },
                                        Modifier.testTag("art-favorite-${art.id}"))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerseArtDetail(artwork: VerseArtwork, favorite: Boolean, onFavorite: () -> Unit, onNotice: (String) -> Unit) {
    val context = LocalContext.current
    val edition = LocalAppTranslation.current
    val colors = LocalHopeColors.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var showText by rememberSaveable(artwork.id) { mutableStateOf(false) }
    var pendingSaveId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingSaveEdition by rememberSaveable { mutableStateOf<String?>(null) }
    val verse by produceState<Verse?>(null, artwork.id, edition) {
        value = null
        value = VerseArtImages.verse(context.applicationContext, artwork, edition)
    }
    val reference = verse?.displayReference.orEmpty()
    val savedMessage = appString(R.string.art_image_saved)
    val errorMessage = appString(R.string.art_image_error)
    val shareTitle = appString(R.string.share_card_title)

    fun performFileAction(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        scope.launch {
            try { action() }
            catch (error: CancellationException) { throw error }
            catch (_: Exception) { onNotice(errorMessage) }
            finally { busy = false }
        }
    }

    val saveDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(VerseArtFiles.MIME_TYPE)) { uri ->
        val pending = VerseArtCatalog.artwork(pendingSaveId)
        val savedEdition = Translation.fromId(pendingSaveEdition)
        pendingSaveId = null
        pendingSaveEdition = null
        if (uri != null && pending != null) performFileAction {
            withContext(Dispatchers.IO) { VerseArtFiles.write(context, pending, uri, savedEdition) }
            onNotice(savedMessage)
        }
    }

    Column(Modifier.widthIn(max = 720.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("${edition.language} · ${edition.label}", color = colors.textSecondary, fontFamily = Poppins, fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp))
        Box {
            VerseArtImage(artwork, false, Modifier.fillMaxWidth().aspectRatio(1f).testTag("art-detail-image"), reference)
            ArtFavoriteButton(favorite, onFavorite, Modifier.align(Alignment.TopEnd).padding(8.dp))
        }
        Text(reference, fontFamily = if (edition == Translation.MAL1910) NotoSansMalayalam else Poppins, fontWeight = FontWeight.Bold, fontSize = 26.sp,
            color = colors.text, modifier = Modifier.padding(top = 24.dp))
        Text(edition.displayName, fontFamily = Poppins, fontSize = 13.sp, color = colors.textSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(enabled = !busy, onClick = {
                if (Build.VERSION.SDK_INT >= 29) performFileAction {
                    withContext(Dispatchers.IO) { VerseArtFiles.saveToPhotos(context, artwork, edition) }
                    onNotice(savedMessage)
                } else {
                    pendingSaveId = artwork.id
                    pendingSaveEdition = edition.id
                    saveDocument.launch("Hope-Cards-${artwork.id}-${edition.id}.jpg")
                }
            }, modifier = Modifier.heightIn(min = 48.dp).testTag("art-save-image"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.text)) {
                Text(appString(R.string.art_save_image), fontFamily = Poppins)
            }
            Button(enabled = !busy, onClick = {
                performFileAction {
                    val intent = withContext(Dispatchers.IO) { VerseArtFiles.shareIntent(context, artwork, edition) }
                    context.startActivity(Intent.createChooser(intent, shareTitle))
                }
            }, modifier = Modifier.heightIn(min = 48.dp).testTag("art-share"),
                colors = ButtonDefaults.buttonColors(containerColor = colors.buttonBackground, contentColor = colors.buttonText)) {
                AppIcon(AppIconGlyph.ShareOutline, null, colors.buttonText, size = 18.dp)
                Spacer(Modifier.width(8.dp))
                Text(appString(R.string.share), fontFamily = Poppins)
            }
        }
        HorizontalDivider(color = colors.divider, modifier = Modifier.padding(top = 24.dp))
        TextButton(onClick = { showText = !showText }, modifier = Modifier.padding(top = 8.dp)) {
            Text(appString(if (showText) R.string.art_hide_verse else R.string.art_read_verse), fontFamily = Poppins, color = colors.text)
        }
        if (showText) verse?.let { Text(it.text, color = colors.cardText,
            fontFamily = if (edition == Translation.MAL1910) NotoSansMalayalam else SourceSerif, fontSize = 20.sp, lineHeight = 32.sp) }
    }
}

@Composable
private fun ArtFavoriteButton(saved: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalHopeColors.current
    IconButton(onClick = onClick, modifier = modifier.background(colors.background.copy(alpha = .94f), CircleShape)) {
        AppIcon(if (saved) AppIconGlyph.Heart else AppIconGlyph.HeartOutline,
            appString(if (saved) R.string.art_unfavorite else R.string.art_favorite),
            if (saved) colors.danger else colors.text, size = 21.dp)
    }
}

private data class ArtImageState(val bitmap: ImageBitmap? = null, val failed: Boolean = false)

@Composable
private fun VerseArtImage(art: VerseArtwork, thumbnail: Boolean, modifier: Modifier, description: String?) {
    val context = LocalContext.current.applicationContext
    val colors = LocalHopeColors.current
    val edition = LocalAppTranslation.current
    val state by produceState(ArtImageState(), art.id, edition, thumbnail) {
        value = ArtImageState()
        value = try {
            ArtImageState(VerseArtImages.image(context, art, edition, if (thumbnail) 360 else 1080).asImageBitmap())
        } catch (error: CancellationException) { throw error }
          catch (_: Exception) { ArtImageState(failed = true) }
    }
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(colors.accentSoft), contentAlignment = Alignment.Center) {
        state.bitmap?.let { Image(it, description, Modifier.matchParentSize(), contentScale = ContentScale.Fit) }
            ?: if (state.failed) {
                Text(appString(R.string.art_image_unavailable), fontSize = 12.sp, color = colors.text)
            } else {
                CircularProgressIndicator(Modifier.size(20.dp), color = colors.accent, strokeWidth = 2.dp)
            }
    }
}
