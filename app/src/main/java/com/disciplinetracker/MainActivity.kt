package com.disciplinetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.disciplinetracker.navigation.AppNavGraph
import com.disciplinetracker.ui.theme.DisciplineTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DisciplineTrackerTheme {
                AppNavGraph()
            }
        }
    }
}
