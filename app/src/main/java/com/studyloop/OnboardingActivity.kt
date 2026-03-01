package com.studyloop

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.fragment.app.Fragment

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var btnSkip: Button
    private lateinit var dotsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Skip onboarding if already seen
        val prefs = getSharedPreferences("studyloop_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("onboarding_done", false)) {
            goToMain(); return
        }

        setContentView(R.layout.activity_onboarding)
        viewPager    = findViewById(R.id.viewPager)
        btnNext      = findViewById(R.id.btnNext)
        btnSkip      = findViewById(R.id.btnSkip)
        dotsLayout   = findViewById(R.id.dotsLayout)

        viewPager.adapter = OnboardingAdapter(this)
        setupDots(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setupDots(position)
                when (position) {
                    2 -> { btnNext.text = "Sign in with Google"; btnSkip.text = "Use Offline" }
                    else -> { btnNext.text = "Next →"; btnSkip.text = "Skip" }
                }
            }
        })

        btnNext.setOnClickListener {
            if (viewPager.currentItem < 2) {
                viewPager.currentItem += 1
            } else {
                finishOnboarding(true)
            }
        }

        btnSkip.setOnClickListener { finishOnboarding(false) }
    }

    private fun setupDots(position: Int) {
        dotsLayout.removeAllViews()
        repeat(3) { i ->
            val dot = View(this).apply {
                val size = if (i == position) 24 else 16
                layoutParams = LinearLayout.LayoutParams(
                    (size * resources.displayMetrics.density).toInt(),
                    (8 * resources.displayMetrics.density).toInt()
                ).apply { marginEnd = (6 * resources.displayMetrics.density).toInt() }
                background = ContextCompat.getDrawable(context,
                    if (i == position) R.drawable.dot_active else R.drawable.dot_inactive)
            }
            dotsLayout.addView(dot)
        }
    }

    private fun finishOnboarding(signIn: Boolean) {
        getSharedPreferences("studyloop_prefs", MODE_PRIVATE)
            .edit().putBoolean("onboarding_done", true).apply()
        goToMain()
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

class OnboardingAdapter(activity: OnboardingActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 3
    override fun createFragment(position: Int) = OnboardingSlide.newInstance(position)
}

class OnboardingSlide : Fragment() {
    companion object {
        fun newInstance(position: Int) = OnboardingSlide().apply {
            arguments = Bundle().apply { putInt("position", position) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_onboarding_slide, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val position = arguments?.getInt("position") ?: 0
        val tvEmoji   = view.findViewById<TextView>(R.id.tv_slide_emoji)
        val tvTitle   = view.findViewById<TextView>(R.id.tv_slide_title)
        val tvDesc    = view.findViewById<TextView>(R.id.tv_slide_desc)
        val tvBullets = view.findViewById<TextView>(R.id.tv_slide_bullets)

        when (position) {
            0 -> {
                tvEmoji.text  = "📚"
                tvTitle.text  = "Welcome to StudyLoop"
                tvDesc.text   = "Your personal memory coach — science-backed spaced repetition helps you remember everything longer with less effort."
                tvBullets.text = "⏰ Smart alarms\n🧠 Spaced reminders\n📝 Color-coded notes\n✅ To-do tracker\n🧮 Scientific calculator"
            }
            1 -> {
                tvEmoji.text  = "📉"
                tvTitle.text  = "Beat the Forgetting Curve"
                tvDesc.text   = "Without review, you forget 70% of new information within 24 hours. StudyLoop fights back."
                tvBullets.text = "📅 Reviews at Day 1 · 3 · 7 · 14 · 30 · 60 · 120\n📊 Live retention graph\n🔔 Smart notifications\n🔥 Streak tracking"
            }
            2 -> {
                tvEmoji.text  = "☁️"
                tvTitle.text  = "Never Lose Your Progress"
                tvDesc.text   = "Sign in with Google to protect everything you learn — forever."
                tvBullets.text = "✅ Auto cloud backup\n✅ Restore on any device\n✅ 7-day sync (watch 2 short ads)\n✅ Your streak protected\n✅ Free — no subscription"
            }
        }
    }
}
