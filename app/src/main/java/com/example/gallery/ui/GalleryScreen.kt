package com.example.gallery.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.gallery.model.Picture
import com.example.gallery.model.generateSamplePictures
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val gallery = remember {
        mutableStateListOf<Picture>().apply { addAll(generateSamplePictures()) }
    }
    var searchText by rememberSaveable { mutableStateOf("") }
    var isGridMode by rememberSaveable { mutableStateOf(true) }

    val filtered by remember(searchText) {
        derivedStateOf {
            val base: List<Picture> = gallery
            if (searchText.isBlank()) base
            else base.filter { it.author.contains(other = searchText, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = searchText,
                        onValueChange = { value: String -> searchText = value },
                        singleLine = true,
                        label = { Text(text = "Поиск по автору") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { searchText = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Сбросить поиск")
                                }
                            }
                        }
                    )

                    Spacer(Modifier.width(8.dp))

                    IconButton(onClick = { isGridMode = !isGridMode }) {
                        if (isGridMode) {
                            Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = "Режим списка")
                        } else {
                            Icon(Icons.Filled.GridOn, contentDescription = "Режим сетки")
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    IconButton(onClick = { gallery.clear() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Очистить всё")
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val newPicture = Picture(
                        id = (gallery.maxOfOrNull { it.id } ?: 0) + 1,
                        author = "New Author",
                        url = "https://picsum.photos/seed/${System.currentTimeMillis()}/600/400"
                    )
                    val exists = gallery.any { it.id == newPicture.id || it.url == newPicture.url }
                    if (exists) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Такое изображение уже существует (id или url).")
                        }
                    } else {
                        gallery.add(0, newPicture)
                    }
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        if (filtered.isEmpty()) {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text(text = "Пусто")
            }
        } else {
            if (isGridMode) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    modifier = modifier,
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { picture ->
                        PictureCard(
                            picture = picture,
                            onClick = { gallery.remove(picture) },
                            isGrid = true,
                            onImageError = { msg ->
                                scope.launch { snackbarHostState.showSnackbar("Ошибка загрузки: ${msg}") }
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = modifier,
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { picture ->
                        PictureCard(
                            picture = picture,
                            onClick = { gallery.remove(picture) },
                            isGrid = false,
                            onImageError = { msg ->
                                scope.launch { snackbarHostState.showSnackbar("Ошибка загрузки: ${msg}") }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PictureCard(
    picture: Picture,
    onClick: () -> Unit,
    isGrid: Boolean,
    onImageError: (String) -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val cardModifier = if (isGrid) {
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    } else {
        Modifier
            .fillMaxWidth()
            .height(200.dp)
    }

    Card(
        shape = shape,
        modifier = cardModifier.clickable { onClick() }
    ) {
        Box {
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(picture.url)
                    .addHeader("User-Agent", "GalleryCompose/1.0 (Android; Coil)")
                    .addHeader("Referer", "https://commons.wikimedia.org/")
                    .crossfade(true)
                    .build(),
                contentDescription = picture.author,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = android.R.drawable.ic_menu_report_image),
                error = painterResource(id = android.R.drawable.stat_notify_error),
                modifier = Modifier.fillMaxSize(),
                onError = { result ->
                    val message = result.result.throwable.message ?: "unknown"
                    onImageError(message)
                }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(8.dp)
            ) {
                Text(
                    text = picture.author,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

