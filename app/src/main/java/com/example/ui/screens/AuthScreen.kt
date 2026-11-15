package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.SnackbarTone
import com.example.ui.components.SosHydroPasswordField
import com.example.ui.components.SosHydroSnackbarHost
import com.example.ui.components.SosHydroTextField
import com.example.ui.components.SosHydroTopBar
import com.example.ui.components.showTypedSnackbar
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GasAmber
import com.example.ui.theme.GasOrange
import com.example.ui.theme.SkyTelemetry
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.StockEmeraldBright
import com.example.ui.viewmodel.AuthNavigationEvent
import com.example.ui.viewmodel.AuthUiState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AuthProfileType {
    STATION,
    DETAILLANT
}

@Composable
fun AuthScreen(
    profileType: AuthProfileType,
    authUiState: AuthUiState,
    lastSavedPhone: String?,
    authEvents: SharedFlow<AuthNavigationEvent>,
    onBack: () -> Unit,
    onSubmitCredentials: (String, String) -> Unit,
    onCreateNew: (String) -> Unit,
    onNavigateToForm: () -> Unit
) {
    val isStation = profileType == AuthProfileType.STATION
    val title = stringResource(if (isStation) R.string.auth_station_title else R.string.auth_detaillant_title)
    val prompt = stringResource(if (isStation) R.string.auth_station_prompt else R.string.auth_detaillant_prompt)
    val phoneLabel = stringResource(if (isStation) R.string.auth_station_phone_label else R.string.auth_detaillant_phone_label)
    val ctaLabel = stringResource(if (isStation) R.string.auth_station_cta else R.string.auth_detaillant_cta)
    val firstVisitPrompt = stringResource(if (isStation) R.string.auth_station_first_visit_prompt else R.string.auth_detaillant_first_visit_prompt)
    val createLabel = stringResource(if (isStation) R.string.auth_station_first_visit else R.string.auth_detaillant_first_visit)
    val credentialsRequiredMsg = stringResource(R.string.auth_credentials_required)

    val accentColor = if (isStation) ElectricBlue else GasOrange
    val iconTint = if (isStation) SkyTelemetry else GasAmber
    val profileIcon = if (isStation) Icons.Rounded.LocalGasStation else Icons.Rounded.LocalFireDepartment
    val createColor = if (isStation) StockEmeraldBright else GasAmber

    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val isLoading = authUiState is AuthUiState.Loading

    val whatsappUrl = if (isStation) {
        "https://wa.me/24166233181?text=Bonjour%2C%20j'ai%20oubli%C3%A9%20le%20mot%20de%20passe%20de%20ma%20station%20sur%20SOS-HYDRO."
    } else {
        "https://wa.me/24166233181?text=Bonjour%2C%20j'ai%20oubli%C3%A9%20le%20mot%20de%20passe%20de%20ma%20boutique%20de%20gaz%20sur%20SOS-HYDRO."
    }

    LaunchedEffect(Unit) {
        authEvents.collectLatest { event ->
            when (event) {
                is AuthNavigationEvent.OpenStationForm -> {
                    if (isStation) onNavigateToForm()
                }
                is AuthNavigationEvent.OpenDetaillantForm -> {
                    if (!isStation) onNavigateToForm()
                }
                is AuthNavigationEvent.ShowError -> {
                    snackbarHostState.showTypedSnackbar(event.message, SnackbarTone.ERROR)
                }
            }
        }
    }

    Scaffold(
        containerColor = Slate950,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SosHydroSnackbarHost(snackbarHostState) },
        topBar = {
            SosHydroTopBar(
                title = title,
                icon = profileIcon,
                iconTint = iconTint,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = profileIcon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        SosHydroTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = phoneLabel,
                            placeholder = stringResource(R.string.auth_phone_hint),
                            leadingIcon = Icons.Rounded.Phone,
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next,
                            isNumericMono = true,
                            testTag = "auth_phone_input"
                        )

                        if (!lastSavedPhone.isNullOrBlank() && phone.trim() != lastSavedPhone) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AssistChip(
                                onClick = { phone = lastSavedPhone },
                                modifier = Modifier.testTag("auth_last_phone_chip"),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.History,
                                        contentDescription = null,
                                        tint = iconTint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(R.string.auth_last_used_phone, lastSavedPhone),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(containerColor = Slate800),
                                border = BorderStroke(1.dp, Slate700)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        SosHydroPasswordField(
                            value = password,
                            onValueChange = { password = it },
                            label = stringResource(R.string.auth_password_label),
                            placeholder = stringResource(R.string.auth_password_hint),
                            imeAction = ImeAction.Done,
                            testTag = "auth_password_input",
                            onImeAction = {
                                focusManager.clearFocus()
                                if (phone.trim().isNotEmpty() && password.isNotEmpty()) {
                                    onSubmitCredentials(phone, password)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (phone.trim().isEmpty() || password.isEmpty()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showTypedSnackbar(
                                            credentialsRequiredMsg,
                                            SnackbarTone.WARNING
                                        )
                                    }
                                } else {
                                    onSubmitCredentials(phone, password)
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Color.White,
                                disabledContainerColor = accentColor.copy(alpha = 0.4f),
                                disabledContentColor = Color.White.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("auth_submit_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    text = ctaLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Support WhatsApp (Mot de passe oublié)
                        TextButton(
                            onClick = {
                                runCatching {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                                    context.startActivity(intent)
                                }.onFailure {
                                    coroutineScope.launch {
                                        snackbarHostState.showTypedSnackbar(
                                            "Support WhatsApp : 066 23 31 81",
                                            SnackbarTone.INFO
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("auth_whatsapp_support_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Chat,
                                    contentDescription = null,
                                    tint = StockEmeraldBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${stringResource(R.string.auth_forgot_password_prefix)} ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate400
                                )
                                Text(
                                    text = stringResource(R.string.auth_support_phone),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate200
                                )
                            }
                        }

                        HorizontalDivider(color = Slate800, modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            text = firstVisitPrompt,
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )

                        TextButton(
                            onClick = { onCreateNew(phone) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("auth_create_new_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AddCircleOutline,
                                contentDescription = null,
                                tint = createColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = createLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = createColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
