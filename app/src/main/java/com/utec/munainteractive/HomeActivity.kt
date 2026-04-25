@file:OptIn(ExperimentalMaterial3Api::class)

package com.utec.munainteractive

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.utec.munainteractive.ui.theme.MUNAInteractiveTheme
import com.utec.munainteractive.data.model.MuseumObject
import com.utec.munainteractive.ui.viewmodel.MuseumViewModel
import com.utec.munainteractive.device.BleScanner

// Datos para las noticias locales
data class NewsItem(val title: String, val description: String, val date: String)

class HomeActivity : ComponentActivity() {

    private var bleScanner: BleScanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userName = intent.getStringExtra("USER_NAME") ?: "Invitado"
        enableEdgeToEdge()

        val newsList = listOf(
            NewsItem("Nueva Exposición: Mayas", "Descubre los secretos de la civilización maya.", "25 Mar"),
            NewsItem("Evento Nocturno", "Recorrido con linternas este viernes a las 7 PM.", "27 Mar"),
            NewsItem("Taller de Cerámica", "Aprende técnicas ancestrales de modelado.", "30 Mar")
        )

        setContent {
            MUNAInteractiveTheme(dynamicColor = false) {
                val viewModel: MuseumViewModel = viewModel()
                val sheetState = rememberModalBottomSheetState()

                // Gestión de Permisos (Necesario para que el RSSI y BLE funcionen)
                val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION // <-- Añadido aquí
                    )
                } else {
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION // <-- Añadido aquí también
                    )
                }

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val allGranted = permissions.values.all { it }
                    if (allGranted) {
                        startBleScanning(viewModel)
                    }
                }

                LaunchedEffect(Unit) {
                    launcher.launch(permissionsToRequest)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("MUNA Interactive", style = MaterialTheme.typography.titleLarge) }
                        )
                    }
                ) { innerPadding ->
                    HomeScreen(
                        userName = userName,
                        news = newsList,
                        modifier = Modifier.padding(innerPadding)
                    )

                    // BottomSheet que se levanta automáticamente al detectar proximidad
                    if (viewModel.showSheet && viewModel.detectedObject != null) {
                        ModalBottomSheet(
                            onDismissRequest = { viewModel.dismissSheet() },
                            sheetState = sheetState
                        ) {
                            ObjectInfoContent(
                                museumObject = viewModel.detectedObject!!,
                                onClose = { viewModel.dismissSheet() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun startBleScanning(viewModel: MuseumViewModel) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

        if (adapter != null && adapter.isEnabled) {
            bleScanner = BleScanner(adapter) { uid ->
                viewModel.onBeaconDetected(uid)
            }
            bleScanner?.startScanning()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleScanner?.stopScanning()
    }
}

@Composable
fun HomeScreen(userName: String, news: List<NewsItem>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text("¡Bienvenido!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.secondary)
                Text(userName, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))
                Text("Noticias y Eventos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
        items(news) { item -> NewsCard(item) }
    }
}

@Composable
fun NewsCard(newsItem: NewsItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(newsItem.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(newsItem.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(newsItem.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ObjectInfoContent(museumObject: MuseumObject, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cerca de ti", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = null) }
        }

        Text(museumObject.nombre, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)

        if (museumObject.artista.isNotEmpty()) {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                Text(museumObject.artista, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AsyncImage(
            model = museumObject.url_src,
            contentDescription = museumObject.nombre,
            modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(museumObject.descripcion, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Justify)

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { /* Audioguía */ }, modifier = Modifier.fillMaxWidth()) { Text("Reproducir Audioguía") }
    }
}