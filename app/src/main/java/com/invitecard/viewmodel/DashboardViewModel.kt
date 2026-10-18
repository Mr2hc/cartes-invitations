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
        val defaultCategories = listOf(
            Category("Tous", Icons.Default.AllInclusive),
            Category("Mariage", Icons.Default.Favorite),
            Category("Anniversaire", Icons.Default.Cake),
            Category("Baptême", Icons.Default.ChildCare),
            Category("Fête", Icons.Default.Celebration),
            Category("Business", Icons.Default.Business),
            Category("Baby Shower", Icons.Default.ChildFriendly)
        )

        val categoryPhotos = mapOf(
            "Mariage" to listOf(
                "photo-1519741497674-611481863552", // Couple enlacé et fleurs
                "photo-1522673607200-164d1b6ce486", // Chaises fleuries réception jardin
                "photo-1511795409834-ef04bbd61622", // Table banquet mariage
                "photo-1583939003579-730e3918a45a", // Baiser des mariés sous pétales
                "photo-1520854221256-17451cc331bf"  // Robe blanche et alliance
            ),
            "Anniversaire" to listOf(
                "photo-1504196606672-aef5c9cefc92", // Ballons multicolores
                "photo-1558301211-0d8c8ddee6ec", // Gâteau d'anniversaire rose
                "photo-1530103862676-de8c9debad1d", // Ballons pastels
                "photo-1513151233558-d860c5398176", // Pluie de confettis
                "photo-1513201099705-a9746e1e201f"  // Boîte cadeau ruban
            ),
            "Baptême" to listOf(
                "photo-1544126592-807ade215a0b", // Nouveau-né endormi dans des draps blancs
                "photo-1510154221590-ff63e90a136f", // Mains protectrices sur petons de bébé
                "photo-1491013516836-7db643ee125a", // Visage de bébé aux yeux bleus
                "photo-1519689680058-324335c77eba", // Bébé souriant
                "photo-1516627145497-ae6968895b74"  // Enfant sourire lumière douce
            ),
            "Fête" to listOf(
                "photo-1492684223066-81342ee5ff30", // Confettis festifs sur foule
                "photo-1517457373958-b7bdd4587205", // Guirlandes et terrasse de fête
                "photo-1496337589254-7e19d01cec44", // DJ et lumières de fête
                "photo-1514525253161-7a46d19cd819", // Toast et ambiance soirée
                "photo-1513151233558-d860c5398176"  // Confettis colorés
            ),
            "Business" to listOf(
                "photo-1526253038957-bce54e05968e", // Développeurs et ordinateurs
                "photo-1486406146926-c627a92ad1ab", // Gratte-ciels corporate
                "photo-1557804506-669a67965ba0", // Réunion tableau blanc
                "photo-1497366216548-37526070297c", // Hall moderne entreprise
                "photo-1562564055-71e051d33c19"  // Signature contrat
            ),
            "Baby Shower" to listOf(
                "photo-1555252333-9f8e92e65df9", // Petits pieds de bébé dans couverture blanche
                "photo-1515488042361-ee00e0ddd4e4", // Tapis doudous bébé
                "photo-1510154221590-ff63e90a136f", // Mains tenant les pieds de bébé
                "photo-1491013516836-7db643ee125a", // Bébé mignon
                "photo-1596461404969-9ae70f2830c1"  // Train en bois pastel
            )
        )

        fun getTemplateUrl(id: String): String {
            val index = id.removePrefix("tmpl_").toIntOrNull() ?: 0
            val categories = listOf("Mariage", "Anniversaire", "Baptême", "Fête", "Business", "Baby Shower")
            
            val cat = categories[index % categories.size]
            val photos = categoryPhotos[cat].orEmpty()
            val photoId = if (photos.isNotEmpty()) {
                photos[(index / categories.size) % photos.size]
            } else {
                "photo-1512909006721-3d6018887383"
            }
            
            return "https://images.unsplash.com/$photoId?auto=format&fit=crop&w=800&q=80"
        }
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
