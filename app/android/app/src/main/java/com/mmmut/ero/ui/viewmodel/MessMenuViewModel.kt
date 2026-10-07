package com.mmmut.ero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmmut.ero.core.RepoResult
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.local.MessMenuData
import com.mmmut.ero.data.model.MessDay
import com.mmmut.ero.data.model.MessWeeklyMenu
import com.mmmut.ero.data.repository.MessMenuRepository
import com.mmmut.ero.data.repository.MessMenuRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MessMenuUiData(
    val weeklyMenu: MessWeeklyMenu,
    val todayName: String,
    val selectedDay: MessDay,
    val isWeeklyView: Boolean = false
)

class MessMenuViewModel(
    private val repo: MessMenuRepository = MessMenuRepositoryImpl()
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<MessMenuUiData>>(UiState.Loading)
    val state: StateFlow<UiState<MessMenuUiData>> = _state.asStateFlow()

    private var currentWeeklyMenu: MessWeeklyMenu = MessMenuData.DEFAULT_WEEKLY_MENU
    private var todayDayName: String = MessMenuData.getTodayDayName()
    private var selectedDayName: String = todayDayName
    private var isWeeklyView: Boolean = false

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            todayDayName = MessMenuData.getTodayDayName()
            if (selectedDayName.isBlank()) {
                selectedDayName = todayDayName
            }

            when (val res = repo.getWeeklyMenu()) {
                is RepoResult.Ok -> {
                    currentWeeklyMenu = res.value
                    updateUiState()
                }
                is RepoResult.Err -> {
                    currentWeeklyMenu = MessMenuData.DEFAULT_WEEKLY_MENU
                    updateUiState()
                }
            }
        }
    }

    fun selectDay(dayName: String) {
        selectedDayName = dayName
        isWeeklyView = false
        updateUiState()
    }

    fun toggleWeeklyView() {
        isWeeklyView = !isWeeklyView
        updateUiState()
    }

    fun showWeeklyView() {
        isWeeklyView = true
        updateUiState()
    }

    fun showDayView(dayName: String = selectedDayName) {
        selectedDayName = dayName
        isWeeklyView = false
        updateUiState()
    }

    private fun updateUiState() {
        val activeDay = currentWeeklyMenu.days.firstOrNull { it.day.equals(selectedDayName, ignoreCase = true) }
            ?: currentWeeklyMenu.days.firstOrNull { it.day.equals(todayDayName, ignoreCase = true) }
            ?: currentWeeklyMenu.days.first()

        _state.value = UiState.Success(
            MessMenuUiData(
                weeklyMenu = currentWeeklyMenu,
                todayName = todayDayName,
                selectedDay = activeDay,
                isWeeklyView = isWeeklyView
            )
        )
    }
}
