package com.orbit.app.core.data.planner

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class PlannerItem(
    val id: String,
    val category: String, // HEALTH, STUDY, PROJECT, AI_LEARNING, PERSONAL, REVIEW
    val title: String,
    val timeStr: String,
    val durationMinutes: Int,
    val description: String,
    var isCompleted: Boolean = false
)

data class DailyPlanReport(
    val dateStr: String,
    val greeting: String,
    val items: List<PlannerItem>,
    val studySubjectsToday: List<String>
)

@Singleton
class DailyPlannerEngine @Inject constructor() {

    private val allStudySubjects = listOf(
        "Interview Preparation",
        "Android Development",
        "DSA with Java",
        "AI and Generative AI"
    )

    private var currentDayIndex = 0

    fun generatePlan(
        incompleteSubject: String? = null,
        isTiredMode: Boolean = false
    ): DailyPlanReport {
        val now = Date()
        val dateIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
        val dateDisplay = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now)

        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour in 5..11 -> "Good Morning! 🌅"
            hour in 12..16 -> "Good Afternoon! ☀️"
            else -> "Good Evening! 🌙"
        }

        // Rotate 3 subjects out of 4 intelligently
        val rotated = mutableListOf<String>()
        if (!incompleteSubject.isNullOrBlank() && allStudySubjects.any { it.contains(incompleteSubject, ignoreCase = true) }) {
            val matched = allStudySubjects.first { it.contains(incompleteSubject, ignoreCase = true) }
            rotated.add(matched)
        }

        for (i in 0..3) {
            val idx = (currentDayIndex + i) % allStudySubjects.size
            val subj = allStudySubjects[idx]
            if (!rotated.contains(subj) && rotated.size < 3) {
                rotated.add(subj)
            }
        }
        currentDayIndex = (currentDayIndex + 1) % allStudySubjects.size

        val items = mutableListOf<PlannerItem>()

        // 1. Morning Health Routine
        items.add(PlannerItem("p1", "HEALTH", "Wake Up & Freshen Up", "07:00 AM", 30, "Morning hygiene and stretch"))
        items.add(PlannerItem("p2", "HEALTH", "Hydrate & Yoga / Exercise", "07:30 AM", 30, "Drink 500ml water + light morning yoga"))
        items.add(PlannerItem("p3", "HEALTH", "Healthy Breakfast", "08:00 AM", 30, "Nutritious breakfast to fuel your day"))

        if (isTiredMode) {
            // Light Schedule
            items.add(PlannerItem("p4", "STUDY", "Study Session: ${rotated[0]}", "09:30 AM", 45, "Focused core study session"))
            items.add(PlannerItem("p5", "PERSONAL", "Relaxation & Walk", "11:00 AM", 45, "Light break to recharge energy"))
            items.add(PlannerItem("p6", "HEALTH", "Lunch & Rest", "01:00 PM", 60, "Balanced lunch and midday rest"))
            items.add(PlannerItem("p7", "PROJECT", "Orbit AI Project Work", "03:00 PM", 60, "Light project task execution"))
            items.add(PlannerItem("p8", "AI_LEARNING", "10-Min Tech Article & Rest", "06:00 PM", 30, "Read 1 technical article"))
            items.add(PlannerItem("p9", "HEALTH", "Dinner & Relax", "08:00 PM", 60, "Evening dinner with family"))
            items.add(PlannerItem("p10", "REVIEW", "Daily Reflection & Sleep", "10:00 PM", 30, "Log progress and sleep schedule"))
        } else {
            // Standard Balanced Schedule
            items.add(PlannerItem("p4", "STUDY", "Study Session 1: ${rotated[0]}", "09:00 AM", 60, "Deep focus on technical questions & concepts"))
            items.add(PlannerItem("p5", "STUDY", "Study Session 2: ${rotated[1]}", "10:30 AM", 60, "Hands-on problem solving & exercises"))
            items.add(PlannerItem("p6", "PERSONAL", "Personal Time & Relaxation", "12:00 PM", 60, "Short break before lunch"))
            items.add(PlannerItem("p7", "HEALTH", "Lunch & Midday Rest", "01:00 PM", 60, "Healthy lunch and relaxation"))
            items.add(PlannerItem("p8", "STUDY", "Study Session 3: ${rotated[2]}", "02:30 PM", 60, "Core learning & coding practice"))
            items.add(PlannerItem("p9", "PROJECT", "Orbit AI Project Development", "04:00 PM", 90, "Build hands-on Android feature code"))
            items.add(PlannerItem("p10", "PERSONAL", "Evening Walk & Refreshment", "06:00 PM", 60, "Outdoor walk and leisure time"))
            items.add(PlannerItem("p11", "AI_LEARNING", "10-Min Tech Article & AI Insights", "07:15 PM", 30, "Read 1 relevant GenAI/Kotlin article"))
            items.add(PlannerItem("p12", "HEALTH", "Dinner & Unwind", "08:00 PM", 60, "Nutritious dinner and screen wind-down"))
            items.add(PlannerItem("p13", "REVIEW", "Daily Progress Review & Sleep", "09:30 PM", 30, "Review accomplishments and prepare for sleep"))
        }

        return DailyPlanReport(
            dateStr = dateDisplay,
            greeting = greeting,
            items = items,
            studySubjectsToday = rotated
        )
    }
}
