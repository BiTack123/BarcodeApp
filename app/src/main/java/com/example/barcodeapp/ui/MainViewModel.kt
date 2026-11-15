package com.example.barcodeapp.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.barcodeapp.domain.parseBarcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private var device1: String? = null
    private var device2: String? = null

    fun onScan(raw: String, playSound: (Boolean) -> Unit) {
        // Парсим строку ШК; если формат не подходит — игнорируем
        val p = parseBarcode(raw) ?: return

        // Логика проверок:
        // '1' — запоминаем пару устройств; '2' — сверяем device1; '3' — сверяем device2
        val ok = when (p.op) {
            '1' -> {          
                device1 = p.device1!!
                device2 = p.device2!!   
                true
            }
            '2' -> p.device1 == device1         
            '3' -> p.device2 == device2             
            else -> false
        }

        // Обновляем UI-состояние: последний скан, текущие устройства и цвет фона
        _state.value = UiState(
            lastRaw = p.raw,
            device1 = device1,
            device2 = device2,
            bgColor = when (p.op) {
                // Для инициализации — голубой, далее: зелёный при успехе, красный при ошибке
                '1'  -> Color(0xFFBBDEFB)         
                else -> if (ok) Color(0xFFB9F6CA) else Color(0xFFFFCDD2)
            }
        )
        playSound(ok)
    }


    fun reset() {
        // Сбрасываем текущие устройства и возвращаем стандартное состояние
        device1 = null; device2 = null
        _state.value = UiState()
    }
}

data class UiState(
    val lastRaw: String = "",
    val device1: String? = null,
    val device2: String? = null,
    val bgColor: Color = Color.White
)
