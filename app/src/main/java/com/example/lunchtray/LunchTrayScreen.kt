/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lunchtray

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lunchtray.datasource.DataSource.accompanimentMenuItems
import com.example.lunchtray.datasource.DataSource.entreeMenuItems
import com.example.lunchtray.datasource.DataSource.sideDishMenuItems
import com.example.lunchtray.model.OrderUiState
import com.example.lunchtray.ui.AccompanimentMenuScreen
import com.example.lunchtray.ui.BaseMenuScreen
import com.example.lunchtray.ui.CheckoutScreen
import com.example.lunchtray.ui.OrderViewModel
import com.example.lunchtray.ui.SideDishMenuPreview
import com.example.lunchtray.ui.SideDishMenuScreen
import com.example.lunchtray.ui.StartOrderScreen

// TODO: Screen enum
enum class LaunchTrayScreen(@StringRes val title: Int){
    Start(title = R.string.app_name),
    Entree(title = R.string.choose_entree),
    SideDish(title = R.string.choose_side_dish),
    Accompaniment(title = R.string.choose_accompaniment),
    Checkout(title = R.string.order_checkout)
}

@Composable
fun LaunchTrayAppBar(
    currentScreen: String,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(currentScreen) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}


@Composable
fun LunchTrayApp(
    modifier: Modifier = Modifier,
    viewModel: OrderViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    // TODO: Create Controller and initialization
    val backStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        topBar = {
            LaunchTrayAppBar(
                currentScreen = backStackEntry?.destination?.route ?: LaunchTrayScreen.Start.name,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = LaunchTrayScreen.Start.name,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(route = LaunchTrayScreen.Start.name) {
                StartOrderScreen(
                    onStartOrderButtonClicked = {
                        navController.navigate(LaunchTrayScreen.Entree.name)
                    },

                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_medium))
                        .fillMaxSize()
                )
            }

            composable(route = LaunchTrayScreen.Entree.name) {
                BaseMenuScreen(
                    options =  entreeMenuItems,

                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(
                            viewModel =  viewModel,
                            navController = navController
                        )
                    },

                    onNextButtonClicked = {
                        navController.navigate(LaunchTrayScreen.SideDish.name)
                    },

                    onSelectionChanged = {
                        viewModel.updateEntree(it)
                    },

                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_medium))
                        .verticalScroll(rememberScrollState())
                )
            }

            composable(route = LaunchTrayScreen.SideDish.name) {
                SideDishMenuScreen(
                    options = sideDishMenuItems,

                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(
                            viewModel =  viewModel,
                            navController = navController
                        )
                    },

                    onNextButtonClicked = {
                        navController.navigate(LaunchTrayScreen.Accompaniment.name)
                    },

                    onSelectionChanged = {
                        viewModel.updateSideDish(it)
                    },

                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_medium))
                        .verticalScroll(rememberScrollState())
                )
            }

            composable(route = LaunchTrayScreen.Accompaniment.name) {
                AccompanimentMenuScreen(
                    options = accompanimentMenuItems,

                    onNextButtonClicked = {
                        navController.navigate(LaunchTrayScreen.Checkout.name)
                    },

                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(
                            viewModel =  viewModel,
                            navController = navController
                        )
                    },

                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_medium))
                        .verticalScroll(rememberScrollState()),

                    onSelectionChanged = {
                        viewModel.updateAccompaniment(it)
                    }
                )
            }

            composable(route = LaunchTrayScreen.Checkout.name) {
                CheckoutScreen(
                    orderUiState = uiState,

                    onNextButtonClicked = {
                        navController.navigate(LaunchTrayScreen.Checkout.name)
                    },

                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(
                            viewModel =  viewModel,
                            navController = navController
                        )
                    },

                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_medium))
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

/**Private functions+++++++++++++*/
private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    viewModel.resetOrder()
    navController.popBackStack(LaunchTrayScreen.Start.name, inclusive = false)
}

