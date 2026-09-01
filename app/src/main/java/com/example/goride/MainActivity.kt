package com.example.goride

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.goride.data.repository.UserRepository
/**
 * Unica Activity dell'app (Lez. 2.4).
 *
 * Non contiene logica: ospita il NavHost e collega la barra di
 * navigazione al NavController. Tutte le schermate sono Fragment.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // findFragmentById invece di findNavController(): in onCreate
        // il NavController potrebbe non essere ancora pronto
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        // Se c'e' gia' una sessione, andiamo diritti alla mappa
        if (UserRepository().isLoggato()) {
            navController.navigate(
                R.id.mapFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.loginFragment, true)
                    .build()
            )
        }

        // Lez. 2.4 - collegamento automatico: gli id del menu
        // coincidono con quelli delle destinazioni
        findViewById<BottomNavigationView>(R.id.bottom_nav)
            .setupWithNavController(navController)
        // La barra compare solo nelle schermate principali
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
            bottomNav.visibility = when (destination.id) {
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.forgotPasswordFragment -> android.view.View.GONE
                else -> android.view.View.VISIBLE
            }
        }
    }
}