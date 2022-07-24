package com.sqooid.vult

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Go to login page if have account
        val hash = getSharedPreferences(Vals.SHARED_PREF_FILE, Context.MODE_PRIVATE).getString(Vals.HASH_KEY, "")
        if (hash != null && hash.isNotEmpty()) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
            val inflater = navHostFragment.navController.navInflater
            val graph = inflater.inflate(R.navigation.nav_graph)
            graph.setStartDestination(R.id.login)
            navHostFragment.navController.graph = graph
        }
    }
}