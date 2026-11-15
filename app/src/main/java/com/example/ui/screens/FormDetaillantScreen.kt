package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PropaneTank
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.DetaillantData
import com.example.data.model.GpsCaptureResult
import com.example.ui.components.GpsConfirmationDialog
import com.example.ui.components.GpsNoticeBanner
import com.example.ui.components.SnackbarTone
import com.example.ui.components.SosHydroSnackbarHost
import com.example.ui.components.SosHydroTextField
import com.example.ui.components.SosHydroTopBar
import com.example.ui.components.showTypedSnackbar
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedSoft
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GasAmber
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.Slate400
import com.example.ui.theme.StockEmerald
import com.example.ui.theme.StockEmeraldBright
import com.example.ui.viewmodel.FormEvent
import com.example.ui.viewmodel.FormSubmitState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun FormDetaillantScreen(
    detaillantData: DetaillantData?,
    prefillPhone: String,
    formWarningMessage: String?,
    onConsumeFormWarning: () -> Unit,
    formSubmitState: FormSubmitState,
    formEvents: SharedFlow<FormEvent>,
    hasLocationPermission: () -> Boolean,
    onBack: () -> Unit,
    onRequestGpsCapture: () -> Unit,
    onCancelGpsConfirmation: () -> Unit,
    onSubmitConfirmed: (
        detaillantId: String,
        nomBoutique: String,
        nomGerant: String,
        quartier: String,
        telephone: String,
        prix6kg: String,
        prix12kg: String,
        enStock: Boolean,
        gps: GpsCaptureResult
    ) -> Unit,
    onSuccessReturnHome: () -> Unit
) {
    val isEdit = detaillantData != null
    val initial = detaillantData ?: DetaillantData(telephone = prefillPhone)

    var nomBoutique by rememberSaveable { mutableStateOf(initial.nomBoutique) }
    var nomGerant by rememberSaveable { mutableStateOf(initial.nomGerant) }
    var quartier by rememberSaveable { mutableStateOf(initial.quartier) }
    var telephone by rememberSaveable { mutableStateOf(initial.telephone.ifBlank { prefillPhone }) }
    var prix6kg by rememberSaveable { mutableStateOf(initial.prix6kg) }
    var prix12kg by rememberSaveable { mutableStateOf(initial.prix12kg) }
    var enStock by rememberSaveable { mutableStateOf(initial.enStock) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val permissionDeniedMsg = stringResource(R.string.gps_permission_denied)
    val phoneValidationMsg = stringResource(R.string.validation_phone_empty)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            onRequestGpsCapture()
        } else {
            coroutineScope.launch {
                snackbarHostState.showTypedSnackbar(permissionDeniedMsg, SnackbarTone.ERROR)
            }
        }
    }

    LaunchedEffect(formWarningMessage) {
        if (!formWarningMessage.isNullOrBlank()) {
            snackbarHostState.showTypedSnackbar(formWarningMessage, SnackbarTone.WARNING)
            onConsumeFormWarning()
        }
    }

    LaunchedEffect(Unit) {
        formEvents.collectLatest { event ->
            when (event) {
                is FormEvent.SuccessAndReturnHome -> onSuccessReturnHome()
                is FormEvent.ShowError -> {
                    snackbarHostState.showTypedSnackbar(event.message, SnackbarTone.ERROR)
                }
            }
        }
    }

    if (formSubmitState is FormSubmitState.AwaitingGpsConfirmation) {
        GpsConfirmationDialog(
            gps = formSubmitState.gps,
            onDismiss = onCancelGpsConfirmation,
            onConfirm = {
                onSubmitConfirmed(
                    initial.id,
                    nomBoutique,
                    nomGerant,
                    quartier,
                    telephone,
                    prix6kg,
                    prix12kg,
                    enStock,
                    formSubmitState.gps
                )
            }
        )
    }

    val isBusy = formSubmitState is FormSubmitState.CapturingGps ||
        formSubmitState is FormSubmitState.Submitting

    Scaffold(
        containerColor = Slate950,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SosHydroSnackbarHost(snackbarHostState) },
        topBar = {
            SosHydroTopBar(
                title = stringResource(
                    if (isEdit) R.string.form_detaillant_title_edit else R.string.form_detaillant_title_new
                ),
                icon = Icons.Rounded.LocalFireDepartment,
                iconTint = GasAmber,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GpsNoticeBanner()

                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SosHydroTextField(
                            value = nomBoutique,
                            onValueChange = { nomBoutique = it },
                            label = stringResource(R.string.form_detaillant_boutique_label),
                            leadingIcon = Icons.Rounded.Storefront,
                            testTag = "detaillant_boutique_input"
                        )

                        SosHydroTextField(
                            value = nomGerant,
                            onValueChange = { nomGerant = it },
                            label = stringResource(R.string.form_detaillant_gerant_label),
                            leadingIcon = Icons.Rounded.Person,
                            testTag = "detaillant_gerant_input"
                        )

                        SosHydroTextField(
                            value = quartier,
                            onValueChange = { quartier = it },
                            label = stringResource(R.string.form_detaillant_quartier_label),
                            leadingIcon = Icons.Rounded.LocationCity,
                            testTag = "detaillant_quartier_input"
                        )

                        SosHydroTextField(
                            value = telephone,
                            onValueChange = { telephone = it },
                            label = stringResource(R.string.form_detaillant_tel_label),
                            leadingIcon = Icons.Rounded.Phone,
                            keyboardType = KeyboardType.Phone,
                            isNumericMono = true,
                            testTag = "detaillant_phone_input"
                        )
                    }
                }

                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Tarifs Bouteilles & Disponibilité",
                            style = MaterialTheme.typography.titleMedium,
                            color = GasAmber
                        )

                        SosHydroTextField(
                            value = prix6kg,
                            onValueChange = { next -> prix6kg = next.filter { it.isDigit() || it == '.' } },
                            label = stringResource(R.string.form_detaillant_prix6_label),
                            leadingIcon = Icons.Rounded.PropaneTank,
                            suffixText = stringResource(R.string.currency_fcfa_suffix),
                            keyboardType = KeyboardType.Number,
                            isNumericMono = true,
                            testTag = "detaillant_prix6_input"
                        )

                        SosHydroTextField(
                            value = prix12kg,
                            onValueChange = { next -> prix12kg = next.filter { it.isDigit() || it == '.' } },
                            label = stringResource(R.string.form_detaillant_prix12_label),
                            leadingIcon = Icons.Rounded.PropaneTank,
                            suffixText = stringResource(R.string.currency_fcfa_suffix),
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                            isNumericMono = true,
                            testTag = "detaillant_prix12_input"
                        )

                        HorizontalDivider(color = Slate800)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { enStock = !enStock }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.form_detaillant_stock_label),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(if (enStock) R.string.stock_available else R.string.stock_out),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (enStock) StockEmeraldBright else DangerRedSoft
                                )
                            }

                            Switch(
                                checked = enStock,
                                onCheckedChange = { enStock = it },
                                modifier = Modifier.testTag("detaillant_stock_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = StockEmerald,
                                    uncheckedThumbColor = Slate400,
                                    uncheckedTrackColor = Slate800
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (telephone.trim().isEmpty()) {
                            coroutineScope.launch {
                                snackbarHostState.showTypedSnackbar(phoneValidationMsg, SnackbarTone.WARNING)
                            }
                            return@Button
                        }
                        if (hasLocationPermission()) {
                            onRequestGpsCapture()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = Color.White,
                        disabledContainerColor = ElectricBlue.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("detaillant_submit_gps_button")
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (formSubmitState is FormSubmitState.CapturingGps) {
                                stringResource(R.string.gps_capturing_label)
                            } else {
                                "Envoi en cours…"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(
                                if (isEdit) R.string.form_detaillant_submit_edit
                                else R.string.form_detaillant_submit_new
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
