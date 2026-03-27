package com.example.train

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.train.ui.theme.*

@Composable
fun Settings() {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(color = Color.White)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(70.dp))
        Text(text = "Settings",
            style = MaterialTheme.typography.titleSmall,
            fontSize = 30.sp,
            color = DeepNavy
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsContent()
    }
}

@Composable
fun SettingsContent() {
    var darkModeEnabled by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var useMetricUnits by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Column(modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Appearance
        SettingsSection(title = "Appearance") {
            SettingsToggleItem(
                title = "Dark Mode",
                checked = darkModeEnabled,
                onCheckedChange = { darkModeEnabled = it }
            )
        }

        // Notifications
        SettingsSection(title = "Notifications") {
            SettingsToggleItem(
                title = "Enable Notifications",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }

        // Units Section
        SettingsSection(title = "Units") {
            SettingsToggleItem(
                title = "Use Metric Units",
                checked = useMetricUnits,
                onCheckedChange = { useMetricUnits = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
// Account Section
        Button(
            onClick = {
                android.widget.Toast.makeText(context, "Logout", android.widget.Toast.LENGTH_SHORT).show()
            }
        ) {
            Text("Log Out")
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = DeepNavy, thickness = 0.5.dp)
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )
        content()
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedBorderColor = DeepNavy,
                checkedTrackColor = DeepNavy,
                uncheckedBorderColor = StoneGrey,
                uncheckedThumbColor = StoneGrey,
                uncheckedTrackColor = Color.Transparent
            )
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}



@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    TrAInAppTheme {
        Settings()
    }
}
