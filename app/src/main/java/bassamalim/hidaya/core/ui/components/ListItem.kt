package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import bassamalim.hidaya.core.ui.theme.appTypography
import bassamalim.hidaya.core.ui.theme.dimensions

/**
 * Standard list row: optional leading slot (e.g. icon), headline, optional
 * supporting text, optional trailing slot (e.g. switch, chevron, count).
 *
 * Use this anywhere a screen renders a vertical list of similar rows.
 */
@Composable
fun MyListItem(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val dims = MaterialTheme.dimensions
    val rowModifier = modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = dims.listItemHeight)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = dims.spaceLg, vertical = dims.spaceMd)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Box(
                modifier = Modifier.size(dims.iconMd),
                contentAlignment = Alignment.Center,
            ) { leading() }
            Spacer(Modifier.width(dims.spaceLg))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = headline,
                style = MaterialTheme.appTypography.title,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (supporting != null) {
                Spacer(Modifier.size(dims.spaceXs))
                Text(
                    text = supporting,
                    style = MaterialTheme.appTypography.label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (trailing != null) {
            Spacer(Modifier.width(dims.spaceSm))
            trailing()
        }
    }
}

/**
 * Settings-style row: label on one side, control (switch/checkbox/value text)
 * on the other. Use this for settings screens and similar key/value lists.
 */
@Composable
fun MySettingItem(
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    val dims = MaterialTheme.dimensions
    val rowModifier = modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = dims.listItemHeight)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = dims.spaceLg, vertical = dims.spaceMd)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.appTypography.subtitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (description != null) {
                Spacer(Modifier.size(dims.spaceXs))
                Text(
                    text = description,
                    style = MaterialTheme.appTypography.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.width(dims.spaceSm))
        trailing()
    }
}
