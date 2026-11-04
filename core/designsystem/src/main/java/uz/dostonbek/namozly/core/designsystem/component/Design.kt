package uz.dostonbek.namozly.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import uz.dostonbek.namozly.core.designsystem.theme.JakartaFamily
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens

/*
 * Dizayn fayllaridagi (*.dc.html) qurilish bloklari — px qiymatlari dp/sp ga 1:1 o'tkazilgan.
 */

/** Dizayndagi ichki chiziq rangi (#F0EBDF / dark: line). */
val DividerSoft: Color
    @Composable get() = if (NamozlyThemeTokens.t.isDark) NamozlyThemeTokens.t.line else Color(0xFFF0EBDF)

@Composable
fun DText(
    text: String,
    size: TextUnit,
    weight: FontWeight = FontWeight.Normal,
    color: Color = NamozlyThemeTokens.t.text,
    modifier: Modifier = Modifier,
    spacing: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    align: TextAlign? = null,
) = Text(
    text, modifier, color = color, fontSize = size, fontWeight = weight, fontFamily = JakartaFamily,
    letterSpacing = spacing, lineHeight = lineHeight, maxLines = maxLines, overflow = TextOverflow.Ellipsis, textAlign = align,
)

/** 44dp oq doiradagi orqaga tugmasi. */
@Composable
fun DBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val t = NamozlyThemeTokens.t
    Box(
        modifier.size(44.dp).clip(CircleShape).background(t.surface).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = t.strong, modifier = Modifier.size(20.dp)) }
}

/** 44dp doira ichidagi ikonka tugmasi. */
@Composable
fun DIconButton(icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color? = null, background: Color? = null, contentDescription: String? = null) {
    val t = NamozlyThemeTokens.t
    Box(
        modifier.size(44.dp).clip(CircleShape).background(background ?: t.surface).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription, tint = tint ?: t.strong, modifier = Modifier.size(20.dp)) }
}

/**
 * Ekran sarlavhasi: [orqaga] Sarlavha (22sp/800) + ixtiyoriy izoh (13sp). Orqaga bo'lmasa — 24sp.
 */
@Composable
fun DHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val t = NamozlyThemeTokens.t
    Row(
        modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (onBack != null) DBackButton(onBack)
        Column(Modifier.weight(1f)) {
            DText(title, if (onBack != null) 22.sp else 24.sp, FontWeight.ExtraBold, t.strong, maxLines = 1)
            if (subtitle != null) DText(subtitle, 13.sp, FontWeight.Normal, t.muted, maxLines = 1)
        }
        actions()
    }
}

/** Oq karta (dizayndagi radius 18–24). */
@Composable
fun DCard(
    modifier: Modifier = Modifier,
    radius: Dp = 20.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    color: Color = NamozlyThemeTokens.t.surface,
    onClick: (() -> Unit)? = null,
    spacing: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius))
            .background(color)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

/** Kichik bosh harfli bo'lim yorlig'i: "IFTOR DUOSI", "UMUMIY". */
@Composable
fun DOverline(text: String, modifier: Modifier = Modifier, color: Color = NamozlyThemeTokens.t.accent) {
    DText(text.uppercase(), 12.sp, FontWeight.Bold, color, modifier, spacing = 0.04.em)
}

/** Sozlamalar uslubidagi qator: chapda nom, o'ngda qiymat va ">" belgisi. */
@Composable
fun DRow(
    title: String,
    value: String? = null,
    height: Dp = 46.dp,
    divider: Boolean = false,
    chevron: Boolean = true,
    onClick: (() -> Unit)? = null,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val t = NamozlyThemeTokens.t
    Column {
        if (divider) HorizontalDivider(thickness = 1.dp, color = DividerSoft)
        Row(
            Modifier
                .fillMaxWidth()
                .height(height)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                DText(title, 15.sp, FontWeight.SemiBold, t.text, maxLines = 1)
                if (subtitle != null) DText(subtitle, 11.sp, FontWeight.Normal, t.muted, maxLines = 1)
            }
            if (value != null) DText(value, 14.sp, FontWeight.SemiBold, t.icon, maxLines = 1)
            trailing?.invoke()
            if (chevron && onClick != null) Icon(Icons.Rounded.ChevronRight, null, tint = t.faint, modifier = Modifier.size(16.dp))
        }
    }
}

/** Pill-tugma (36dp): tanlangani zumrad, qolgani oq. */
@Composable
fun DPill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, height: Dp = 36.dp) {
    val t = NamozlyThemeTokens.t
    Box(
        modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(if (selected) t.primary else t.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) { DText(text, 13.sp, if (selected) FontWeight.Bold else FontWeight.SemiBold, if (selected) Color.White else t.text, maxLines = 1) }
}

/** Segment boshqaruvi (Yorug' | Tungi | Tizim). */
@Composable
fun DSegmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val t = NamozlyThemeTokens.t
    Row(
        Modifier.clip(RoundedCornerShape(10.dp)).background(if (t.isDark) t.chip else Color(0xFFF0EBDF)).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier.height(32.dp).clip(RoundedCornerShape(8.dp)).background(if (on) t.surface else Color.Transparent)
                    .clickable { onSelect(i) }.padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) { DText(label, 12.sp, if (on) FontWeight.Bold else FontWeight.SemiBold, if (on) t.strong else t.muted) }
        }
    }
}

/** Progress chizig'i (6dp). */
@Composable
fun DProgress(progress: Float, track: Color, fill: Color, modifier: Modifier = Modifier, height: Dp = 6.dp) {
    Box(modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(height / 2)).background(track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(height).clip(RoundedCornerShape(height / 2)).background(fill))
    }
}

/** Oltin maslahat kartasi (#FBF4E4). */
@Composable
fun DHint(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    val t = NamozlyThemeTokens.t
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (t.isDark) t.goldSoft else Color(0xFFFBF4E4)).padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, tint = t.accent, modifier = Modifier.size(20.dp))
        DText(text, 13.sp, FontWeight.Normal, if (t.isDark) t.goldText else Color(0xFF5C4517), lineHeight = 19.sp)
    }
}
