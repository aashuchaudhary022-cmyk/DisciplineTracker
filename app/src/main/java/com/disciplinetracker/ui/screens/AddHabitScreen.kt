package com.disciplinetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.disciplinetracker.viewmodel.AddHabitViewModel

@Composable
fun AddHabitScreen(
    viewModel: AddHabitViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Habit name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.targetPerWeek,
            onValueChange = viewModel::onTargetPerWeekChange,
            label = { Text("Target per week") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = viewModel::saveHabit) {
            Text("Save Habit")
        }

        state.saveMessage?.let { Text(it) }
    }
}
