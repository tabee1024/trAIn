package com.example.train.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4 // Requires 'androidTest' folder
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

// --- MOCK BINDING INTERFACE ---
interface FragmentSettingsBinding {
    val root: View
    val textSettings: TextView

    companion object {
        fun inflate(inflater: LayoutInflater, container: ViewGroup?, attachToRoot: Boolean): FragmentSettingsBinding {
            // Correct Kotlin Mockito syntax: mock(Class::class.java)
            val mockView = mock(View::class.java)
            val mockTextView = mock(TextView::class.java)

            return object : FragmentSettingsBinding {
                override val root: View = mockView
                override val textSettings: TextView = mockTextView
            }
        }
    }
}

// --- MOCK VIEWMODEL ---
// Added '?' to LiveData to match your null-testing logic
class MockSettingsViewModel(
    val mockText: LiveData<String>? = MutableLiveData("Initial Text")
) : ViewModel() {
    val text: LiveData<String>? = mockText
}

// --- TEST CLASS ---
// This annotation requires the "ext:junit" library we added to your gradle
@RunWith(AndroidJUnit4::class)
class SettingsFragmentTest {

    @Test
    fun test_onCreateView_with_null_liveData_should_not_crash() {
        // Arrange
        val nullLiveDataViewModel = MockSettingsViewModel(mockText = null)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return nullLiveDataViewModel as T
            }
        }

        // Act
        // Use launchInContainer for Fragment tests that involve UI/Inflation
        val scenario = FragmentScenario.launchInContainer(
            SettingsFragment::class.java,
            null,
            factory
        )

        // Assert
        scenario.onFragment { fragment ->
            assertNotNull(fragment)
            // Ensure fragment reached the RESUMED state without crashing
            assert(fragment.lifecycle.currentState == Lifecycle.State.RESUMED)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun test_onCreateView_with_null_binding_should_throw_exception() {
        // This test simulates a crash when _binding!! is called on a null object
        val scenario = FragmentScenario.launchInContainer(SettingsFragment::class.java)

        // Force the fragment to move to a state where it interacts with the view
        scenario.moveToState(Lifecycle.State.RESUMED)
    }
}