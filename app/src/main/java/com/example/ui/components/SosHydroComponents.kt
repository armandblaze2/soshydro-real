package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.GpsCaptureResult
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GasOrange
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SkyTelemetry
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.StockEmerald
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberText
import java.util.Locale

enum class SnackbarTone {
    SUCCESS,
    WARNING,
    ERROR,
    INFO
}

suspend fun SnackbarHostState.showTypedSnackbar(
    message: String,
    tone: SnackbarTone = SnackbarTone.INFO
) {
    currentSnackbarData?.dismiss()
    val encoded = "${tone.name}|$message"
    showSnackbar(
        message = encoded,
        duration = SnackbarDuration.Short
    )
}

@Composable
fun SosHydroSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState = hostState) { data: SnackbarData ->
        val raw = data.visuals.message
        val parts = raw.split("|", limit = 2)
        val tone = if (parts.size == 2) {
            runCatching { SnackbarTone.valueOf(parts[0]) }.getOrDefault(SnackbarTone.INFO)
        } else {
            SnackbarTone.INFO
        }
        val text = if (parts.size == 2) parts[1] else raw

        val containerColor = when (tone) {
            SnackbarTone.SUCCESS -> Color(0xFF065F46)
            SnackbarTone.WARNING -> Color(0xFF9A3412)
            SnackbarTone.ERROR -> Color(0xFF991B1B)
            SnackbarTone.INFO -> Slate800
        }
        val accentColor = when (tone) {
            SnackbarTone.SUCCESS -> StockEmerald
            SnackbarTone.WARNING -> GasOrange
            SnackbarTone.ERROR -> DangerRed
            SnackbarTone.INFO -> SkyTelemetry
        }

        Snackbar(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("app_snackbar"),
            shape = RoundedCornerShape(14.dp),
            containerColor = containerColor,
            contentColor = Color.White
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosHydroTopBar(
    title: String,
    icon: ImageVector? = null,
    iconTint: Color = SkyTelemetry,
    onBackClick: (() -> Unit)? = null,
    trailingBadge: String? = null
) {
    Column {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Slate900,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            ),
            navigationIcon = {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("topbar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            },
            actions = {
                if (trailingBadge != null) {
                    Surface(
                        color = StockEmerald.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, StockEmerald.copy(alpha = 0.35f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StockEmerald)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = trailingBadge,
                                style = MaterialTheme.typography.labelMedium,
                                color = StockEmerald
                            )
                        }
                    }
                }
            }
        )
        HorizontalDivider(color = Slate800, thickness = 1.dp)
    }
}

@Composable
fun GpsNoticeBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = WarningAmber.copy(alpha = 0.10f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = WarningAmberText
            )
        }
    }
}

@Composable
fun SosHydroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    suffixText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    isNumericMono: Boolean = false,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    color = Slate400.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = Slate400
                )
            }
        },
        suffix = suffixText?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = SkyTelemetry
                )
            }
        },
        textStyle = if (isNumericMono) {
            MaterialTheme.typography.bodyLarge.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        } else {
            MaterialTheme.typography.bodyLarge.copy(color = Color.White)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction() },
            onSend = { onImeAction() },
            onGo = { onImeAction() }
        ),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Slate850,
            unfocusedContainerColor = Slate800,
            focusedBorderColor = ElectricBlue,
            unfocusedBorderColor = Slate700,
            focusedLabelColor = SkyTelemetry,
            unfocusedLabelColor = Slate400,
            cursorColor = SkyTelemetry
        )
    )
}

@Composable
fun SosHydroPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "••••••••",
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    testTag: String
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = Slate400.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = Slate400
            )
        },
        trailingIcon = {
            IconButton(
                onClick = { passwordVisible = !passwordVisible },
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = if (passwordVisible) "Masquer le mot de passe" else "Afficher le mot de passe",
                    tint = Slate400
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction() },
            onSend = { onImeAction() },
            onGo = { onImeAction() }
        ),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Slate850,
            unfocusedContainerColor = Slate800,
            focusedBorderColor = ElectricBlue,
            unfocusedBorderColor = Slate700,
            focusedLabelColor = SkyTelemetry,
            unfocusedLabelColor = Slate400,
            cursorColor = SkyTelemetry
        )
    )
}

@Composable
fun GpsConfirmationDialog(
    gps: GpsCaptureResult,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val latStr = String.format(Locale.US, "%.5f", gps.latitude)
    val lngStr = String.format(Locale.US, "%.5f", gps.longitude)
    val accStr = String.format(Locale.US, "%.0f", gps.accuracyMeters)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate800,
        titleContentColor = Color.White,
        textContentColor = Slate400,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(StockEmerald.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.GpsFixed,
                    contentDescription = null,
                    tint = StockEmerald,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.gps_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Latitude  : $latStr",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SkyTelemetry
                        )
                        Text(
                            text = "Longitude : $lngStr",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SkyTelemetry
                        )
                        Text(
                            text = "Précision : à $accStr m près",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.bodyMedium,
                            color = StockEmerald
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Voulez-vous valider et enregistrer ?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("gps_dialog_cancel_button")
            ) {
                Text(
                    text = stringResource(R.string.gps_dialog_cancel),
                    color = Slate400
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StockEmerald,
                    contentColor = Color(0xFF04261B)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("gps_dialog_confirm_button")
            ) {
                Text(
                    text = stringResource(R.string.gps_dialog_confirm),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
