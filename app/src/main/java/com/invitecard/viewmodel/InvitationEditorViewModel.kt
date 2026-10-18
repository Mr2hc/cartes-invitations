package com.invitecard.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.invitecard.ui.Editor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class SerializableOffset(val x: Float, val y: Float) {
    fun toOffset() = Offset(x, y)
}

fun Offset.toSerializable() = SerializableOffset(x, y)

@Serializable
data class VisualElement(
    val id: String,
    val type: ElementType,
    val text: String = "",
    val stickerIndex: Int? = null,
    val colorArgb: Int = Color.Black.toArgb(),
    val fontSize: Float = 24f,
    val fontFamilyName: String = "SansSerif",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val alignment: Int = 1, // 0: Left, 1: Center, 2: Right
    val position: SerializableOffset = SerializableOffset(100f, 100f),
    val scale: Float = 1f,
    val hasShadow: Boolean = false,
    val shadowColorArgb: Int = Color.Black.copy(alpha = 0.5f).toArgb(),
    val shadowOffsetX: Float = 4f,
    val shadowOffsetY: Float = 4f,
    val shadowRadius: Float = 4f,
    val hasGradient: Boolean = false,
    val gradientStartColorArgb: Int = Color(0xFFFF9800).toArgb(),
    val gradientEndColorArgb: Int = Color(0xFFE91E63).toArgb()
) {
    val color: Color get() = Color(colorArgb)
    val textAlign: TextAlign get() = when(alignment) {
        0 -> TextAlign.Left
        2 -> TextAlign.Right
        else -> TextAlign.Center
    }
}

enum class ElementType { TEXT, STICKER, QR_CODE }

enum class PageFormat(val label: String, val widthRatio: Float, val heightRatio: Float) {
    A4_PORTRAIT("A4 Portrait", 1f, 1.414f),
    A4_LANDSCAPE("A4 Paysage", 1.414f, 1f),
    CARD_PORTRAIT("Carte Standard", 0.714f, 1f),
    HALF_PAGE("Demi-page", 1f, 0.707f)
}

data class EditorUiState(
    val backgroundColor: Color = Color.White,
    val backgroundImage: String? = null,
    val format: PageFormat = PageFormat.CARD_PORTRAIT,
    val rotation: Float = 0f,
    val saturation: Float = 1f,
    val elements: List<VisualElement> = emptyList(),
    val selectedElementId: String? = null,
    val isGeneratingImage: Boolean = false
)

