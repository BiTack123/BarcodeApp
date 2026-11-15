package com.example.barcodeapp.ui

import android.Manifest
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.barcodeapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val reqCam = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Просим доступ к камере; без него сканер не заработает
        if (!granted) Toast.makeText(this, "Камера нужна для сканирования", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Фиксируем вертикальную ориентацию и не даём экрану гаснуть
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        reqCam.launch(Manifest.permission.CAMERA)

        // Два звука: успешный и ошибочный
        val okSound  by lazy { MediaPlayer.create(this, R.raw.beep_ok) }
        val badSound by lazy { MediaPlayer.create(this, R.raw.beep_bad) }

        setContent {
            val vm: MainViewModel = hiltViewModel()
            val ui by vm.state.collectAsState()

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = ui.bgColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                ) {
                    // Заголовок экрана
                    Text("Сканер станков", style = MaterialTheme.typography.titleLarge)

                    BarcodeScanner(
                        // Передаём обработчик: после скана ViewModel решает, какой звук проиграть
                        onScan = { vm.onScan(it) { ok -> if (ok) okSound.start() else badSound.start() } },
                        modifier = Modifier.size(280.dp)
                    )

                    // Подсказки по последнему скану и текущим устройствам
                    if (ui.lastRaw.isNotEmpty())
                        Text("Последний ШК: ${ui.lastRaw}")

                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Text("Устройство1: ${ui.device1 ?: "---"}")
                        Text("Устройство2: ${ui.device2 ?: "---"}")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // «Старт» очищает состояние, «Финиш» закрывает экран
                        Button(onClick = vm::reset) { Text("Старт") }
                        Button(onClick = { finish() }) { Text("Финиш") }
                    }
                }
            }
        }
    }
}
