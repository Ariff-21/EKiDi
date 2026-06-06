package com.example.ekidi.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var soundCorrect: Int = 0
    private var soundWrong: Int = 0
    private var soundClick: Int = 0
    
    private var mediaPlayer: MediaPlayer? = null

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSounds()
    }

    private fun loadSounds() {
        soundCorrect = loadRawResource("correct_sound")
        soundWrong = loadRawResource("wrong_sound")
        soundClick = loadRawResource("click_sound")
    }

    private fun loadRawResource(name: String): Int {
        val id = context.resources.getIdentifier(name, "raw", context.packageName)
        return if (id != 0) {
            soundPool?.load(context, id, 1) ?: 0
        } else 0
    }

    fun playCorrect() {
        if (soundCorrect != 0) soundPool?.play(soundCorrect, 1f, 1f, 0, 0, 1f)
    }

    fun playWrong() {
        if (soundWrong != 0) soundPool?.play(soundWrong, 1f, 1f, 0, 0, 1f)
    }

    fun playClick() {
        if (soundClick != 0) soundPool?.play(soundClick, 1f, 1f, 0, 0, 1f)
    }

    fun startBackgroundMusic(name: String) {
        val id = context.resources.getIdentifier(name, "raw", context.packageName)
        if (id != 0) {
            startBackgroundMusic(id)
        }
    }

    private fun startBackgroundMusic(resId: Int) {
        stopBackgroundMusic()
        try {
            mediaPlayer = MediaPlayer.create(context, resId).apply {
                isLooping = true
                setVolume(0.4f, 0.4f)
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopBackgroundMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        stopBackgroundMusic()
    }
}
