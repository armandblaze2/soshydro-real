package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.components.SosHydroSnackbarHost
import com.example.ui.components.SosHydroTextField
import com.example.ui.components.SosHydroTopBar
import com.example.ui.components.showTypedSnackbar
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GasAmber
import com.example.ui.theme.GasOrange
import com.example.ui.theme.SkyTelemetry
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
    onSubmitPhone: (String) -> Unit,
    onCreateNew: (String) -> Unit,
    onNavigateToForm: () -> Unit
) {
    val isStation = profileType == AuthProfileType.STATION
    val title = stringResource(if (isStation) R.string.auth_station_title else R.string.auth_detaillant_title)
    val prompt = stringResource(if (isStation) R.string.auth_station_prompt else R.string.auth_detaillant_prompt)
    val ctaLabel = stringResource(if (isStation) R.string.auth_station_cta else R.string.auth_detaillant_cta)
    val createLabel = stringResource(if (isStation) R.string.auth_station_first_visit else R.string.auth_detaillant_first_visit)
    val notFoundMessage = stringResource(if (isStation) R.string.auth_station_not_found else R.string.auth_detaillant_not_found)
    val phoneRequiredMsg = stringResource(R.string.auth_phone_required)

    val accentColor = if (isStation) ElectricBlue else GasOrange
    val iconTint = if (isStation) SkyTelemetry else GasAmber
    val profileIcon = if (isStation) Icons.Rounded.LocalGasStation else Icons.Rounded.LocalFireDepartment
    val createColor = if (isStation) StockEmeraldBright else GasAmber

    var phone by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val isLoading = authUiState is AuthUiState.Loading

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
                    snackbarHostState.showTypedSnackbar("Erreur réseau : ${event.message}", SnackbarTone.ERROR)
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
                    .padding(horizontal = 24.dp, vertical = 28.dp),
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = profileIcon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        SosHydroTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = stringResource(R.string.form_station_tel_label),
                            placeholder = stringResource(R.string.auth_phone_hint),
                            leadingIcon = Icons.Rounded.Phone,
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done,
                            isNumericMono = true,
                            testTag = "auth_phone_input",
                            onImeAction = {
                                focusManager.clearFocus()
                                if (phone.trim().isNotEmpty()) {
                                    onSubmitPhone(phone)
                                }
                            }
                        )

                        if (!lastSavedPhone.isNullOrBlank() && phone.trim() != lastSavedPhone) {
                            Spacer(modifier = Modifier.height(10.dp))
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

                        Spacer(modifier = Modifier.height(22.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (phone.trim().isEmpty()) {
                                    // Trigger inline snackbar feedback via coroutine? Handled cleanly:
                                } else {
                                    onSubmitPhone(phone)
                                }
                            },
                            enabled = !isLoading && phone.trim().isNotEmpty(),
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

                        if (phone.trim().isEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = phoneRequiredMsg,
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                TextButton(
                    onClick = { onCreateNew(phone) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("auth_create_new_button")
                ) {
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
