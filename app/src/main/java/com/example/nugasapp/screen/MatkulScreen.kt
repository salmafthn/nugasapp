package com.example.nugasapp.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.nugasapp.data.network.entity.Matkul
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatkulScreen(
    auth: FirebaseAuth,
    navController: NavHostController) {
    val db = Firebase.firestore
    var jadwal by remember { mutableStateOf<List<Matkul>>(emptyList()) }

    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {

                jadwal.forEach { matkul ->
                    Text(text = "Hari: ${matkul.Hari}")
                    Text(text = "Mata Kuliah: ${matkul.MataKuliah}")
                    Text(text = "Praktikum: ${if (matkul.Praktikum) "Yes" else "No"}")
                    Text(text = "Ruang: ${matkul.Ruang}")
                    Text(text = "Waktu: ${matkul.Waktu}")
                    Divider()
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        val result = db.collection("Matkul").get().await()
        jadwal = result.documents.map { doc ->
            Matkul(
                Hari = doc.getString("Hari") ?: "",
                MataKuliah = doc.getString("MataKuliah") ?: "",
                Praktikum = doc.getBoolean("Praktikum") ?: false,
                Ruang = doc.getString("Ruang") ?: "",
                Waktu = doc.getString("Waktu") ?: ""
            )
        }
    }
}