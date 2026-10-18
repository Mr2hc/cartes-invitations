package com.invitecard.ui.editor

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.invitecard.util.CardExporter
import com.invitecard.util.DraftManager
import com.invitecard.util.QRCodeGenerator
import com.invitecard.viewmodel.ElementType
import com.invitecard.viewmodel.InvitationEditorViewModel
import com.invitecard.viewmodel.PageFormat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationEditorScreen(viewModel: InvitationEditorViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textMeasurer = rememberTextMeasurer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    
    var currentStep by remember { mutableIntStateOf(1) } 
    var showTextInputDialog by remember { mutableStateOf(false) }
    var showGeminiDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showFormatMenu by remember { mutableStateOf(false) }
    var showStickerMenu by remember { mutableStateOf(false) }
    var showQRDialog by remember { mutableStateOf(false) }
    var showExportSuccess by remember { mutableStateOf(false) }
    var showEffectsDialog by remember { mutableStateOf(false) }
    
    var pendingText by remember { mutableStateOf("") }
    var qrContent by remember { mutableStateOf("https://") }
    var isEditingExisting by remember { mutableStateOf(false) }
    var geminiPrompt by remember { mutableStateOf("") }
    var lastExportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // Load Draft on launch
    LaunchedEffect(Unit) {
        val draft = DraftManager.loadDraft(context)
        if (draft != null) {
            viewModel.loadDraft(draft)
            Toast.makeText(context, "Brouillon chargé !", Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.setPersonalImage(it.toString()) } }

    val stickers = listOf(
        Icons.Default.Favorite, Icons.Default.Star, Icons.Default.Celebration, 
        Icons.Default.Cake, Icons.Default.ChildCare, Icons.Default.ChildFriendly,
        Icons.Default.MusicNote, Icons.Default.WineBar, Icons.Default.Pets
    )
    val stickerPainters = stickers.map { rememberVectorPainter(it) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(if (currentStep == 1) "Étape 2 : Personnaliser" else "Étape 3 : Prévisualiser", 
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Votre Invitation", style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { if (currentStep > 1) currentStep-- }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (currentStep == 1) {
                        IconButton(onClick = { 
                            DraftManager.saveDraft(context, uiState.elements)
                            Toast.makeText(context, "Brouillon enregistré", Toast.LENGTH_SHORT).show()
                        }) { Icon(Icons.Default.Save, contentDescription = "Sauvegarder") }
                        
                        Button(onClick = { currentStep = 2 }, shape = MaterialTheme.shapes.medium) {
                            Text("Suivant")
                        }
                    } else {
                        IconButton(onClick = {
                            scope.launch {
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                val uri = CardExporter.saveToGallery(context, bitmap)
                                lastExportUri = uri
                                showExportSuccess = true
                                DraftManager.clearDraft(context)
                            }
                        }) { Icon(Icons.Default.Download, contentDescription = "Terminer") }
                    }
                }
            )
        },
        bottomBar = {
            if (currentStep == 1) {
                EditorBottomBar(
                    viewModel = viewModel, 
                    isElementSelected = uiState.selectedElementId != null,
                    onAddPhoto = { photoPickerLauncher.launch("image/*") },
                    onAddText = { pendingText = ""; isEditingExisting = false; showTextInputDialog = true },
                    onAddSticker = { showStickerMenu = true },
                    onAddQR = { showQRDialog = true },
                    onEditText = {
                        val selected = uiState.elements.find { it.id == uiState.selectedElementId }
                        pendingText = selected?.text ?: ""; isEditingExisting = true; showTextInputDialog = true
                    },
                    onShowGemini = { showGeminiDialog = true },
                    onShowColors = { showColorPicker = true },
                    onShowFormat = { showFormatMenu = true },
                    onShowEffects = { showEffectsDialog = true }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues).background(if (isSystemInDarkTheme()) Color(0xFF0F172A) else Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.95f)
                        .aspectRatio(uiState.format.widthRatio / uiState.format.heightRatio),
                    shape = MaterialTheme.shapes.extraSmall,
                    elevation = CardDefaults.cardElevation(if (currentStep == 2) 12.dp else 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                graphicsLayer.record { this@drawWithContent.drawContent() }
                                drawLayer(graphicsLayer)
                            }
                            .background(Color.White)
                    ) {
                        // 1. Background
                        Box(modifier = Modifier.fillMaxSize().background(uiState.backgroundColor)) {
                            if (uiState.backgroundImage != null) {
                                AsyncImage(
                                    model = uiState.backgroundImage,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().rotate(uiState.rotation),
                                    contentScale = ContentScale.Crop,
                                    colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(uiState.saturation) })
                                )
                            }
                        }

                        // 2. Elements Layer
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(uiState.elements) {
                                    detectTapGestures { offset ->
                                        if (currentStep == 1) {
                                            val clickedElement = uiState.elements.findLast { element ->
                                                val dx = offset.x - element.position.x
                                                val dy = offset.y - element.position.y
                                                dx in -50f..300f && dy in -50f..150f 
                                            }
                                            viewModel.selectElement(clickedElement?.id)
                                        }
                                    }
                                }
                                .pointerInput(uiState.selectedElementId) {
                                    detectDragGestures { change, dragAmount ->
                                        if (currentStep == 1) {
                                            change.consume()
                                            uiState.selectedElementId?.let { id ->
                                                viewModel.updateElementPosition(id, dragAmount)
                                            }
                                        }
                                    }
                                }
                        ) {
                            uiState.elements.forEach { element ->
                                val isSelected = uiState.selectedElementId == element.id && currentStep == 1
                                
                                when (element.type) {
                                    ElementType.TEXT -> {
                                        val fontFamily = when(element.fontFamilyName) {
                                            "Serif" -> FontFamily.Serif
                                            "Cursive" -> FontFamily.Cursive
                                            "Monospace" -> FontFamily.Monospace
                                            else -> FontFamily.SansSerif
                                        }
                                        val textShadow = if (element.hasShadow) {
                                            Shadow(
                                                color = Color(element.shadowColorArgb),
                                                offset = Offset(element.shadowOffsetX, element.shadowOffsetY),
                                                blurRadius = element.shadowRadius
                                            )
                                        } else null

                                        withTransform({
                                            translate(element.position.x, element.position.y)
                                            scale(element.scale, element.scale)
                                        }) {
                                            val baseStyle = if (element.hasGradient) {
                                                TextStyle(
                                                    fontSize = element.fontSize.sp,
                                                    fontFamily = fontFamily,
                                                    fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                                                    fontStyle = if (element.isItalic) FontStyle.Italic else FontStyle.Normal,
                                                    textAlign = element.textAlign
                                                ).copy(
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(Color(element.gradientStartColorArgb), Color(element.gradientEndColorArgb))
                                                    )
                                                )
                                            } else {
                                                TextStyle(
                                                    fontSize = element.fontSize.sp,
                                                    color = element.color,
                                                    fontFamily = fontFamily,
                                                    fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                                                    fontStyle = if (element.isItalic) FontStyle.Italic else FontStyle.Normal,
                                                    textAlign = element.textAlign
                                                )
                                            }
                                            val textStyle = if (element.hasShadow) {
                                                baseStyle.copy(
                                                    shadow = Shadow(
                                                        color = Color(element.shadowColorArgb),
                                                        offset = Offset(element.shadowOffsetX, element.shadowOffsetY),
                                                        blurRadius = element.shadowRadius
                                                    )
                                                )
                                            } else {
                                                baseStyle
                                            }
                                            drawText(
                                                textMeasurer = textMeasurer,
                                                text = element.text,
                                                style = textStyle
                                            )
                                        }
                                    }
                                    ElementType.STICKER -> {
                                        val painter = stickerPainters.getOrNull(element.stickerIndex ?: 0)
                                        if (painter != null) {
                                            withTransform({
                                                translate(element.position.x, element.position.y)
                                                scale(element.scale, element.scale)
                                            }) {
                                                val iconSize = 100f
                                                with(painter) {
                                                    draw(size = Size(iconSize, iconSize), colorFilter = ColorFilter.tint(element.color))
                                                }
                                            }
                                        }
                                    }
                                    ElementType.QR_CODE -> {
                                        val qrBitmap = QRCodeGenerator.generate(element.text, 200).asImageBitmap()
                                        withTransform({
                                            translate(element.position.x, element.position.y)
                                            scale(element.scale, element.scale)
                                        }) {
                                            drawImage(qrBitmap)
                                        }
                                    }
                                }
                                
                                if (isSelected) {
                                    drawRect(
                                        color = Color(0xFF2196F3).copy(alpha = 0.2f),
                                        topLeft = Offset(element.position.x - 10, element.position.y - 10),
                                        size = Size(200f * element.scale, 100f * element.scale)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (uiState.isGeneratingImage) CircularProgressIndicator()
        }
    }

    // Success & Share Dialog
    if (showExportSuccess) {
        AlertDialog(
            onDismissRequest = { showExportSuccess = false },
            title = { 
                Column {
                    Text("Étape 4 : Partager & Imprimer", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Félicitations ! 🎉") 
                }
            },
            text = { Text("Votre invitation est prête. Comment souhaitez-vous l'envoyer ?") },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { lastExportUri?.let { CardExporter.shareImage(context, it) }; showExportSuccess = false }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.Share, null); Spacer(Modifier.width(8.dp)); Text("Partager (WhatsApp, etc.)")
                    }
                    OutlinedButton(onClick = { scope.launch { val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap(); CardExporter.printBitmap(context, bitmap) }; showExportSuccess = false }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Print, null); Spacer(Modifier.width(8.dp)); Text("Imprimer le document")
                    }
                }
            }
        )
    }

    // QR Dialog
    if (showQRDialog) {
        AlertDialog(
            onDismissRequest = { showQRDialog = false },
            title = { Text("Lien RSVP (QR Code)") },
            text = {
                OutlinedTextField(value = qrContent, onValueChange = { qrContent = it }, label = { Text("Lien vers votre formulaire") }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(onClick = { viewModel.addQRCodeElement(qrContent); showQRDialog = false }) { Text("Ajouter QR Code") }
            }
        )
    }

    // Text Effects Dialog
    if (showEffectsDialog) {
        val selected = uiState.elements.find { it.id == uiState.selectedElementId }
        if (selected != null && selected.type == ElementType.TEXT) {
            var shadowEnabled by remember { mutableStateOf(selected.hasShadow) }
            var shadowColor by remember { mutableStateOf(Color(selected.shadowColorArgb)) }
            var shadowDx by remember { mutableStateOf(selected.shadowOffsetX) }
            var shadowDy by remember { mutableStateOf(selected.shadowOffsetY) }
            var shadowRad by remember { mutableStateOf(selected.shadowRadius) }

            var gradientEnabled by remember { mutableStateOf(selected.hasGradient) }
            var gradStartColor by remember { mutableStateOf(Color(selected.gradientStartColorArgb)) }
            var gradEndColor by remember { mutableStateOf(Color(selected.gradientEndColorArgb)) }

            val colorsList = listOf(
                Color.Black, Color.DarkGray, Color.Gray, Color.LightGray, Color.White,
                Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF3F51B5),
                Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4), Color(0xFF009688),
                Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39), Color(0xFFFFEB3B),
                Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722)
            )

            AlertDialog(
                onDismissRequest = { showEffectsDialog = false },
                title = { Text("Effets de Texte", style = MaterialTheme.typography.titleMedium) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --- SHADOW SECTION ---
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Ombre portée", fontWeight = FontWeight.Bold)
                                    Switch(checked = shadowEnabled, onCheckedChange = { shadowEnabled = it })
                                }
                                
                                if (shadowEnabled) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Décalage X: ${shadowDx.toInt()} px", style = MaterialTheme.typography.bodySmall)
                                    Slider(value = shadowDx, onValueChange = { shadowDx = it }, valueRange = -20f..20f)

                                    Text("Décalage Y: ${shadowDy.toInt()} px", style = MaterialTheme.typography.bodySmall)
                                    Slider(value = shadowDy, onValueChange = { shadowDy = it }, valueRange = -20f..20f)

                                    Text("Flou: ${shadowRad.toInt()}", style = MaterialTheme.typography.bodySmall)
                                    Slider(value = shadowRad, onValueChange = { shadowRad = it }, valueRange = 1f..30f)

                                    Text("Couleur de l'ombre", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(colorsList) { color ->
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(color, MaterialTheme.shapes.extraSmall)
                                                    .clickable { shadowColor = color }
                                                    .border(
                                                        width = if (shadowColor == color) 2.dp else 0.dp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = MaterialTheme.shapes.extraSmall
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // --- GRADIENT SECTION ---
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Dégradé de couleur", fontWeight = FontWeight.Bold)
                                    Switch(checked = gradientEnabled, onCheckedChange = { gradientEnabled = it })
                                }

                                if (gradientEnabled) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Couleur de départ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(colorsList) { color ->
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(color, MaterialTheme.shapes.extraSmall)
                                                    .clickable { gradStartColor = color }
                                                    .border(
                                                        width = if (gradStartColor == color) 2.dp else 0.dp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = MaterialTheme.shapes.extraSmall
                                                    )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Couleur de fin", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(colorsList) { color ->
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(color, MaterialTheme.shapes.extraSmall)
                                                    .clickable { gradEndColor = color }
                                                    .border(
                                                        width = if (gradEndColor == color) 2.dp else 0.dp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = MaterialTheme.shapes.extraSmall
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.updateSelectedElementShadow(
                            enabled = shadowEnabled,
                            color = shadowColor,
                            dx = shadowDx,
                            dy = shadowDy,
                            radius = shadowRad
                        )
                        viewModel.updateSelectedElementGradient(
                            enabled = gradientEnabled,
                            startColor = gradStartColor,
                            endColor = gradEndColor
                        )
                        showEffectsDialog = false
                    }) {
                        Text("Appliquer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEffectsDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        } else {
            Toast.makeText(context, "Veuillez sélectionner un élément texte", Toast.LENGTH_SHORT).show()
            showEffectsDialog = false
        }
    }

    // Text Input Dialog
    if (showTextInputDialog) {
        TextInputDialog(
            text = pendingText,
            onTextChange = { pendingText = it },
            isEditing = isEditingExisting,
            onConfirm = {
                if (pendingText.isNotBlank()) {
                    if (isEditingExisting) viewModel.updateSelectedElementText(pendingText)
                    else viewModel.addTextElement(pendingText)
                }
                showTextInputDialog = false
            },
            onDismiss = { showTextInputDialog = false }
        )
    }

    // Color Picker Dialog
    if (showColorPicker) {
        ColorPickerDialog(
            isElementSelected = uiState.selectedElementId != null,
            onColorSelected = { color ->
                if (uiState.selectedElementId != null) {
                    viewModel.updateSelectedElementColor(color)
                } else {
                    viewModel.setBackground(color)
                }
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    // Format Menu Dialog
    if (showFormatMenu) {
        FormatMenuDialog(
            selectedFormat = uiState.format,
            onFormatSelected = { format ->
                viewModel.setFormat(format)
                showFormatMenu = false
            },
            onDismiss = { showFormatMenu = false }
        )
    }

    // AI Generation Dialog
    if (showGeminiDialog) {
        AIGenerationDialog(
            prompt = geminiPrompt,
            onPromptChange = { geminiPrompt = it },
            onGenerate = {
                viewModel.generateWithIA(geminiPrompt)
                showGeminiDialog = false
            },
            onDismiss = { showGeminiDialog = false }
        )
    }

    // Sticker Menu Dialog
    if (showStickerMenu) {
        StickerMenuDialog(
            stickers = stickers,
            onStickerSelected = { index ->
                viewModel.addStickerElement(index)
                showStickerMenu = false
            },
            onDismiss = { showStickerMenu = false }
        )
    }
}

@Composable
fun EditorBottomBar(
    viewModel: InvitationEditorViewModel, 
    isElementSelected: Boolean,
    onAddPhoto: () -> Unit,
    onAddText: () -> Unit,
    onAddSticker: () -> Unit,
    onAddQR: () -> Unit,
    onEditText: () -> Unit,
    onShowGemini: () -> Unit,
    onShowColors: () -> Unit,
    onShowFormat: () -> Unit,
    onShowEffects: () -> Unit
) {
    var showFontMenu by remember { mutableStateOf(false) }

    Surface(tonalElevation = 3.dp) {
        Column {
            if (isElementSelected) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = onEditText) { Icon(Icons.Default.Edit, "Modifier") }
                    IconButton(onClick = { showFontMenu = true }) { Icon(Icons.Default.TextFields, "Police") }
                    IconButton(onClick = { viewModel.toggleSelectedElementBold() }) { Icon(Icons.Default.FormatBold, "Gras") }
                    IconButton(onClick = onShowEffects) { Icon(Icons.Default.Gradient, "Effets") }
                    IconButton(onClick = { viewModel.moveSelectedToFront() }) { Icon(Icons.Default.FlipToFront, "Devant") }
                    IconButton(onClick = { viewModel.moveSelectedToBack() }) { Icon(Icons.Default.FlipToBack, "Derrière") }
                    IconButton(onClick = { viewModel.deleteSelectedElement() }) { Icon(Icons.Default.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error) }
                }
            }
            BottomAppBar(
                actions = {
                    IconButton(onClick = onAddText) { Icon(Icons.Default.Add, "Texte") }
                    IconButton(onClick = onAddPhoto) { Icon(Icons.Default.AddPhotoAlternate, "Photo") }
                    IconButton(onClick = onAddSticker) { Icon(Icons.Default.AutoAwesomeMosaic, "Sticker") }
                    IconButton(onClick = onAddQR) { Icon(Icons.Default.QrCode, "QR RSVP") }
                    IconButton(onClick = onShowColors) { Icon(Icons.Default.ColorLens, "Couleur") }
                    IconButton(onClick = onShowFormat) { Icon(Icons.Default.AspectRatio, "Format") }
                    IconButton(onClick = onShowGemini) { 
                        Icon(Icons.Default.AutoAwesome, "IA Fond", tint = MaterialTheme.colorScheme.primary) 
                    }
                }
            )
        }
    }

    // Font Menu Dialog
    if (showFontMenu) {
        FontMenuDialog(
            currentFont = "Normal",
            onFontSelected = { font ->
                viewModel.updateSelectedElementFont(font)
                showFontMenu = false
            },
            onDismiss = { showFontMenu = false }
        )
    }
}

@Composable
fun TextInputDialog(
    text: String,
    onTextChange: (String) -> Unit,
    isEditing: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Modifier le message" else "Nouveau message") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Appliquer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun ColorPickerDialog(
    isElementSelected: Boolean,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        Color.Black, Color.DarkGray, Color(0xFFF44336), Color(0xFFE91E63),
        Color(0xFF9C27B0), Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF4CAF50),
        Color(0xFFFFEB3B), Color(0xFFFF9800), Color.White
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sélectionner une couleur") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (isElementSelected) "Couleur de l'élément" else "Couleur de fond",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    colors.forEach { color ->
                        Surface(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(44.dp)
                                .clickable { onColorSelected(color) },
                            shape = MaterialTheme.shapes.small,
                            color = color
                        ) {}
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
    )
}

@Composable
fun FormatMenuDialog(
    selectedFormat: PageFormat,
    onFormatSelected: (PageFormat) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Format de la page") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                PageFormat.entries.forEach { format ->
                    TextButton(
                        onClick = { onFormatSelected(format) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            format.label,
                            modifier = Modifier.fillMaxWidth(),
                            color = if (selectedFormat == format)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
    )
}

@Composable
fun AIGenerationDialog(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Génération IA (Fond)") },
        text = {
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                label = { Text("Style ou description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
        },
        confirmButton = {
            Button(onClick = onGenerate) { Text("Générer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun StickerMenuDialog(
    stickers: List<androidx.compose.material.icons.ImageVector>,
    onStickerSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un sticker") },
        text = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                stickers.forEachIndexed { index, icon ->
                    IconButton(onClick = { onStickerSelected(index) }) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
    )
}

@Composable
fun FontMenuDialog(
    currentFont: String,
    onFontSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val fonts = listOf("Normal", "Serif", "Cursive", "Monospace")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Police de caractère") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                fonts.forEach { font ->
                    TextButton(
                        onClick = { onFontSelected(font) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            font,
                            modifier = Modifier.fillMaxWidth(),
                            color = if (currentFont == font)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
    )
}
