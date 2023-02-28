package com.sqooid.vult

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.sqooid.vult.client.ISyncClient
import com.sqooid.vult.preferences.IPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.time.Duration

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var preferences: IPreferences
    @Inject
    lateinit var syncClient: ISyncClient

    var autoLockJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initApp()

        // Set secure window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        Log.d("app", "Created activity")
    }

    override fun onTopResumedActivityChanged(isTopResumedActivity: Boolean) {
        if (!isTopResumedActivity) {
            autoLockJob = lifecycleScope.launch(Dispatchers.Default) {
                delay(Duration.parse("3s"))
                lifecycleScope.launch(Dispatchers.Main) {
                    initApp()
                }
            }
        } else {
            autoLockJob?.cancel()
        }
    }

    private fun initApp() {
        // Go to login page if have account
        val hash = preferences.loginHash
        if (hash.isNotEmpty()) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
            val inflater = navHostFragment.navController.navInflater
            val graph = inflater.inflate(R.navigation.nav_graph)
            graph.setStartDestination(R.id.login)
            navHostFragment.navController.graph = graph
        }
    }
}