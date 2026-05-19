package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.dimensions

/**
 * Lightweight wrapper so previews render with the project's tokens/colours.
 * Keep all preview-only code in this file so production source stays clean.
 */
@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
    AppTheme(theme = Theme.LIGHT) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.dimensions.spaceLg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spaceSm),
        ) { content() }
    }
}

@Preview(name = "MyCard", showBackground = true)
@Composable
private fun MyCardPreview() {
    PreviewSurface {
        MyCard {
            Text("Card headline", style = MaterialTheme.typography.titleMedium)
            Text("Body content goes here.")
        }
        MyCard(onClick = {}) {
            Text("Clickable card", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(name = "MyListItem", showBackground = true)
@Composable
private fun MyListItemPreview() {
    PreviewSurface {
        MyListItem(
            headline = "Notifications",
            supporting = "Manage prayer reminders",
            leading = { Icon(Icons.Default.Settings, contentDescription = null) },
            onClick = {},
        )
        MyListItem(
            headline = "Headline only",
            onClick = {},
        )
    }
}

@Preview(name = "MySettingItem", showBackground = true)
@Composable
private fun MySettingItemPreview() {
    PreviewSurface {
        MySettingItem(
            label = "Dark mode",
            description = "Use a dark colour scheme",
            trailing = { Switch(checked = true, onCheckedChange = {}) },
        )
        MySettingItem(
            label = "Language",
            trailing = { Text("English") },
            onClick = {},
        )
    }
}

@Preview(name = "MySectionHeader", showBackground = true)
@Composable
private fun MySectionHeaderPreview() {
    PreviewSurface {
        MySectionHeader(title = "Favourites")
        MySectionHeader(
            title = "Recent",
            trailing = { Text("See all", color = MaterialTheme.colorScheme.primary) },
        )
    }
}

@Preview(name = "MyEmptyState", showBackground = true, heightDp = 320)
@Composable
private fun MyEmptyStatePreview() {
    PreviewSurface {
        MyEmptyState(
            message = "Nothing here yet",
            icon = Icons.Default.Inbox,
        )
    }
}

@Preview(name = "MyErrorState", showBackground = true, heightDp = 320)
@Composable
private fun MyErrorStatePreview() {
    PreviewSurface {
        MyErrorState(
            message = "Something went wrong while loading.",
            onRetry = {},
        )
    }
}

@Preview(name = "MyButton", showBackground = true)
@Composable
private fun MyButtonPreview() {
    PreviewSurface {
        MyButton(text = "Primary", onClick = {})
        MyFilledTonalButton(text = "Tonal", onClick = {})
        MyOutlinedButton(text = "Outlined", onClick = {})
        MyTextButton(text = "Text", onClick = {})
    }
}

@Preview(name = "MyText", showBackground = true)
@Composable
private fun MyTextPreview() {
    PreviewSurface {
        MyText(text = "Default body text")
        MyText(text = "Smaller caption", fontSize = MaterialTheme.typography.labelSmall.fontSize)
    }
}

@Preview(name = "MyDialog", showBackground = true, heightDp = 320)
@Composable
private fun MyDialogPreview() {
    PreviewSurface {
        MyDialog(shown = true) {
            Column(
                modifier = Modifier.padding(MaterialTheme.dimensions.spaceLg),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spaceSm),
            ) {
                DialogTitle("Dialog title")
                MyText("Dialog body content.")
            }
        }
    }
}
