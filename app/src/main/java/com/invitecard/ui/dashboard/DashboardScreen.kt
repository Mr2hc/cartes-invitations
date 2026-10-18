package com.invitecard.ui.dashboard

import android.widget.Toast
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.invitecard.viewmodel.Category
import com.invitecard.viewmodel.DashboardViewModel
import com.invitecard.viewmodel.Template
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToEditor: (templateId: String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Étape 1 : Choisir un modèle", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text("InviteCard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold) 
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToEditor(null) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Créer de zéro") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            DashboardContent(
                categories = uiState.categories,
                styles = uiState.availableStyles,
                templates = uiState.filteredTemplates,
                selectedCategory = uiState.selectedCategory,
                selectedStyle = uiState.selectedStyle,
                onCategoryClick = { viewModel.selectCategory(it.name) },
                onStyleClick = { viewModel.selectStyle(it) },
                onTemplateClick = { onNavigateToEditor(it) }
            )
            
            // Lien PayPal discret pour soutenir le projet
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(28.dp)
                    .background(Color(0xFF0070BA)) // Couleur bleue PayPal
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures { onClick -> 
                            val payPalUrl = "https://paypal.me/BriceFene"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(payPalUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Payments, // Icône de paiement
                    contentDescription = "Soutenir le projet",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Texte discret en bas pour encourager le soutien
            Text(
                text = "Application gratuite - Soutenez le développement via PayPal",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .alpha(0.7f)
                ,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    categories: List<Category>,
    styles: List<String>,
    templates: List<Template>,
    selectedCategory: String?,
    selectedStyle: String?,
    onCategoryClick: (Category) -> Unit,
    onStyleClick: (String?) -> Unit,
    onTemplateClick: (String) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Categories Horizontal Scroll
        Text(
            "Événements",
            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category.name || (selectedCategory == null && category.name == "Tous")
                
                CategoryChip(
                    category = category.name,
                    isSelected = isSelected,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
        
        // Styles Horizontal Scroll
        Text(
            "Styles",
            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(styles) { style ->
                val isSelected = selectedStyle == style
                
                StyleChip(
                    style = style,
                    isSelected = isSelected,
                    onClick = { onStyleClick(style) }
                )
            }
        }
        
        // Templates Grid
        Text(
            "Modèles",
            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(templates) { template ->
                TemplateCard(
                    template = template,
                    onClick = { onTemplateClick(template.id) }
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = category,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun StyleChip(
    style: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = style,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun TemplateCard(
    template: Template,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box {
            AsyncImage(
                model = template.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Style & Category Badge
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        template.style, 
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        template.category, 
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}