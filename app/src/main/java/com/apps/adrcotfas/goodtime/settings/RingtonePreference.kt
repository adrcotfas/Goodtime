package com.apps.adrcotfas.goodtime.settings

import android.media.RingtoneManager
import com.google.gson.Gson

data class Ringtone(val uri: String, val name: String)

fun toRingtone(ringtoneRaw: String, defaultName: String = ""): Ringtone {
    val gson = Gson()
    return if (ringtoneRaw.isNotEmpty())
        gson.fromJson(
            ringtoneRaw,
            Ringtone::class.java
        )
    else Ringtone(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString(), defaultName)
}
