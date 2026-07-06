package com.funnyenglish.feature.quiz

import android.media.AudioManager
import android.media.ToneGenerator

object SoundEffectPlayer {
    private val toneGenerator by lazy { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }

    fun playCorrect() {
        try {
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
        } catch (_: Exception) { /* ignore */ }
    }

    fun playIncorrect() {
        try {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 200)
        } catch (_: Exception) { /* ignore */ }
    }
}
