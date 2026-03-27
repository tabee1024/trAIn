package com.example.train

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.train.ui.theme.*
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Copies an image from a URI to the app's internal storage.
 * Returns the File object pointing to the new local image.
 */
fun saveImageToInternalStorage(context: Context, uri: Uri): File? {
    val fileName = "profile_picture.jpg"
    val file = File(context.filesDir, fileName)

    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun Profile() {
    val survey = UserProfileStore.latestSurvey
    val context = LocalContext.current

    // State to hold the URI of the selected profile image
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher to pick an image from the gallery
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        // Note: For actual cropping, you would usually launch a
        // secondary Activity here from a library like UCrop.
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = "My Fitness Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepNavy)

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Image Container
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(SoftBlueTint)
                .border(2.dp, DeepNavy, CircleShape)
                .clickable { launcher.launch("image/*") }, // Launches Gallery
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                // We use Coil to display the URI (Requires 'io.coil-kt:coil-compose' in gradle)
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop // This provides a "center-crop" look
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(50.dp), tint = DeepNavy)
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(20.dp), tint = DeepNavy)
                }
            }
        }

        Text(
            text = "Tap to change photo",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SoftBlueTint.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileSectionTitle("Basic Information")
                ProfileInfoRow("Nickname", survey?.nickname ?: "Not set")
                ProfileInfoRow("Age Range", survey?.ageRange?.label ?: "Not set")
                ProfileInfoRow("Gender", survey?.gender?.label ?: "Not set")
                ProfileInfoRow("Overall Health", survey?.healthLevel?.label ?: "Not set")

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                ProfileSectionTitle("Training Profile")
                ProfileInfoRow("Fitness Level", survey?.fitnessLevel?.label ?: "Not set")
                ProfileInfoRow("Primary Goal", survey?.primaryGoal?.label ?: "Not set")
                ProfileInfoRow("Experience", survey?.previousExperience?.label ?: "Not set")

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                ProfileSectionTitle("Safety")
                val injuryText = survey?.injuries?.joinToString { it.label }.takeIf { !it.isNullOrBlank() } ?: "None reported"
                ProfileInfoRow("Injuries", injuryText)
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun ProfileSectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleSmall, color = DeepNavy, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}