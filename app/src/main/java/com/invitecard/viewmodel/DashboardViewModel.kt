package com.invitecard.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class Category(val name: String, val icon: ImageVector)
data class Template(val id: String, val imageUrl: String, val category: String, val style: String)

data class DashboardUiState(
    val categories: List<Category> = emptyList(),
    val popularTemplates: List<Template> = emptyList(),
    val selectedCategory: String? = null,
    val selectedStyle: String? = null
) {
    val filteredTemplates: List<Template>
        get() = popularTemplates.filter { template ->
            (selectedCategory == null || template.category == selectedCategory) &&
            (selectedStyle == null || template.style == selectedStyle)
        }
    
    val availableStyles: List<String>
        get() = listOf("Chic", "Luxe", "Minimaliste", "Romantique", "Floral", "Moderne", "Festif", "Corporate", "Créatif")
}

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {
    companion object {
        private val defaultCategories = listOf(
            Category("Tous", Icons.Default.AllInclusive),
            Category("Mariage", Icons.Default.Favorite),
            Category("Anniversaire", Icons.Default.Cake),
            Category("Baptême", Icons.Default.ChildCare),
            Category("Fête", Icons.Default.Celebration),
            Category("Business", Icons.Default.Business),
            Category("Baby Shower", Icons.Default.ChildFriendly)
        )
    }

    private val _uiState = MutableStateFlow(DashboardUiState(
        categories = defaultCategories,
        popularTemplates = generatePremiumTemplates()
    ))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun selectCategory(categoryName: String?) {
        _uiState.update { it.copy(selectedCategory = if (categoryName == "Tous") null else categoryName) }
    }

    fun selectStyle(styleName: String?) {
        _uiState.update { it.copy(selectedStyle = styleName) }
    }

    companion object {
        val categoryPhotos = mapOf(
            "Mariage" to listOf(
                "photo-1512909006721-3d6018887383",
                "photo-1522673607200-164d1b6ce486",
                "photo-1511795409834-ef04bbd61622",
                "photo-1583939003579-730e3918a45a",
                "photo-1501281668745-f7f57925c3b4"
            ),
            "Anniversaire" to listOf(
                "photo-1504196606672-aef5c9cefc92",
                "photo-1558301211-0d8c8ddee6ec",
                "photo-1530103862676-de8c9debad1d",
                "photo-1513151233558-d860c5398176",
                "photo-1513201099705-a9746e1e201f"
            ),
            "Baptême" to listOf(
                "photo-1549417229-aa67d3263c09",
                "photo-1579783900882-c0d3dad7b119",
                "photo-1519689680058-324335c77eba",
                "photo-1478812954026-9c750f0e89fc",
                "photo-1509198397868-475647b2a1e5"
            ),
            "Fête" to listOf(
                "photo-1492684223066-81342ee5ff30",
                "photo-1513151233558-d860c5398176",
                "photo-1618005182384-a83a8bd57fbe",
                "photo-1620121692029-d088224ddc74",
                "photo-1513151233558-d860c5398176"
            ),
            "Business" to listOf(
                "photo-1526253038957-bce54e05968e",
                "photo-1486406146926-c627a92ad1ab",
                "photo-1557804506-669a67965ba0",
                "photo-1497366216548-37526070297c",
                "photo-1562564055-71e051d33c19"
            ),
            "Baby Shower" to listOf(
                "photo-1555252333-9f8e92e65df9",
                "photo-1519689680058-324335c77eba",
                "photo-1502086223501-7ea6ecd79368",
                "photo-1544816155-12df9643f363",
                "photo-1596461404969-9ae70f2830c1"
            )
        )

        fun getTemplateUrl(id: String): String {
            val index = id.removePrefix("tmpl_").toIntOrNull() ?: 0
            val categories = listOf("Mariage", "Anniversaire", "Baptême", "Fête", "Business", "Baby Shower")
            
            val cat = categories[index % categories.size]
            val photos = categoryPhotos[cat] ?: emptyList()
            val photoIndex = (index / categories.size) % photos.size
            val photoId = photos[photoIndex]
            
            return "https://images.unsplash.com/$photoId?auto=format&fit=crop&w=800&q=80"
        }
    }

    private fun generatePremiumTemplates(): List<Template> {
        val categories = listOf("Mariage", "Anniversaire", "Baptême", "Fête", "Business", "Baby Shower")
        val styles = listOf("Chic", "Luxe", "Minimaliste", "Romantique", "Floral", "Moderne", "Festif", "Corporate", "Créatif")
        return (0 until 30).map { i ->
            val cat = categories[i % categories.size]
            val style = styles[i % styles.size]
            Template(
                id = "tmpl_$i",
                imageUrl = getTemplateUrl("tmpl_$i"),
                category = cat,
                style = style
            )
        }
    }
}
