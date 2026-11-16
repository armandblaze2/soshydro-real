package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.SnackbarTone
import com.example.ui.components.SosHydroSnackbarHost
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

@Composable
fun AccueilScreen(
    homeBannerMessage: String?,
    onConsumeHomeBanner: () -> Unit,
    onStationClick: () -> Unit,
    onDetaillantClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(homeBannerMessage) {
        if (!homeBannerMessage.isNullOrBlank()) {
            snackbarHostState.showTypedSnackbar(homeBannerMessage, SnackbarTone.SUCCESS)
            onConsumeHomeBanner()
        }
    }

    Scaffold(
        containerColor = Slate950,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SosHydroSnackbarHost(snackbarHostState) },
        topBar = {
            SosHydroTopBar(
                title = stringResource(R.string.accueil_topbar_title),
                icon = Icons.Rounded.Storefront,
                iconTint = SkyTelemetry
            )
        },
        bottomBar = {
            Surface(
                color = Slate900,
                tonalElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.accueil_footer_copyright),
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ElectricBlue.copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.18f),
                            radius = size.minDimension * 0.65f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GasOrange.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.85f, size.height * 0.65f),
                            radius = size.minDimension * 0.45f
                        )
                    )
                }
        ) {
            val isWide = maxWidth >= 600.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier.widthIn(max = if (isWide) 680.dp else 520.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Official SOS-HYDRO logo container
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    SkyTelemetry.copy(alpha = 0.45f),
                                    Slate700,
                                    GasOrange.copy(alpha = 0.35f)
                                )
                            )
                        ),
                        shadowElevation = 10.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_logotraffic),
                                contentDescription = stringResource(R.string.cd_logo_soshydro),
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(104.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.accueil_hero_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.accueil_hero_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ProfileSelectorCard(
                                title = stringResource(R.string.accueil_btn_station_title),
                                subtitle = stringResource(R.string.accueil_btn_station_desc),
                                icon = Icons.Rounded.LocalGasStation,
                                accentColor = ElectricBlue,
                                iconTint = SkyTelemetry,
                                isPrimaryHighlight = true,
                                testTag = "btn_profile_station",
                                onClick = onStationClick,
                                modifier = Modifier.weight(1f)
                            )
                            ProfileSelectorCard(
                                title = stringResource(R.string.accueil_btn_detaillant_title),
                                subtitle = stringResource(R.string.accueil_btn_detaillant_desc),
                                icon = Icons.Rounded.LocalFireDepartment,
                                accentColor = GasOrange,
                                iconTint = GasAmber,
                                isPrimaryHighlight = false,
                                testTag = "btn_profile_detaillant",
                                onClick = onDetaillantClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        ProfileSelectorCard(
                            title = stringResource(R.string.accueil_btn_station_title),
                            subtitle = stringResource(R.string.accueil_btn_station_desc),
                            icon = Icons.Rounded.LocalGasStation,
                            accentColor = ElectricBlue,
                            iconTint = SkyTelemetry,
                            isPrimaryHighlight = true,
                            testTag = "btn_profile_station",
                            onClick = onStationClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileSelectorCard(
                            title = stringResource(R.string.accueil_btn_detaillant_title),
                            subtitle = stringResource(R.string.accueil_btn_detaillant_desc),
                            icon = Icons.Rounded.LocalFireDepartment,
                            accentColor = GasOrange,
                            iconTint = GasAmber,
                            isPrimaryHighlight = false,
                            testTag = "btn_profile_detaillant",
                            onClick = onDetaillantClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSelectorCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    iconTint: Color,
    isPrimaryHighlight: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isPrimaryHighlight) {
        ElectricBlue.copy(alpha = 0.16f)
    } else {
        Slate800
    }
    val borderColor = if (isPrimaryHighlight) {
        ElectricBlue.copy(alpha = 0.55f)
    } else {
        Slate700
    }

    Card(
        onClick = onClick,
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate200.copy(alpha = 0.72f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.07f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
