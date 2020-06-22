package com.wreckingball.design.ui.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wreckingball.design.models.Sign
import com.wreckingball.design.repositories.SignRepository

class DashboardViewModel(private val signRepository: SignRepository) : ViewModel() {
    val signs: MutableLiveData<List<Sign>> = signRepository.signs
}