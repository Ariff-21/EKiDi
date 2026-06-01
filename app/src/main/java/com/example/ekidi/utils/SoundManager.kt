package com.example.ekidi.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.ekidi.R

class SoundManager(context: Context) {
    private var soundPool: SoundPool
    private var soundMap: MutableMap<Int, Int> = mutableMapOf()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load sounds - assumes files exist in res/raw
        // I will use try-catch or checks if resources exist to avoid crashes if files are missing
        loadSound(context, SOUND_CLICK, "click")
        loadSound(context, SOUND_CORRECT, "correct")
        loadSound(context, SOUND_WRONG, "wrong")
        loadSound(context, SOUND_SUCCESS, "success_level")
    }

    private fun loadSound(context: Context, key: Int, name: String) {
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        if (resId != 0) {
            soundMap[key] = soundPool.load(context, resId, 1)
        }
    }

    fun playSound(soundKey: Int) {
        val soundId = soundMap[soundKey]
        if (soundId != null) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun release() {
        soundPool.release()
    }

    companion object {
        const val SOUND_CLICK = 1
        const val SOUND_CORRECT = 2
        const val SOUND_WRONG = 3
        const val SOUND_SUCCESS = 4
    }
}
