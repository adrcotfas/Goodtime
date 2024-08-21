package com.apps.adrcotfas.goodtime.bl.notifications

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE
import android.hardware.camera2.CameraManager
import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.data.settings.FlashType
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TorchManagerData(
    val enabled: Boolean = false,
    val loop: Boolean = false
)

/**
 * Uses the camera flash, if available, to notify the user.
 */
class TorchManager(
    context: Context,
    readFromSettingsScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val playerScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val settingsRepo: SettingsRepository,
    private val logger: Logger
) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    private var data: TorchManagerData = TorchManagerData()

    private var job: Job? = null

    init {
        for (id in cameraManager.cameraIdList) {
            val flashAvailable =
                cameraManager.getCameraCharacteristics(id).get(FLASH_INFO_AVAILABLE)
            if (flashAvailable == true) {
                cameraId = id
                break
            }
        }
        readFromSettingsScope.launch {
            settingsRepo.settings.map {
                TorchManagerData(
                    enabled = it.flashType == FlashType.CAMERA,
                    loop = it.insistentNotification
                )
            }.collect {
                data = it
            }
        }
    }

    fun isFlashAvailable() = cameraId != null

    fun start() {
        if (!data.enabled) return
        job = playerScope.launch {
            cameraId?.let {
                try {
                    val pattern = listOf(100L, 50L, 100L)
                    if (data.loop) {
                        while(isActive) {
                            cameraManager.lightUp(pattern, it)
                            delay(1000)
                        }
                    } else {
                        cameraManager.lightUp(pattern, it)
                    }
                } catch (e: CameraAccessException) {
                    logger.e(e) { "Failed to access the camera" }
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        cameraId?.let {
            cameraManager.setTorchMode(it, false)
        }
    }
}

private suspend fun CameraManager.lightUp(listOf: List<Long>, cameraId: String) {
    listOf.forEachIndexed { index, i ->
        if (index % 2 == 0) {
            setTorchMode(cameraId, true)
        } else {
            setTorchMode(cameraId, false)
        }
        delay(i)
    }
    setTorchMode(cameraId, false)
}
