package com.blood.spectra.ui.palettes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blood.spectra.SpectraViewModel
import com.blood.spectra.data.Palette
import com.blood.spectra.logic.ColorFormats
import com.blood.spectra.logic.ColorValue
import com.blood.spectra.ui.SpectraIcons
import com.blood.spectra.ui.common.checkerboard

@Composable
fun PalettesScreen(
    vm: SpectraViewModel,
    modifier: Modifier = Modifier,
) {
    val palettes by vm.palettes.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var expandedId by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(modifier.fillMaxSize()) {
        if (palettes.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(palettes, key = { it.id }) { palette ->
                    PaletteCard(
                        palette = palette,
                        expanded = expandedId == palette.id,
                        onToggle = { expandedId = if (expandedId == palette.id) null else palette.id },
                        onPickColor = { vm.loadColor(it) },
                        onRemoveColor = { vm.removeColorFromPalette(palette.id, it.argb) },
                        onDelete = { vm.deletePalette(palette.id); expandedId = null },
                        onRename = { vm.renamePalette(palette.id, it) },
                        onExport = { shareText(context, exportPalette(palette)) },
                        onAddCurrent = { vm.saveCurrentToPalette(palette.id) },
                    )
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }

        // FAB-like create button
        Surface(
            onClick = { showCreate = true },
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.size(8.dp))
                Text("New palette", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    if (showCreate) {
        NameDialog(
            title = "New palette",
            initial = "",
            onConfirm = { name ->
                vm.createPalette(name) { id -> expandedId = id }
                showCreate = false
            },
            onDismiss = { showCreate = false },
        )
    }
}

@Composable
private fun PaletteCard(
    palette: Palette,
    expanded: Boolean,
    onToggle: () -> Unit,
    onPickColor: (ColorValue) -> Unit,
    onRemoveColor: (ColorValue) -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
    onExport: () -> Unit,
    onAddCurrent: () -> Unit,
) {
    var showRename by remember { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    palette.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${palette.colors.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .clickable { onToggle() },
                )
            }
            Spacer(Modifier.height(10.dp))

            // color preview row (tap to load / expand)
            if (palette.colors.isEmpty()) {
                Text(
                    "Empty — add the current color below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggle() },
                ) {
                    palette.colors.take(10).forEach { sc ->
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .checkerboard(cell = 6.dp)
                                .background(Color(sc.argb)),
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                // detailed grid: each color tappable to load, long-press to remove
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    palette.colors.forEach { sc ->
                        val cv = ColorValue.fromArgb(sc.argb)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .checkerboard(cell = 6.dp)
                                    .background(Color(sc.argb))
                                    .border(1.dp, Color(0x22000000), RoundedCornerShape(8.dp))
                                    .clickable { onPickColor(cv) },
                            )
                            Spacer(Modifier.size(12.dp))
                            Text(
                                ColorFormats.hex(cv),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "Remove",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onRemoveColor(cv) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionChip("Add current", onAddCurrent, Modifier.weight(1f))
                    ActionChip("Export", onExport, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionChip("Rename", { showRename = true }, Modifier.weight(1f))
                    ActionChip("Delete", onDelete, Modifier.weight(1f), danger = true)
                }
            }
        }
    }

    if (showRename) {
        NameDialog(
            title = "Rename palette",
            initial = palette.name,
            onConfirm = { onRename(it); showRename = false },
            onDismiss = { showRename = false },
        )
    }
}

@Composable
private fun ActionChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, danger: Boolean = false) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (danger) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (danger) MaterialTheme.colorScheme.onErrorContainer
        else MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun NameDialog(
    title: String,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it.take(40) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        androidx.compose.material3.Icon(
            SpectraIcons.Palette, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text("No palettes yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        Text(
            "Create a palette, then add colors from the Picker.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

// ---- helpers ----------------------------------------------------------------

private fun exportPalette(p: Palette): String {
    val lines = p.colors.joinToString("\n") { ColorFormats.hex(ColorValue.fromArgb(it.argb)) }
    return "${p.name}\n$lines"
}

private fun shareText(context: android.content.Context, text: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, text)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Export palette"))
}
