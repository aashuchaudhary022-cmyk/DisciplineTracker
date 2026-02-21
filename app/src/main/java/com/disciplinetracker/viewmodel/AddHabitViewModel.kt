package com.disciplinetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.disciplinetracker.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddHabitUiState(
    val name: String = "",
    val description: String = "",
    val targetPerWeek: String = "3",
    val saveMessage: String? = null
)

class AddHabitViewModel(
    private val repository: HabitRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddHabitUiState())
    val uiState: StateFlow<AddHabitUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, saveMessage = null) }

    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value, saveMessage = null) }

    fun onTargetPerWeekChange(value: String) {
        if (value.all { it.isDigit() } || value.isEmpty()) {
            _uiState.update { it.copy(targetPerWeek = value, saveMessage = null) }
        }
    }

    fun saveHabit() {
        viewModelScope.launch {
            val state = _uiState.value
            val target = state.targetPerWeek.toIntOrNull() ?: 1
            if (state.name.isBlank()) {
                _uiState.update { it.copy(saveMessage = "Habit name is required") }
                return@launch
            }

            repository.addHabit(
                name = state.name,
                description = state.description,
                targetPerWeek = target
            )

            _uiState.value = AddHabitUiState(saveMessage = "Habit saved")
        }
    }

    class Factory(private val repository: HabitRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddHabitViewModel(repository) as T
        }
    }
}
