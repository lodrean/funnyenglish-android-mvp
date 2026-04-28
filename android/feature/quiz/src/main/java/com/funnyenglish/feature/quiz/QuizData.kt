package com.funnyenglish.feature.quiz

data class QuizQuestion(
    val word: String,
    val correctTranslation: String,
    val options: List<String>
)

val quizQuestions = listOf(
    QuizQuestion("serendipity", "счастливая случайность", listOf("счастливая случайность", "печаль", "ярость", "скука")),
    QuizQuestion("ephemeral", "мимолётный", listOf("вечный", "мимолётный", "тяжёлый", "светлый")),
    QuizQuestion("resilient", "стойкий", listOf("стойкий", "хрупкий", "ленивый", "грубый")),
    QuizQuestion("eloquent", "красноречивый", listOf("немой", "красноречивый", "злой", "быстрый")),
    QuizQuestion("meticulous", "дотошный", listOf("небрежный", "дотошный", "смелый", "тихий")),
    QuizQuestion("pragmatic", "прагматичный", listOf("прагматичный", "мечтательный", "грустный", "весёлый")),
    QuizQuestion("ambiguous", "двусмысленный", listOf("ясный", "двусмысленный", "короткий", "длинный")),
    QuizQuestion("benevolent", "доброжелательный", listOf("злобный", "доброжелательный", "ленивый", "громкий")),
    QuizQuestion("candid", "искренний", listOf("лживый", "искренний", "медленный", "тёмный")),
    QuizQuestion("diligent", "прилежный", listOf("прилежный", "ленивый", "грубый", "тихий"))
)
