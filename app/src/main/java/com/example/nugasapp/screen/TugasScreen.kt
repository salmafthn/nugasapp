package com.example.nugasapp.screen

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nugasapp.data.local.TugasRepository
import com.example.nugasapp.data.viewmodel.MainViewModel
import com.example.nugasapp.data.viewmodel.MainViewModelFactory
import java.io.File

@Composable
fun TugasScreen(application: Application) {
    val tugasRepository = TugasRepository(application)
    val mainViewModel: MainViewModel = viewModel(factory = MainViewModelFactory(tugasRepository))

    var matkul by remember { mutableStateOf("") }
    var detailTugas by remember { mutableStateOf("") }
    val tugasList by mainViewModel.tugasList.observeAsState(emptyList())

    var snackbarVisible by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Camera and Image Preview Variables
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri?.let { uri ->
                bitmap = loadBitmapFromUri(context, uri)
            }
        } else {
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher for requesting camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imageUri?.let { uri -> cameraLauncher.launch(uri) }
        } else {
            Toast.makeText(context, "Camera permission is required to take a photo.", Toast.LENGTH_SHORT).show()
        }
    }

    // Image file creation
    val imageFile = remember {
        File(context.filesDir, "tugas_image.jpg").apply {
            imageUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)
        }
    }

    fun showSnackbar(message: String) {
        snackbarMessage = message
        snackbarVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Bagian untuk form tambah tugas dan tombol
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Tambah tugas", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = matkul,
                onValueChange = { matkul = it },
                label = { Text("Matkul") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = detailTugas,
                onValueChange = { detailTugas = it },
                label = { Text("Detail Tugas") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display image preview if an image is captured
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Row {
                Button(
                    onClick = {
                        // Check if permission is granted
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            // Launch the camera if permission is granted
                            imageUri?.let { uri -> cameraLauncher.launch(uri) }
                        } else {
                            // Request permission if not granted
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Camera")
                }

                Button(
                    onClick = {
                        if (matkul.isNotEmpty() && detailTugas.isNotEmpty() && imageUri != null) {
                            mainViewModel.addTugas(matkul, detailTugas, imageUri.toString())
                            showSnackbar("Tugas sudah ditambahkan.")
                            matkul = ""
                            detailTugas = ""
                            bitmap = null
                        } else {
                            showSnackbar("Mohon isi semua kolom dan ambil foto.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tambah tugas")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bagian untuk daftar tugas menggunakan LazyColumn
        Text(text = "Daftar Tugas", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (tugasList.isEmpty()) {
            Text(text = "Tidak ada tugas.", fontSize = 16.sp)
        } else {
            LazyColumn {
                items(tugasList) { tugas ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Matkul: ${tugas.matkul}", fontSize = 18.sp)
                            Text(text = "Detail: ${tugas.detailTugas}", fontSize = 16.sp)
                            // Display the image from Uri
                            tugas.imageUri?.let { uri ->
                                val tugasBitmap = loadBitmapFromUri(context, Uri.parse(uri))
                                tugasBitmap?.let { bmp ->
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (snackbarVisible) {
        Snackbar(
            action = {
                TextButton(onClick = { snackbarVisible = false }) {
                    Text("Tutup")
                }
            }
        ) {
            Text(text = snackbarMessage)
        }
    }
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
