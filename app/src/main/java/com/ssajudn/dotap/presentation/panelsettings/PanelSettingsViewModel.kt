package com.ssajudn.dotap.presentation.panelsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.Defaults
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.domain.model.PanelSlot
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.InstalledAppsRepository
import com.ssajudn.dotap.domain.repository.SettingsRepository
import com.ssajudn.dotap.presentation.common.AppsCatalogState
import com.ssajudn.dotap.presentation.common.loadCatalog
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PanelSettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {
    private val appsState = MutableStateFlow(AppsCatalogState())
    private val pickerTarget = MutableStateFlow<PanelAppPickerTarget?>(null)

    val uiState: StateFlow<PanelSettingsUiState> = combine(
        settingsRepository.settings,
        appsState,
        pickerTarget,
    ) { settings, catalog, target ->
        val slots = settings.panelSlots
        PanelSettingsUiState(
            slots = slots,
            availableActions = resolveAvailableActions(slots),
            installedApps = catalog.apps,
            appLabels = catalog.labels,
            isLoadingApps = catalog.isLoading,
            appPickerTarget = target,
            canAddMore = slots.size < Limits.MAX_PANEL_SLOTS,
            canRemove = slots.size > Limits.MIN_PANEL_SLOTS,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = PanelSettingsUiState(),
    )

    private val _uiEffect = Channel<PanelSettingsUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<PanelSettingsUiEffect> = _uiEffect.receiveAsFlow()

    init {
        loadLaunchableApps()
    }

    fun onEvent(event: PanelSettingsUiEvent) {
        viewModelScope.launch(dispatcherProvider.main) {
            when (event) {
                is PanelSettingsUiEvent.MoveSlot -> moveSlot(event.fromIndex, event.toIndex)
                is PanelSettingsUiEvent.AddSlot -> handleAddSlot(event.action)
                is PanelSettingsUiEvent.EditSlotApp -> handleEditSlotApp(event.index)
                is PanelSettingsUiEvent.AppPicked -> handleAppPicked(event.packageName)
                PanelSettingsUiEvent.DismissAppPicker -> pickerTarget.value = null
                is PanelSettingsUiEvent.RemoveSlot -> removeSlot(event.index)
                PanelSettingsUiEvent.ResetDefaultsClicked -> resetDefaults()
                PanelSettingsUiEvent.BackClicked -> _uiEffect.send(PanelSettingsUiEffect.NavigateBack)
            }
        }
    }

    private fun loadLaunchableApps() {
        viewModelScope.launch(dispatcherProvider.main) {
            appsState.value = installedAppsRepository.loadCatalog()
        }
    }

    private fun resolveAvailableActions(slots: List<PanelSlot>): List<ActionType> {
        val activeActions = slots.map { it.action }.toSet()
        return ActionType.entries.filter { action ->
            action.canBePanelSlot && (action == ActionType.LAUNCH_APP || action !in activeActions)
        }
    }

    private suspend fun moveSlot(fromIndex: Int, toIndex: Int) {
        settingsRepository.update { settings ->
            val current = settings.panelSlots
            val isValidMove = fromIndex in current.indices &&
                toIndex in current.indices &&
                fromIndex != toIndex
            if (isValidMove) {
                val reordered = current.toMutableList().apply {
                    add(toIndex, removeAt(fromIndex))
                }
                settings.withPanelSlots(reordered)
            } else {
                settings
            }
        }
    }

    private suspend fun handleAddSlot(action: ActionType) {
        if (!action.canBePanelSlot) return
        if (action == ActionType.LAUNCH_APP) {
            if (uiState.value.canAddMore) {
                pickerTarget.value = PanelAppPickerTarget.NewSlot
            }
            return
        }
        settingsRepository.update { settings ->
            val canAdd = settings.panelSlots.none { it.action == action } &&
                settings.panelSlots.size < Limits.MAX_PANEL_SLOTS
            if (canAdd) {
                settings.withPanelSlots(settings.panelSlots + PanelSlot(action))
            } else {
                settings
            }
        }
    }

    private fun handleEditSlotApp(index: Int) {
        val slot = uiState.value.slots.getOrNull(index) ?: return
        if (slot.action == ActionType.LAUNCH_APP) {
            pickerTarget.value = PanelAppPickerTarget.ExistingSlot(
                index = index,
                currentPackage = slot.appPackage,
            )
        }
    }

    private suspend fun handleAppPicked(packageName: String) {
        val target = pickerTarget.value ?: return
        pickerTarget.value = null
        settingsRepository.update { settings ->
            when (target) {
                PanelAppPickerTarget.NewSlot -> if (settings.panelSlots.size < Limits.MAX_PANEL_SLOTS) {
                    settings.withPanelSlots(
                        settings.panelSlots + PanelSlot(ActionType.LAUNCH_APP, packageName),
                    )
                } else {
                    settings
                }

                is PanelAppPickerTarget.ExistingSlot -> {
                    val updated = settings.panelSlots.mapIndexed { index, slot ->
                        if (index == target.index) {
                            PanelSlot(ActionType.LAUNCH_APP, packageName)
                        } else {
                            slot
                        }
                    }
                    settings.withPanelSlots(updated)
                }
            }
        }
    }

    private suspend fun removeSlot(index: Int) {
        settingsRepository.update { settings ->
            val canRemove = index in settings.panelSlots.indices &&
                settings.panelSlots.size > Limits.MIN_PANEL_SLOTS
            if (canRemove) {
                settings.withPanelSlots(settings.panelSlots.filterIndexed { i, _ -> i != index })
            } else {
                settings
            }
        }
    }

    private suspend fun resetDefaults() {
        settingsRepository.update { settings ->
            settings.withPanelSlots(Defaults.DEFAULT_PANEL_SLOTS)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
