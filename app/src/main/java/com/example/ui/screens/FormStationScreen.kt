package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.OilBarrel
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PropaneTank
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.STATION_GROUPS
import com.example.data.model.StationData
import com.example.ui.components.GpsConfirmationDialog
import com.example.ui.components.GpsNoticeBanner
import com.example.ui.components.SnackbarTone
import com.example.ui.components.SosHydroPasswordField
import com.example.ui.components.SosHydroSnackbarHost
import com.example.ui.components.SosHydroTextField
import com.example.ui.components.SosHydroTopBar
import com.example.ui.components.showTypedSnackbar
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedSoft
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GasAmber
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SkyTelemetry
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.StockEmerald
import com.example.ui.theme.StockEmeraldBright
import com.example.ui.viewmodel.FormEvent
import com.example.ui.viewmodel.FormSubmitState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormStationScreen(
    stationData: StationData?,
    prefillPhone: String,
    formWarningMessage: String?,
    onConsumeFormWarning: () -> Unit,
    formSubmitState: FormSubmitState,
    formEvents: SharedFlow<FormEvent>,
    hasLocationPermission: () -> Boolean,
    onBack: () -> Unit,
    onRequestGpsCapture: () -> Unit,
    onCancelGpsConfirmation: () -> Unit,
    onSubmit: (
        stationId: String,
        nomStation: String,
        lieuDit: String,
        telephone: String,
        motDePasse: String,
        estOuvert: Boolean,
        prixEssence: String,
        dispoEssence: Boolean,
        prixGazoil: String,
        dispoGazoil: Boolean,
        prixPetrole: String,
        dispoPetrole: Boolean,
        prixGaz12kg: String,
        dispoGaz12kg: Boolean,
        prixGaz35kg: String,
        dispoGaz35kg: Boolean,
        latitude: String,
        longitude: String
    ) -> Unit,
    onSuccessReturnHome: () -> Unit
) {
    val isNewStation = stationData == null
    val initial = stationData ?: StationData(telephone = prefillPhone)

    var nomStation by rememberSaveable { mutableStateOf(initial.nomStation) }
    var lieuDit by rememberSaveable { mutableStateOf(initial.lieuDit) }
    var telephone by rememberSaveable { mutableStateOf(initial.telephone.ifBlank { prefillPhone }) }
    var motDePasse by rememberSaveable { mutableStateOf("") }
    var confirmerMotDePasse by rememberSaveable { mutableStateOf("") }
    var estOuvert by rememberSaveable { mutableStateOf(initial.estOuvert) }

    var prixEssence by rememberSaveable { mutableStateOf(initial.prixEssence) }
    var dispoEssence by rememberSaveable { mutableStateOf(initial.dispoEssence) }

    var prixGazoil by rememberSaveable { mutableStateOf(initial.prixGazoil) }
    var dispoGazoil by rememberSaveable { mutableStateOf(initial.dispoGazoil) }

    var prixPetrole by rememberSaveable { mutableStateOf(initial.prixPetrole) }
    var dispoPetrole by rememberSaveable { mutableStateOf(initial.dispoPetrole) }

    var prixGaz12 by rememberSaveable { mutableStateOf(initial.prixGaz12kg) }
    var dispoGaz12 by rememberSaveable { mutableStateOf(initial.dispoGaz12kg) }

    var prixGaz35 by rememberSaveable { mutableStateOf(initial.prixGaz35kg) }
    var dispoGaz35 by rememberSaveable { mutableStateOf(initial.dispoGaz35kg) }

    var dropdownExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val permissionDeniedMsg = stringResource(R.string.gps_permission_denied)
    val phoneValidationMsg = stringResource(R.string.validation_phone_empty)
    val passwordRequiredMsg = stringResource(R.string.validation_password_required)
    val passwordMismatchMsg = stringResource(R.string.validation_password_mismatch)

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
                onSubmit(
                    initial.id,
                    nomStation,
                    lieuDit,
                    telephone,
                    motDePasse,
                    estOuvert,
                    prixEssence,
                    dispoEssence,
                    prixGazoil,
                    dispoGazoil,
                    prixPetrole,
                    dispoPetrole,
                    prixGaz12,
                    dispoGaz12,
                    prixGaz35,
                    dispoGaz35,
                    formSubmitState.gps.latitude.toString(),
                    formSubmitState.gps.longitude.toString()
                )
            }
        )
    }

    val isBusy = formSubmitState is FormSubmitState.CapturingGps ||
        formSubmitState is FormSubmitState.Submitting

    val headerTitle = if (!isNewStation) {
        if (initial.lieuDit.isNotBlank()) "${initial.nomStation} (${initial.lieuDit})" else initial.nomStation
    } else {
        stringResource(R.string.form_station_title_new)
    }

    Scaffold(
        containerColor = Slate950,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SosHydroSnackbarHost(snackbarHostState) },
        topBar = {
            SosHydroTopBar(
                title = headerTitle,
                icon = Icons.Rounded.LocalGasStation,
                iconTint = SkyTelemetry,
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
                // Status mode pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNewStation) "Configuration initiale" else "Tableau de bord Station",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Surface(
                        color = if (isNewStation) {
                            StockEmerald.copy(alpha = 0.14f)
                        } else {
                            ElectricBlue.copy(alpha = 0.16f)
                        },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            1.dp,
                            if (isNewStation) StockEmerald.copy(alpha = 0.4f) else ElectricBlue.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = stringResource(
                                if (isNewStation) R.string.form_badge_new_gps else R.string.form_badge_edit_stock
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isNewStation) StockEmeraldBright else SkyTelemetry,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // GPS Banner shown ONLY on initial creation
                if (isNewStation) {
                    GpsNoticeBanner(text = stringResource(R.string.form_gps_notice_station))
                }

                // Identification, password & status card
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
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = nomStation,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.form_station_group_label)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Business,
                                        contentDescription = null,
                                        tint = SkyTelemetry
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate850,
                                    unfocusedContainerColor = Slate800,
                                    focusedBorderColor = ElectricBlue,
                                    unfocusedBorderColor = Slate700,
                                    focusedLabelColor = SkyTelemetry,
                                    unfocusedLabelColor = Slate400,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag("station_group_dropdown")
                            )

                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                containerColor = Slate800
                            ) {
                                STATION_GROUPS.forEach { group ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = group,
                                                color = if (group == nomStation) SkyTelemetry else Color.White,
                                                fontWeight = if (group == nomStation) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            nomStation = group
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        SosHydroTextField(
                            value = lieuDit,
                            onValueChange = { lieuDit = it },
                            label = stringResource(R.string.form_station_lieudit_label),
                            leadingIcon = Icons.Rounded.LocationOn,
                            imeAction = ImeAction.Next,
                            testTag = "station_lieudit_input"
                        )

                        SosHydroTextField(
                            value = telephone,
                            onValueChange = { telephone = it },
                            label = stringResource(R.string.form_station_tel_label),
                            leadingIcon = Icons.Rounded.Phone,
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next,
                            isNumericMono = true,
                            testTag = "station_phone_input"
                        )

                        // Double password matching block
                        Surface(
                            color = Slate950.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Slate700),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SosHydroPasswordField(
                                    value = motDePasse,
                                    onValueChange = { motDePasse = it },
                                    label = stringResource(
                                        if (isNewStation) R.string.form_password_new
                                        else R.string.form_password_optional
                                    ),
                                    imeAction = ImeAction.Next,
                                    testTag = "station_password_input"
                                )
                                SosHydroPasswordField(
                                    value = confirmerMotDePasse,
                                    onValueChange = { confirmerMotDePasse = it },
                                    label = stringResource(R.string.form_password_confirm),
                                    imeAction = ImeAction.Next,
                                    testTag = "station_password_confirm_input"
                                )
                            }
                        }

                        HorizontalDivider(color = Slate800)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { estOuvert = !estOuvert }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.form_station_open_label),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Surface(
                                    color = if (estOuvert) {
                                        StockEmerald.copy(alpha = 0.16f)
                                    } else {
                                        DangerRed.copy(alpha = 0.16f)
                                    },
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = stringResource(
                                            if (estOuvert) R.string.form_station_open_badge
                                            else R.string.form_station_closed_badge
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (estOuvert) StockEmeraldBright else DangerRedSoft,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Switch(
                                checked = estOuvert,
                                onCheckedChange = { estOuvert = it },
                                modifier = Modifier.testTag("station_open_switch"),
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

                // Fuel & Gas Inventory Card
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
                            text = stringResource(R.string.form_station_products_header),
                            style = MaterialTheme.typography.titleMedium,
                            color = SkyTelemetry
                        )

                        FuelProductRow(
                            label = stringResource(R.string.product_essence),
                            icon = Icons.Rounded.LocalGasStation,
                            iconTint = SkyTelemetry,
                            price = prixEssence,
                            onPriceChange = { prixEssence = it },
                            inStock = dispoEssence,
                            onStockToggle = { dispoEssence = it },
                            priceTag = "station_price_essence",
                            stockTag = "station_stock_essence"
                        )
                        HorizontalDivider(color = Slate800)

                        FuelProductRow(
                            label = stringResource(R.string.product_gazoil),
                            icon = Icons.Rounded.OilBarrel,
                            iconTint = WarningAmberColor,
                            price = prixGazoil,
                            onPriceChange = { prixGazoil = it },
                            inStock = dispoGazoil,
                            onStockToggle = { dispoGazoil = it },
                            priceTag = "station_price_gazoil",
                            stockTag = "station_stock_gazoil"
                        )
                        HorizontalDivider(color = Slate800)

                        FuelProductRow(
                            label = stringResource(R.string.product_petrole),
                            icon = Icons.Rounded.OilBarrel,
                            iconTint = Slate400,
                            price = prixPetrole,
                            onPriceChange = { prixPetrole = it },
                            inStock = dispoPetrole,
                            onStockToggle = { dispoPetrole = it },
                            priceTag = "station_price_petrole",
                            stockTag = "station_stock_petrole"
                        )
                        HorizontalDivider(color = Slate800)

                        FuelProductRow(
                            label = stringResource(R.string.product_gaz_12),
                            icon = Icons.Rounded.LocalFireDepartment,
                            iconTint = GasAmber,
                            price = prixGaz12,
                            onPriceChange = { prixGaz12 = it },
                            inStock = dispoGaz12,
                            onStockToggle = { dispoGaz12 = it },
                            priceTag = "station_price_gaz12",
                            stockTag = "station_stock_gaz12"
                        )
                        HorizontalDivider(color = Slate800)

                        FuelProductRow(
                            label = stringResource(R.string.product_gaz_35),
                            icon = Icons.Rounded.PropaneTank,
                            iconTint = GasAmber,
                            price = prixGaz35,
                            onPriceChange = { prixGaz35 = it },
                            inStock = dispoGaz35,
                            onStockToggle = { dispoGaz35 = it },
                            priceTag = "station_price_gaz35",
                            stockTag = "station_stock_gaz35"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Dynamic CTA: GPS Capture on Creation vs Direct Submit on Modification
                val ctaContainerColor = if (isNewStation) StockEmerald else ElectricBlue
                val ctaContentColor = if (isNewStation) Color(0xFF04261B) else Color.White

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (telephone.trim().isEmpty()) {
                            coroutineScope.launch {
                                snackbarHostState.showTypedSnackbar(phoneValidationMsg, SnackbarTone.WARNING)
                            }
                            return@Button
                        }
                        if (isNewStation && motDePasse.trim().isEmpty()) {
                            coroutineScope.launch {
                                snackbarHostState.showTypedSnackbar(passwordRequiredMsg, SnackbarTone.WARNING)
                            }
                            return@Button
                        }
                        if ((motDePasse.isNotEmpty() || confirmerMotDePasse.isNotEmpty()) &&
                            motDePasse != confirmerMotDePasse
                        ) {
                            coroutineScope.launch {
                                snackbarHostState.showTypedSnackbar(passwordMismatchMsg, SnackbarTone.ERROR)
                            }
                            return@Button
                        }

                        if (isNewStation) {
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
                        } else {
                            // Direct submission without GPS recapture on existing station update
                            onSubmit(
                                initial.id,
                                nomStation,
                                lieuDit,
                                telephone,
                                motDePasse,
                                estOuvert,
                                prixEssence,
                                dispoEssence,
                                prixGazoil,
                                dispoGazoil,
                                prixPetrole,
                                dispoPetrole,
                                prixGaz12,
                                dispoGaz12,
                                prixGaz35,
                                dispoGaz35,
                                initial.latitude,
                                initial.longitude
                            )
                        }
                    },
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ctaContainerColor,
                        contentColor = ctaContentColor,
                        disabledContainerColor = ctaContainerColor.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("station_submit_gps_button")
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            color = ctaContentColor,
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
                            imageVector = if (isNewStation) Icons.Rounded.MyLocation else Icons.Rounded.Save,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(
                                if (isNewStation) R.string.form_station_submit_new
                                else R.string.form_station_submit_edit
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

private val WarningAmberColor = Color(0xFFF59E0B)

@Composable
private fun FuelProductRow(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    price: String,
    onPriceChange: (String) -> Unit,
    inStock: Boolean,
    onStockToggle: (Boolean) -> Unit,
    priceTag: String,
    stockTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.weight(1.1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        OutlinedTextField(
            value = price,
            onValueChange = { next -> onPriceChange(next.filter { it.isDigit() || it == '.' }) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            suffix = {
                Text(
                    text = stringResource(R.string.currency_fcfa_suffix),
                    fontFamily = JetBrainsMonoFontFamily,
                    color = SkyTelemetry,
                    fontWeight = FontWeight.Bold
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Slate850,
                unfocusedContainerColor = Slate800,
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = Slate700,
                cursorColor = SkyTelemetry
            ),
            modifier = Modifier
                .weight(0.95f)
                .testTag(priceTag)
        )

        Surface(
            onClick = { onStockToggle(!inStock) },
            color = if (inStock) {
                StockEmerald.copy(alpha = 0.16f)
            } else {
                DangerRed.copy(alpha = 0.14f)
            },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                if (inStock) StockEmerald.copy(alpha = 0.45f) else DangerRed.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag(stockTag)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = if (inStock) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (inStock) StockEmeraldBright else DangerRedSoft,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(if (inStock) R.string.stock_available else R.string.stock_out),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (inStock) StockEmeraldBright else DangerRedSoft
                )
            }
        }
    }
}
