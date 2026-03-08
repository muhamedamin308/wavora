package com.wavora.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
//  WAVORA Brand Palette
//  Deep violet-navy + coral accent. OLED-friendly dark theme (true black).
// ─────────────────────────────────────────────────────────────────────────────

// ── Primary — Electric Violet ─────────────────────────────────────────────────
val Violet10 = Color(0xFF0D0B1F)
val Violet20 = Color(0xFF1A1635)
val Violet30 = Color(0xFF2A2356)
val Violet40 = Color(0xFF3B3079)
val Violet50 = Color(0xFF4D3F9C)
val Violet60 = Color(0xFF6152BD)
val Violet70 = Color(0xFF7B6CD8)
val Violet80 = Color(0xFF9D90EF)
val Violet90 = Color(0xFFC3BBFF)
val Violet95 = Color(0xFFE3DFFF)
val Violet99 = Color(0xFFF5F4FF)

// ── Secondary — Soft Indigo ───────────────────────────────────────────────────
val Indigo20 = Color(0xFF161B4A)
val Indigo40 = Color(0xFF2D3899)
val Indigo60 = Color(0xFF5260CC)
val Indigo80 = Color(0xFF9CA8FF)
val Indigo90 = Color(0xFFCDD3FF)

// ── Accent — Coral (for playback controls, highlights) ───────────────────────
val Coral30 = Color(0xFF7A1F2E)
val Coral40 = Color(0xFFA03040)
val Coral50 = Color(0xFFCC4558)
val Coral60 = Color(0xFFE86070)
val Coral70 = Color(0xFFFF8090)
val Coral80 = Color(0xFFFFADB7)
val Coral90 = Color(0xFFFFD9DC)

// ── Neutral — True dark for OLED ─────────────────────────────────────────────
val Black = Color(0xFF000000)  // true OLED black — saves battery on AMOLED
val Neutral05 = Color(0xFF0C0C0F)
val Neutral10 = Color(0xFF1A1A1F)
val Neutral15 = Color(0xFF222228)
val Neutral20 = Color(0xFF2C2C33)
val Neutral30 = Color(0xFF3E3E47)
val Neutral40 = Color(0xFF55555F)
val Neutral50 = Color(0xFF6E6E78)
val Neutral60 = Color(0xFF8A8A93)
val Neutral70 = Color(0xFFA6A6AE)
val Neutral80 = Color(0xFFC3C3CA)
val Neutral90 = Color(0xFFE1E1E6)
val Neutral95 = Color(0xFFF0F0F4)
val White = Color(0xFFFFFFFF)

// ── Surface variants used for card backgrounds ────────────────────────────────
val SurfaceDark = Color(0xFF12121A)   // slightly elevated from black
val SurfaceDark2 = Color(0xFF1C1C26)
val SurfaceDark3 = Color(0xFF26263A)

// ── Semantic ─────────────────────────────────────────────────────────────────
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFF9800)
val Error = Color(0xFFCF6679)

// ─────────────────────────────────────────────────────────────────────────────
//  Light theme seed colors (Material You — dynamic color fallback)
// ─────────────────────────────────────────────────────────────────────────────
val LightPrimary = Violet60
val LightOnPrimary = White
val LightPrimaryContainer = Violet90
val LightOnPrimaryContainer = Violet10

val LightSecondary = Indigo60
val LightOnSecondary = White

val LightBackground = Violet99
val LightOnBackground = Violet10
val LightSurface = White
val LightOnSurface = Neutral10
val LightSurfaceVariant = Violet95
val LightOnSurfaceVariant = Neutral40
val LightOutline = Neutral60

// ─────────────────────────────────────────────────────────────────────────────
//  Dark theme seed colors (OLED-first)
// ─────────────────────────────────────────────────────────────────────────────
val DarkPrimary = Violet80
val DarkOnPrimary = Violet20
val DarkPrimaryContainer = Violet30
val DarkOnPrimaryContainer = Violet90

val DarkSecondary = Indigo80
val DarkOnSecondary = Indigo20

val DarkBackground = Black          // true OLED black
val DarkOnBackground = Neutral90
val DarkSurface = SurfaceDark    // slightly raised
val DarkOnSurface = Neutral90
val DarkSurfaceVariant = SurfaceDark2
val DarkOnSurfaceVariant = Neutral70
val DarkOutline = Neutral40

// ── Playback accent (same in both themes for consistency) ─────────────────────
val PlaybackAccent = Coral60        // play button, seek bar thumb, active state
val PlaybackAccentDark = Coral70
