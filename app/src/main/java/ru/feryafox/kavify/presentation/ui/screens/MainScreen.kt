package ru.feryafox.kavify.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import ru.feryafox.kavify.data.repositories.preferences.PreferencesManagerFactory
import ru.feryafox.kavify.presentation.ui.Routes
import ru.feryafox.kavify.presentation.viewmodels.MainScreenViewModel
import ru.feryafox.kavify.root.KAVIFY_ID


@Composable
fun MainListScreen(
    navController: NavHostController,
    viewModel: MainScreenViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val prefsManager = remember {
        PreferencesManagerFactory.create(KAVIFY_ID)
    }
    var selectedItemId by remember {
        mutableStateOf(prefsManager.getString("last_screen", default = ""))
    }

    var selectedContent by remember {
        mutableStateOf<@Composable () -> Unit>({
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(64.dp)
                            .padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Выберите пункт меню",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        })
    }

    LaunchedEffect(selectedItemId, viewModel.categories) {
        if (selectedItemId.isNotBlank()) {
            viewModel.categories.forEach { category ->
                category.items.find { it.id == selectedItemId }?.let { foundItem ->
                    selectedContent = foundItem.content
                    return@LaunchedEffect
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                LazyColumn(modifier = Modifier.weight(1f, fill = true)) {
                    viewModel.categories.forEach { category ->
                        item {
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(category.items) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedContent = item.content
                                        selectedItemId = item.id
                                        prefsManager.setString("last_screen", item.id)
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(Routes.SETTING.path)
                            coroutineScope.launch { drawerState.close() }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Настройки",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Настройки",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) {
        Box(
            Modifier
                .fillMaxSize()
        ) {
            selectedContent()
        }
    }
}
