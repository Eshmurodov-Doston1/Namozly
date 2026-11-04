package uz.dostonbek.namozly.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.dostonbek.namozly.core.designsystem.theme.JakartaFamily
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens

data class NavBarItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
)

/**
 * Dizayndagi pastki menyu (NavBar.dc.html): 80dp, 2 tab + markazda ko'tarilgan qibla kompasi + 2 tab.
 */
@Composable
fun NamozlyNavBar(
    items: List<NavBarItem>,
    selectedKey: String?,
    onSelect: (NavBarItem) -> Unit,
    qiblaLabel: String,
    qiblaSelected: Boolean,
    /** Lambda: sensor o'zgarishlari NavBar'ni qayta chizmaydi, faqat kompas belgisini. */
    heading: () -> Float?,
    qiblaBearing: Float,
    compassAligned: Boolean,
    onCompassClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    require(items.size == 4) { "NamozlyNavBar 4 ta element kutadi" }
    val t = NamozlyThemeTokens.t
    Column(modifier.fillMaxWidth().background(t.nav)) {
        HorizontalDivider(thickness = 1.dp, color = t.line)
        Row(
            Modifier.fillMaxWidth().height(79.dp).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.take(2).forEach { NavItem(it, it.key == selectedKey, Modifier.weight(1f)) { onSelect(it) } }
            // Markaz: kompas
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onCompassClick),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-26).dp)
                        .shadow(if (compassAligned) 14.dp else 10.dp, CircleShape, ambientColor = Color(0x590A3D33), spotColor = Color(0x590A3D33))
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(t.bg)
                        .padding(4.dp),
                ) {
                    QiblaCompassBadge(heading = heading, qiblaBearing = qiblaBearing, aligned = compassAligned, size = 58.dp)
                }
                Text(
                    qiblaLabel,
                    modifier = Modifier.padding(bottom = 10.dp),
                    fontFamily = JakartaFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (qiblaSelected) (if (t.isDark) t.strong else Color(0xFF0A3D33)) else t.navOff,
                )
            }
            items.drop(2).forEach { NavItem(it, it.key == selectedKey, Modifier.weight(1f)) { onSelect(it) } }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun NavItem(item: NavBarItem, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val t = NamozlyThemeTokens.t
    val on = if (t.isDark) Color(0xFFF6F1E6) else Color(0xFF0A3D33)
    val textColor by animateColorAsState(if (selected) on else t.navOff, label = "navText")
    val iconColor by animateColorAsState(if (selected) t.icon else t.navOff, label = "navIcon")
    val pill by animateColorAsState(if (selected) t.chip else Color.Transparent, label = "navPill")
    val pillWidth by animateDpAsState(if (selected) 60.dp else 48.dp, label = "pillW")
    Column(
        modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Box(
            Modifier.size(width = pillWidth, height = 32.dp).clip(RoundedCornerShape(16.dp)).background(pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(if (selected) item.selectedIcon else item.icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        }
        Text(
            item.label,
            fontFamily = JakartaFamily,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
