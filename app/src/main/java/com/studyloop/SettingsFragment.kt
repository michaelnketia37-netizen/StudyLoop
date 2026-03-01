package com.studyloop

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.studyloop.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Share app
        binding.btnShareApp.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Check out StudyLoop!")
                putExtra(Intent.EXTRA_TEXT,
                    "📚 StudyLoop — Remember everything with spaced repetition!\n\n" +
                    "✅ Smart review reminders\n" +
                    "📉 Forgetting curve visualization\n" +
                    "🧮 Scientific calculator\n" +
                    "☁️ Cloud backup\n\n" +
                    "Download free on Google Play:\n" +
                    "https://play.google.com/store/apps/details?id=com.studyloop"
                )
            }
            startActivity(Intent.createChooser(intent, "Share StudyLoop"))
        }

        // Privacy Policy
        binding.btnPrivacy.setOnClickListener {
            openUrl("https://sites.google.com/view/studyloop-legal/privacy")
        }

        // Terms of Use
        binding.btnTerms.setOnClickListener {
            openUrl("https://sites.google.com/view/studyloop-legal/terms")
        }

        // Rate app
        binding.btnRate.setOnClickListener {
            openUrl("https://play.google.com/store/apps/details?id=com.studyloop")
        }

        // App version
        binding.tvVersion.text = "StudyLoop v1.0 · Built by Michael · Ghana"
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Browser not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
