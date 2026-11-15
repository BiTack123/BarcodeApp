package com.example.barcodeapp.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.zxing.ResultPoint         
import com.journeyapps.barcodescanner.*

@Composable
fun BarcodeScanner(
    onScan: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Вставляем в Compose готовый сканер из ZXing, слушаем результаты непрерывно
    val owner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            DecoratedBarcodeView(ctx).apply {
                initializeFromIntent(Intent())
                decodeContinuous(object : BarcodeCallback {
                    override fun barcodeResult(result: BarcodeResult?) {
                        // При каждом найденном коде отдаём текст наружу
                        result?.text?.let(onScan)
                    }
                    override fun possibleResultPoints(resultPoints: List<ResultPoint>) = Unit
                })
                resume()
                owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                    // Синхронизируемся с жизненным циклом экрана
                    override fun onPause(owner: LifecycleOwner) = pause()
                    override fun onResume(owner: LifecycleOwner) = resume()
                })
            }
        }
    )
}
