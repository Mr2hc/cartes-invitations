package com.invitecard.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.invitecard.viewmodel.Category
import com.invitecard.viewmodel.DashboardViewModel
import com.invitecard.viewmodel.Template

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToEditor: (templateId: String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Étape 1 : Choisir un modèle", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text("InviteCard Premium", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold) 
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
        DashboardContent(
            modifier = Modifier.padding(paddingValues),
            categories = uiState.categories,
            styles = uiState.availableStyles,
            templates = uiState.filteredTemplates,
            selectedCategory = uiState.selectedCategory,
            selectedStyle = uiState.selectedStyle,
            onCategoryClick = { viewModel.selectCategory(it.name) },
            onStyleClick = { viewModel.selectStyle(it) },
            onTemplateClick = { onNavigateToEditor(it) }
        )
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
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategoryClick(category) },
                    label = { Text(category.name) },
                    leadingIcon = { Icon(category.icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        // Styles Horizontal Scroll
        Text(
            "Univers Visuels",
            modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                InputChip(
                    selected = selectedStyle == null,
                    onClick = { onStyleClick(null) },
                    label = { Text("Tous les styles") }
                )
            }
            items(styles) { style ->
                InputChip(
                    selected = selectedStyle == style,
                    onClick = { onStyleClick(style) },
                    label = { Text(style) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Template Grid
        val title = when {
            selectedCategory != null && selectedStyle != null -> "$selectedStyle - $selectedCategory"
            selectedCategory != null -> "Modèles $selectedCategory"
            selectedStyle != null -> "Style $selectedStyle"
            else -> "Modèles Premium"
        }
        
        Text(
            title,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(templates, key = { it.id }) { template ->
                TemplateCard(template, onClick = { onTemplateClick(template.id) })
            }
        }
    }
}

@Composable
fun TemplateCard(template: Template, onClick: () -> Unit) {
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
