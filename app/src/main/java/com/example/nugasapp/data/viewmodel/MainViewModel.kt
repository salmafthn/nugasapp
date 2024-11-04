package com.example.nugasapp.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nugasapp.data.local.Tugas
import com.example.nugasapp.data.local.TugasRepository
import com.example.nugasapp.data.network.entity.Github
import com.example.nugasapp.data.network.entity.ProfileRepository
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(private val tugasRepository: TugasRepository) : ViewModel() {
    private val profileRepository = ProfileRepository()

    private val _user = MutableStateFlow<Github?>(null)
    val user: StateFlow<Github?> get() = _user
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _tugasList = tugasRepository.getAllTugas()
    val tugasList: LiveData<List<Tugas>> get() = _tugasList

    init {
        fetchAllTugas()
    }

    private fun fetchAllTugas() {
        viewModelScope.launch {
            tugasRepository.getAllTugas()
        }
    }

    fun addTugas(matkul: String, detailTugas: String, imageUri: String?) {
        val newTugas = Tugas(matkul = matkul, detailTugas = detailTugas, selesai = false, imageUri = imageUri)
        viewModelScope.launch {
            tugasRepository.insert(newTugas)
        }
    }


    fun getGithubProfile(user: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = profileRepository.getProfile(user)
                _user.value = profile
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
                _user.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}