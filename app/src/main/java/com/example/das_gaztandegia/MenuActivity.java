package com.example.das_gaztandegia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView; // IMPORTANTE: Añadido para poder modificar los textos

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

public class MenuActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Referencias del menú lateral (Drawer)
        drawerLayout = findViewById(R.id.drawerLayout);
        ImageButton btnMenuHamburguesa = findViewById(R.id.btnMenuHamburguesa);
        NavigationView navigationView = findViewById(R.id.navigationView);

        // Al pulsar la hamburguesa abrimos el panel
        btnMenuHamburguesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Listener para las opciones del navigation view
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                // Si le da a cerrar sesión
                if (id == R.id.nav_cerrar_sesion) {
                    // Intent explicito al login como pide la teoria
                    Intent intent = new Intent(MenuActivity.this, LoginActivity.class);

                    // Le meto el CLEAR_TOP para cargarme el historial y que no puedan darle a "atrás" y entrar sin pass
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                    finish(); // chapamos la activity actual
                }

                // Escondemos el menú después de hacer click
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // Referencias a los botones gordos del centro (Cards)
        MaterialCardView cardNuevoLote = findViewById(R.id.cardRegistrar);
        MaterialCardView cardVerLotes = findViewById(R.id.cardGestionar);
        MaterialCardView cardStats = findViewById(R.id.cardEstadisticas);
        MaterialCardView cardAjustes = findViewById(R.id.cardAjustes);

        // Cambiado a la versión extendida (sin lambdas)
        cardNuevoLote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irAPantalla(NuevoLoteActivity.class);
            }
        });

        cardVerLotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irAPantalla(ListActivity.class);
            }
        });

        cardStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irAPantalla(StatsActivity.class);
            }
        });

        cardAjustes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irAPantalla(SettingsActivity.class);
            }
        });
    }

    /* =========================================================================
       NUEVO: onResume() para actualizar el menú lateral al volver a esta pantalla
       ========================================================================= */
    @Override
    protected void onResume() {
        super.onResume();
        actualizarTextosDrawer();
    }

    /* =========================================================================
       NUEVO: Método que lee las preferencias y cambia los textos de la cabecera
       ========================================================================= */
    private void actualizarTextosDrawer() {
        NavigationView navigationView = findViewById(R.id.navigationView);

        // Comprobamos que el menú existe y que tiene una cabecera (Header)
        if (navigationView != null && navigationView.getHeaderCount() > 0) {
            View headerView = navigationView.getHeaderView(0);

            // Buscamos los TextViews DENTRO de la cabecera
            // OJO: Asegúrate de que estos IDs son los que tienes en tu archivo XML de la cabecera
            TextView tvNombreQueseria = headerView.findViewById(R.id.tvDrawerNombreQueseria);
            TextView tvNombreTrabajador = headerView.findViewById(R.id.tvDrawerNombreTrabajador);

            // 1. Leemos el nombre de la Quesería (que guardamos en SettingsActivity)
            SharedPreferences prefAjustes = getSharedPreferences("AjustesGaztandegia", MODE_PRIVATE);
            String nombreQueseria = prefAjustes.getString("NOMBRE_QUESERIA", "Gaztandegia SL");

            // 2. Leemos el nombre del Trabajador (que deberíamos haber guardado en LoginActivity)
            SharedPreferences prefUsuario = getSharedPreferences("MisPreferenciasQueseria", MODE_PRIVATE);
            String nombreTrabajador = prefUsuario.getString("NOMBRE_TRABAJADOR_ACTUAL", "Trabajador");

            // 3. Aplicamos los textos
            if (tvNombreQueseria != null) {
                tvNombreQueseria.setText(nombreQueseria);
            }
            if (tvNombreTrabajador != null) {
                tvNombreTrabajador.setText("Operario: " + nombreTrabajador);
            }
        }
    }

    private void irAPantalla(Class<?> claseDestino) {
        Intent intent = new Intent(MenuActivity.this, claseDestino);

        // Aplicando  CLEAR_TOP y SINGLE_TOP
        // para no abrir 500 veces la misma pantalla y reventar la memoria
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
    }
}