package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AccueilScreen
import com.example.ui.screens.AuthProfileType
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FormDetaillantScreen
import com.example.ui.screens.FormStationScreen
import com.example.ui.viewmodel.FournisseurViewModel

@Composable
fun SosHydroNavHost(
    viewModel: FournisseurViewModel = viewModel()
) {
    val navController = rememberNavController()

    val homeBannerMessage by viewModel.homeBannerMessage.collectAsStateWithLifecycle()
    val authUiState by viewModel.authUiState.collectAsStateWithLifecycle()
    val lastStationPhone by viewModel.lastStationPhone.collectAsStateWithLifecycle()
    val lastDetaillantPhone by viewModel.lastDetaillantPhone.collectAsStateWithLifecycle()
    val activeStationData by viewModel.activeStationData.collectAsStateWithLifecycle()
    val activeDetaillantData by viewModel.activeDetaillantData.collectAsStateWithLifecycle()
    val prefillPhone by viewModel.prefillPhone.collectAsStateWithLifecycle()
    val formWarningMessage by viewModel.formWarningMessage.collectAsStateWithLifecycle()
    val formSubmitState by viewModel.formSubmitState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = SosHydroRoutes.ROUTE_ACCUEIL
    ) {
        composable(SosHydroRoutes.ROUTE_ACCUEIL) {
            AccueilScreen(
                homeBannerMessage = homeBannerMessage,
                onConsumeHomeBanner = viewModel::clearHomeBannerMessage,
                onStationClick = {
                    navController.navigate(SosHydroRoutes.ROUTE_AUTH_STATION)
                },
                onDetaillantClick = {
                    navController.navigate(SosHydroRoutes.ROUTE_AUTH_DETAILLANT)
                }
            )
        }

        composable(SosHydroRoutes.ROUTE_AUTH_STATION) {
            AuthScreen(
                profileType = AuthProfileType.STATION,
                authUiState = authUiState,
                lastSavedPhone = lastStationPhone,
                authEvents = viewModel.authEvents,
                onBack = { navController.popBackStack() },
                onSubmitCredentials = viewModel::verifyStationCredentials,
                onCreateNew = { enteredPhone ->
                    viewModel.startNewStationRegistration(enteredPhone)
                    navController.navigate(SosHydroRoutes.ROUTE_FORM_STATION) {
                        popUpTo(SosHydroRoutes.ROUTE_AUTH_STATION) { inclusive = true }
                    }
                },
                onNavigateToForm = {
                    navController.navigate(SosHydroRoutes.ROUTE_FORM_STATION) {
                        popUpTo(SosHydroRoutes.ROUTE_AUTH_STATION) { inclusive = true }
                    }
                }
            )
        }

        composable(SosHydroRoutes.ROUTE_AUTH_DETAILLANT) {
            AuthScreen(
                profileType = AuthProfileType.DETAILLANT,
                authUiState = authUiState,
                lastSavedPhone = lastDetaillantPhone,
                authEvents = viewModel.authEvents,
                onBack = { navController.popBackStack() },
                onSubmitCredentials = viewModel::verifyDetaillantCredentials,
                onCreateNew = { enteredPhone ->
                    viewModel.startNewDetaillantRegistration(enteredPhone)
                    navController.navigate(SosHydroRoutes.ROUTE_FORM_DETAILLANT) {
                        popUpTo(SosHydroRoutes.ROUTE_AUTH_DETAILLANT) { inclusive = true }
                    }
                },
                onNavigateToForm = {
                    navController.navigate(SosHydroRoutes.ROUTE_FORM_DETAILLANT) {
                        popUpTo(SosHydroRoutes.ROUTE_AUTH_DETAILLANT) { inclusive = true }
                    }
                }
            )
        }

        composable(SosHydroRoutes.ROUTE_FORM_STATION) {
            FormStationScreen(
                stationData = activeStationData,
                prefillPhone = prefillPhone,
                formWarningMessage = formWarningMessage,
                onConsumeFormWarning = viewModel::clearFormWarningMessage,
                formSubmitState = formSubmitState,
                formEvents = viewModel.formEvents,
                hasLocationPermission = viewModel.gpsClient::hasLocationPermission,
                onBack = { navController.popBackStack() },
                onRequestGpsCapture = viewModel::captureGpsForConfirmation,
                onCancelGpsConfirmation = viewModel::cancelGpsConfirmation,
                onSubmit = viewModel::submitStationUpdate,
                onSuccessReturnHome = {
                    navController.popBackStack(SosHydroRoutes.ROUTE_ACCUEIL, inclusive = false)
                }
            )
        }

        composable(SosHydroRoutes.ROUTE_FORM_DETAILLANT) {
            FormDetaillantScreen(
                detaillantData = activeDetaillantData,
                prefillPhone = prefillPhone,
                formWarningMessage = formWarningMessage,
                onConsumeFormWarning = viewModel::clearFormWarningMessage,
                formSubmitState = formSubmitState,
                formEvents = viewModel.formEvents,
                hasLocationPermission = viewModel.gpsClient::hasLocationPermission,
                onBack = { navController.popBackStack() },
                onRequestGpsCapture = viewModel::captureGpsForConfirmation,
                onCancelGpsConfirmation = viewModel::cancelGpsConfirmation,
                onSubmit = viewModel::submitDetaillantUpdate,
                onSuccessReturnHome = {
                    navController.popBackStack(SosHydroRoutes.ROUTE_ACCUEIL, inclusive = false)
                }
            )
        }
    }
}
