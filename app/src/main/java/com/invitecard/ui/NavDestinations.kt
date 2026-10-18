package com.invitecard.ui

import kotlinx.serialization.Serializable

@Serializable
object Dashboard

@Serializable
data class Editor(val templateId: String? = null)
