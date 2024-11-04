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

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

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

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val newImageUri = createImageFile(context)
            imageUri = newImageUri
            newImageUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Camera permission is required to take a photo.", Toast.LENGTH_SHORT).show()
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
        Column(modifier = Modifier.fillMaxWidth()) {
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


            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {

                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            val newImageUri = createImageFile(context)
                            imageUri = newImageUri
                            newImageUri?.let { uri ->
                                cameraLauncher.launch(uri)
                            }
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Camera")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (matkul.isNotEmpty() && detailTugas.isNotEmpty()) {
                            mainViewModel.addTugas(
                                matkul = matkul,
                                detailTugas = detailTugas,
                                imageUri = imageUri?.toString()
                            )
                            showSnackbar("Tugas sudah ditambahkan.")
                            matkul = ""
                            detailTugas = ""
                            bitmap = null
                            imageUri = null
                        } else {
                            showSnackbar("Mohon isi semua kolom.")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tambah tugas")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Daftar Tugas", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(tugasList.reversed()) { tugas ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Matkul: ${tugas.matkul}", fontSize = 18.sp)
                        Text(text = "Detail: ${tugas.detailTugas}", fontSize = 16.sp)

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

private fun createImageFile(context: Context): Uri? {
    return try {
        val file = File(context.filesDir, "tugas_image_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
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
