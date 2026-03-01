package com.studyloop

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.AdRequest
import com.studyloop.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigation
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController
        binding.bottomNav.setupWithNavController(navController)

        // AdMob banner
        binding.adView.loadAd(AdRequest.Builder().build())

        // Handle deep-link from notification
        intent?.getStringExtra("TAB")?.let { tab ->
            when (tab) {
                "alarm"    -> binding.bottomNav.selectedItemId = R.id.nav_alarm
                "reminder" -> binding.bottomNav.selectedItemId = R.id.nav_reminder
            }
        }
    }
}
