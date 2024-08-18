package com.apps.adrcotfas.goodtime.settings.notification_sounds

import com.apps.adrcotfas.goodtime.data.settings.SoundData
import kotlinx.serialization.json.Json

fun toSoundData(ringtoneRaw: String): SoundData {
    return if (ringtoneRaw.isNotEmpty())
        Json.decodeFromString(
            SoundData.serializer(),
            ringtoneRaw
        )
    else SoundData()
}