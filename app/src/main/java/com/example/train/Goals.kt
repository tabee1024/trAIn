package com.example.train

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.train.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun Goals() {
    var goals by remember {
        mutableStateOf(
            listOf(
                Goal(1, "Do 10 push ups without stopping", 0.45f),
                Goal(2, "Workout 4 times in a week", 0.75f),
                Goal(3, "Lose 1 kg", 0.20f)
            )
        )
    }
    var editingGoal by remember { mutableStateOf<Goal?>(null) }
    var deletingGoal by remember { mutableStateOf<Goal?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(90.dp))

        Text(
            text = "Your Goals", color = StoneGrey,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Motivational Banner
        MotivationalSection()

        // Goals List
        GoalsList(
            goals = goals,
            onEdit = { editingGoal = it },
            onDelete = { deletingGoal = it }
        )
    }

    // Edit Dialog
    editingGoal?.let { goal ->
        EditGoalDialog(
            goal = goal,
            onDismiss = { editingGoal = null },
            onSave = { updated ->
                goals = goals.map { if (it.id == updated.id) updated else it }
                editingGoal = null
            }
        )
    }

    // Delete Dialog
    deletingGoal?.let { goal ->
        DeleteGoalDialog(
            goal = goal,
            onDismiss = { deletingGoal = null },
            onConfirm = {
                goals = goals.filter { it.id != goal.id }
                deletingGoal = null
            }
        )
    }
}

data class Goal(
    val id: Int,
    val target: String,
    val progress: Float
)

@Composable
fun GoalsList(
    goals: List<Goal>,
    onEdit: (Goal) -> Unit,
    onDelete: (Goal) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(goals) { goal ->
            GoalCard(
                goal = goal,
                onEdit = { onEdit(goal) },
                onDelete = { onDelete(goal) }
            )
        }
    }
}

@Composable
fun GoalCard(
    goal: Goal,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftBlueTint),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = goal.target, style = MaterialTheme.typography.titleMedium)
                GoalMenu(onEdit, onDelete)
            }

            LinearProgressIndicator(
                progress = {goal.progress},
                color = StoneGrey,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${(goal.progress * 100).toInt()}% complete",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun GoalMenu(
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = "Edit") },
                onClick = {
                    expanded = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text(text = "Delete") },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
fun EditGoalDialog(
    goal: Goal,
    onDismiss: () -> Unit,
    onSave: (Goal) -> Unit
) {
    var target by remember { mutableStateOf(goal.target) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SoftBlueTint,
        title = { Text("Edit Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text(text = "Goal Title", color = StoneGrey) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(goal.copy(target = target))
            }) {
                Text("Save", color = StoneGrey)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = StoneGrey)
            }
        }
    )
}

@Composable
fun DeleteGoalDialog(
    goal: Goal,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete Goal") },
        text = { Text(text = "Are you sure you want to delete \"${goal.target}\"?", color = StoneGrey) },
        containerColor = SoftBlueTint,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Delete", color = StoneGrey)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = StoneGrey)
            }
        }
    )
}

// Motivational Quote
@Composable
fun MotivationalSection() {
    val messages = listOf(
        "You're doing great — keep pushing!",
        "Small steps lead to big changes.",
        "Your future self will thank you.",
        "Consistency beats intensity."
    )

    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            index = (index + 1) % messages.size
        }
    }

    Card(modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = SoftBlueTint),
        border = BorderStroke(0.5.dp, color = StoneGrey)
    ) {
        Crossfade(targetState = index) { i ->
            Text(
                text = messages[i],
                modifier = Modifier.padding(16.dp),
                color = StoneGrey,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GoalsPreview() {
    TrAInAppTheme {
        Goals()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDeleteGoalDialog() {
    TrAInAppTheme {
        DeleteGoalDialog(
            goal = Goal(
                id = 1,
                target = "Take 2 push ups",
                progress = 0.5f
            ),
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEditGoalDialog() {
    TrAInAppTheme {
        EditGoalDialog(
            goal = Goal(
                id = 1,
                target = "Workout 4 times",
                progress = 0.75f
            ),
            onDismiss = {},
            onSave = {}
        )
    }
}