@HiltViewModel
class InvitationEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val editorRoute: Editor = savedStateHandle.toRoute<Editor>()
    val templateId: String? = editorRoute.templateId

    private val _uiState = MutableStateFlow(EditorUiState(
        backgroundImage = templateId?.let { DashboardViewModel.getTemplateUrl(it) }
    ))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun addTextElement(text: String) {
        val id = System.currentTimeMillis().toString()
        val newElement = VisualElement(id = id, type = ElementType.TEXT, text = text)
        _uiState.update { it.copy(
            elements = it.elements + newElement,
            selectedElementId = id
        ) }
    }

    fun addStickerElement(index: Int) {
        val id = System.currentTimeMillis().toString()
        val newElement = VisualElement(id = id, type = ElementType.STICKER, stickerIndex = index, position = SerializableOffset(300f, 300f))
        _uiState.update { it.copy(
            elements = it.elements + newElement,
            selectedElementId = id
        ) }
    }

    fun addQRCodeElement(url: String) {
        val id = "qr_${System.currentTimeMillis()}"
        val newElement = VisualElement(
            id = id, 
            type = ElementType.QR_CODE, 
            text = url, 
            position = SerializableOffset(400f, 400f),
            scale = 1.5f
        )
        _uiState.update { it.copy(elements = it.elements + newElement, selectedElementId = id) }
    }

    fun selectElement(id: String?) {
        _uiState.update { it.copy(selectedElementId = id) }
    }

    fun deleteSelectedElement() {
        _uiState.update { state ->
            state.copy(
                elements = state.elements.filter { it.id != state.selectedElementId },
                selectedElementId = null
            )
        }
    }

    fun moveSelectedToFront() {
        _uiState.update { state ->
            val element = state.elements.find { it.id == state.selectedElementId } ?: return@update state
            val remaining = state.elements.filter { it.id != state.selectedElementId }
            state.copy(elements = remaining + element)
        }
    }

    fun moveSelectedToBack() {
        _uiState.update { state ->
            val element = state.elements.find { it.id == state.selectedElementId } ?: return@update state
            val remaining = state.elements.filter { it.id != state.selectedElementId }
            state.copy(elements = listOf(element) + remaining)
        }
    }

    fun updateElementPosition(id: String, delta: Offset) {
        _uiState.update { state ->
            state.copy(elements = state.elements.map {
                if (it.id == id) {
                    val newPos = Offset(it.position.x + delta.x, it.position.y + delta.y)
                    it.copy(position = newPos.toSerializable())
                } else it
            })
        }
    }

    fun updateElementScale(id: String, scaleFactor: Float) {
        _uiState.update { state ->
            state.copy(elements = state.elements.map {
                if (it.id == id) it.copy(scale = (it.scale * scaleFactor).coerceIn(0.5f, 5f)) else it
            })
        }
    }

    fun setBackground(color: Color) {
        _uiState.update { it.copy(backgroundColor = color, backgroundImage = null) }
    }

    fun setPersonalImage(uri: String) {
        _uiState.update { it.copy(backgroundImage = uri, backgroundColor = Color.Transparent) }
    }

    fun updateSelectedElementColor(color: Color) {
        _uiState.update { state ->
            state.copy(elements = state.elements.map {
                if (it.id == state.selectedElementId) it.copy(colorArgb = color.toArgb()) else it
            })
        }
    }

    fun updateSelectedElementFont(fontName: String) {
        _uiState.update { state ->
            state.copy(elements = state.elements.map {
                if (it.id == state.selectedElementId) it.copy(fontFamilyName = fontName) else it
            })
        }
    }

    fun toggleSelectedElementBold() {
        _uiState.update { state ->
            state.copy(elements = state.elements.map {
                if (it.id == state.selectedElementId) it.copy(isBold = !it.isBold) else it
            })
        }
    }

    fun toggleSelectedElementItalic() {
        _uiState.update { state ->
            state.copy(elements = state.elements.map {
                if (it.id == state.selectedElementId) it.copy(isItalic = !it.isItalic) else it
            })
        }
    }

    fun updateSelectedElementText(text: String) {
        _uiState.update { state ->
            state.copy(elements = state.elements.map {
                if (it.id == state.selectedElementId) it.copy(text = text) else it
            })
        }
    }

    fun rotateImage() {
        _uiState.update { it.copy(rotation = (it.rotation + 90f) % 360f) }
    }

    fun setFormat(format: PageFormat) {
        _uiState.update { it.copy(format = format) }
    }

    fun updateSelectedElementAlignment(alignment: TextAlign) {
        _uiState.update { state ->
            val alignInt = when(alignment) {
                TextAlign.Left -> 0
                TextAlign.Right -> 2
                else -> 1
            }
            state.copy(elements = state.elements.map {
                if (it.id == state.selectedElementId) it.copy(alignment = alignInt) else it
            })
        }
    }

    fun generateWithIA(prompt: String) {
        _uiState.update { it.copy(isGeneratingImage = true) }
        val enhancedPrompt = prompt.lowercase().replace(" ", ",")
        val index = System.currentTimeMillis() % 1000
        val imageUrl = "https://loremflickr.com/800/1200/invitation,$enhancedPrompt?lock=$index"
        _uiState.update { it.copy(backgroundImage = imageUrl, isGeneratingImage = false) }
    }

    fun updateSelectedElementShadow(
        enabled: Boolean,
        color: Color,
        dx: Float,
        dy: Float,
        radius: Float
    ) {
        _uiState.update { state ->
            state.copy(elements = state.elements.map {
                if (it.id == state.selectedElementId) {
                    it.copy(
                        hasShadow = enabled,
                        shadowColorArgb = color.toArgb(),
                        shadowOffsetX = dx,
                        shadowOffsetY = dy,
                        shadowRadius = radius
                    )
                } else it
            })
        }
    }

    fun updateSelectedElementGradient(
        enabled: Boolean,
        startColor: Color,
        endColor: Color
    ) {
        _uiState.update { state ->
            state.copy(elements = state.elements.map {
                if (it.id == state.selectedElementId) {
                    it.copy(
                        hasGradient = enabled,
                        gradientStartColorArgb = startColor.toArgb(),
                        gradientEndColorArgb = endColor.toArgb()
                    )
                } else it
            })
        }
    }

    fun startGenerating() {
        _uiState.update { it.copy(isGeneratingImage = true) }
    }

    fun loadDraft(elements: List<VisualElement>) {
        _uiState.update { it.copy(elements = elements) }
    }
}
