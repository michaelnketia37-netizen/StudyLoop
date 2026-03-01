package com.studyloop.reminder

import com.studyloop.core.model.ReviewEntity

object SpacedRepetitionEngine {

    // Ebbinghaus review intervals in days
    val REVIEW_DAYS = listOf(1, 3, 7, 14, 30, 60, 120)

    // Calculate all 7 review timestamps from save time
    fun calculateReviewSchedule(savedAt: Long): List<Long> =
        REVIEW_DAYS.map { days -> savedAt + days * 24 * 60 * 60 * 1000L }

    // Retention at any point using R(t) = R0 × e^(−t/S)
    fun retentionAt(timeSinceReviewMs: Long, stability: Double): Double {
        val t = timeSinceReviewMs / (24.0 * 60 * 60 * 1000)
        return Math.exp(-t / stability).coerceIn(0.0, 1.0)
    }

    // Stability factor grows after each completed review
    fun stabilityForReview(reviewNumber: Int): Double =
        REVIEW_DAYS.getOrElse(reviewNumber) { 120 }.toDouble()

    // Retention boost after completing a review
    fun retentionBoostAfterReview(reviewNumber: Int): Double =
        (0.85 + reviewNumber * 0.02).coerceAtMost(0.98)

    // Generate full curve data (day 0 → 120) for MPAndroidChart
    fun generateCurvePoints(
        savedAt: Long,
        completedReviews: List<ReviewEntity>
    ): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()
        var stability = 1.0
        var lastReviewDay = 0
        var currentRetention = 1.0

        val completedDays = completedReviews
            .filter { it.completedAt != null }
            .map { REVIEW_DAYS[it.reviewNumber] }
            .toSet()

        for (day in 0..120) {
            val elapsed = (day - lastReviewDay).toDouble().coerceAtLeast(0.0)
            val decay = Math.exp(-elapsed / stability)
            val retention = currentRetention * decay
            points.add(Pair(day.toFloat(), (retention * 100).toFloat()))

            if (REVIEW_DAYS.contains(day) && completedDays.contains(day)) {
                val reviewNum = REVIEW_DAYS.indexOf(day)
                stability = stabilityForReview(reviewNum)
                currentRetention = retentionBoostAfterReview(reviewNum)
                lastReviewDay = day
            }
        }
        return points
    }

    // Current retention % (0–100) for display
    fun currentRetentionPercent(
        savedAt: Long,
        completedReviews: List<ReviewEntity>
    ): Int {
        val points = generateCurvePoints(savedAt, completedReviews)
        val daysSinceSave = (System.currentTimeMillis() - savedAt) / (24.0 * 60 * 60 * 1000)
        val closest = points.minByOrNull { Math.abs(it.first - daysSinceSave) }
        return closest?.second?.toInt() ?: 0
    }

    // Days/hours until next review
    fun nextReviewCountdown(completedCount: Int, savedAt: Long): String {
        if (completedCount >= REVIEW_DAYS.size) return "✓ Complete"
        val nextDay = REVIEW_DAYS[completedCount]
        val nextTime = savedAt + nextDay * 24 * 60 * 60 * 1000L
        val diff = nextTime - System.currentTimeMillis()
        return when {
            diff <= 0 -> "Due Now!"
            diff < 3_600_000 -> "${diff / 60_000}m"
            diff < 86_400_000 -> "${diff / 3_600_000}h"
            else -> "${diff / 86_400_000}d"
        }
    }
}
