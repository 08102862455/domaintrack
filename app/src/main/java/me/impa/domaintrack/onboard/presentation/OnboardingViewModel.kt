package me.impa.domaintrack.onboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.impa.domaintrack.core.presentation.navigation.Navigator
import me.impa.domaintrack.core.presentation.navigation.Route
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val navigator: Navigator
) : ViewModel() {

    fun onSettingsClick() {
        viewModelScope.launch {
            navigator.goBack()
            navigator.navigate(Route.Settings)
        }
    }

    fun onBackClick() {
        navigator.goBack()
    }
}
