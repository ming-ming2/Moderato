// app/src/main/java/com/example/moderato/ChordHistoryManager.kt
package com.example.moderato

import android.content.Context
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ChordHistoryManager(private val context: Context) {

    data class ChordHistoryEntry(
        val date: String,
        val chordName: String,
        val chordSymbol: String,
        val chordFullName: String,
        val message: String,
        val emotionCount: Int,
        val dominantEmotion: String,
        val intensity: String,
        val timestamp: String
    )

    companion object {
        private const val CHORD_HISTORY_FILE = "chord_history.txt"
        private const val SEPARATOR = "|"
    }

    fun saveChordHistory(chord: EmotionChordAnalyzer.EmotionChord): Boolean {
        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val entry = buildString {
                append(today).append(SEPARATOR)
                append(chord.chordName).append(SEPARATOR)
                append(chord.chordSymbol).append(SEPARATOR)
                append(chord.chordFullName).append(SEPARATOR)
                append(chord.message.replace("\n", "\\n")).append(SEPARATOR)
                append(chord.emotionCount).append(SEPARATOR)
                append(chord.dominantEmotion).append(SEPARATOR)
                append(chord.intensity).append(SEPARATOR)
                append(timestamp)
                append("\n")
            }

            if (isChordAlreadyExists(today)) {
                updateExistingChord(today, entry)
            } else {
                appendNewChord(entry)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun isChordAlreadyExists(date: String): Boolean {
        return try {
            val fileInput: FileInputStream = context.openFileInput(CHORD_HISTORY_FILE)
            val content = fileInput.bufferedReader().use { it.readText() }
            fileInput.close()

            content.lines().any { line ->
                line.startsWith(date + SEPARATOR)
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun updateExistingChord(date: String, newEntry: String) {
        try {
            val fileInput: FileInputStream = context.openFileInput(CHORD_HISTORY_FILE)
            val lines = fileInput.bufferedReader().use { it.readLines() }
            fileInput.close()

            val updatedLines = lines.map { line ->
                if (line.startsWith(date + SEPARATOR)) {
                    newEntry.trim()
                } else {
                    line
                }
            }

            val fileOutput: FileOutputStream = context.openFileOutput(CHORD_HISTORY_FILE, Context.MODE_PRIVATE)
            fileOutput.write(updatedLines.joinToString("\n").toByteArray())
            fileOutput.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun appendNewChord(entry: String) {
        try {
            val fileOutput: FileOutputStream = context.openFileOutput(CHORD_HISTORY_FILE, Context.MODE_APPEND)
            fileOutput.write(entry.toByteArray())
            fileOutput.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadChordHistory(): List<ChordHistoryEntry> {
        return try {
            val fileInput: FileInputStream = context.openFileInput(CHORD_HISTORY_FILE)
            val content = fileInput.bufferedReader().use { it.readText() }
            fileInput.close()

            content.lines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    parseChordHistoryEntry(line)
                }
                .sortedByDescending { it.date }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseChordHistoryEntry(line: String): ChordHistoryEntry? {
        return try {
            val parts = line.split(SEPARATOR)
            if (parts.size >= 9) {
                ChordHistoryEntry(
                    date = parts[0],
                    chordName = parts[1],
                    chordSymbol = parts[2],
                    chordFullName = parts[3],
                    message = parts[4].replace("\\n", "\n"),
                    emotionCount = parts[5].toInt(),
                    dominantEmotion = parts[6],
                    intensity = parts[7],
                    timestamp = parts[8]
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getChordHistoryByDateRange(startDate: String, endDate: String): List<ChordHistoryEntry> {
        return loadChordHistory().filter { entry ->
            entry.date >= startDate && entry.date <= endDate
        }
    }

    fun getRecentChords(count: Int = 7): List<ChordHistoryEntry> {
        return loadChordHistory().take(count)
    }

    fun getChordStatistics(): ChordStatistics {
        val history = loadChordHistory()
        val chordCounts = mutableMapOf<String, Int>()
        val emotionCounts = mutableMapOf<String, Int>()

        history.forEach { entry ->
            chordCounts[entry.chordName] = chordCounts.getOrDefault(entry.chordName, 0) + 1
            emotionCounts[entry.dominantEmotion] = emotionCounts.getOrDefault(entry.dominantEmotion, 0) + 1
        }

        return ChordStatistics(
            totalDays = history.size,
            mostFrequentChord = chordCounts.maxByOrNull { it.value }?.key ?: "없음",
            mostFrequentEmotion = emotionCounts.maxByOrNull { it.value }?.key ?: "없음",
            chordDistribution = chordCounts,
            emotionDistribution = emotionCounts,
            averageEmotionCount = if (history.isNotEmpty()) {
                history.sumOf { it.emotionCount } / history.size.toDouble()
            } else 0.0
        )
    }

    fun deleteChordHistory(date: String): Boolean {
        return try {
            val fileInput: FileInputStream = context.openFileInput(CHORD_HISTORY_FILE)
            val lines = fileInput.bufferedReader().use { it.readLines() }
            fileInput.close()

            val filteredLines = lines.filter { line ->
                !line.startsWith(date + SEPARATOR)
            }

            val fileOutput: FileOutputStream = context.openFileOutput(CHORD_HISTORY_FILE, Context.MODE_PRIVATE)
            fileOutput.write(filteredLines.joinToString("\n").toByteArray())
            fileOutput.close()

            true
        } catch (e: Exception) {
            false
        }
    }

    fun clearAllHistory(): Boolean {
        return try {
            context.deleteFile(CHORD_HISTORY_FILE)
            true
        } catch (e: Exception) {
            false
        }
    }

    data class ChordStatistics(
        val totalDays: Int,
        val mostFrequentChord: String,
        val mostFrequentEmotion: String,
        val chordDistribution: Map<String, Int>,
        val emotionDistribution: Map<String, Int>,
        val averageEmotionCount: Double
    )
}