package com.example.das_gaztandegia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

        /* =========================================================================
           APLICAR MODO OSCURO (Protección por si la app se restaura en esta pantalla)
           ========================================================================= */
        SharedPreferences prefAjustes = getSharedPreferences("AjustesGaztandegia", MODE_PRIVATE);
        boolean modoOscuroActivado = prefAjustes.getBoolean("MODO_OSCURO", false);

        if (modoOscuroActivado) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

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
                    Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                    // Usamos CLEAR_TASK y NEW_TASK para borrar absolutamente todo el historial al cerrar sesión
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Chapamos la activity actual
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
       onResume() para actualizar el menú lateral al volver a esta pantalla
       ========================================================================= */
    @Override
    protected void onResume() {
        super.onResume();
        actualizarTextosDrawer();
    }

    /* =========================================================================
       Método que lee las preferencias y cambia los textos y la foto de la cabecera
       ========================================================================= */
    private void actualizarTextosDrawer() {
        NavigationView navigationView = findViewById(R.id.navigationView);

        if (navigationView != null && navigationView.getHeaderCount() > 0) {
            View headerView = navigationView.getHeaderView(0);

            // 1. Buscamos TODOS los elementos DENTRO del headerView
            TextView tvNombreQueseria = headerView.findViewById(R.id.tvDrawerNombreQueseria);
            TextView tvNombreTrabajador = headerView.findViewById(R.id.tvDrawerNombreTrabajador);
            ImageView imgLogoDrawer = headerView.findViewById(R.id.imgLogoDrawer);

            // 2. Leemos la memoria
            SharedPreferences prefAjustes = getSharedPreferences("AjustesGaztandegia", MODE_PRIVATE);
            String nombreQueseria = prefAjustes.getString("NOMBRE_QUESERIA", "Gaztandegia SL");
            String rutaFoto = prefAjustes.getString("RUTA_LOGO_QUESERIA", "");

            SharedPreferences prefUsuario = getSharedPreferences("MisPreferenciasQueseria", MODE_PRIVATE);
            String nombreTrabajador = prefUsuario.getString("NOMBRE_TRABAJADOR_ACTUAL", "Trabajador");

            // 3. Aplicamos los textos
            if (tvNombreQueseria != null) tvNombreQueseria.setText(nombreQueseria);
            if (tvNombreTrabajador != null) tvNombreTrabajador.setText("Operario: " + nombreTrabajador);

            /* =========================================================================
               4. CARGAMOS EL LOGO DE FORMA SEGURA (Anti-Crasheo)
               ========================================================================= */
            if (!rutaFoto.isEmpty() && imgLogoDrawer != null) {
                try {
                    java.io.File archivoLogo = new java.io.File(rutaFoto);
                    if (archivoLogo.exists()) {
                        android.graphics.Bitmap myBitmap = android.graphics.BitmapFactory.decodeFile(archivoLogo.getAbsolutePath());
                        imgLogoDrawer.setImageBitmap(myBitmap);
                    }
                } catch (Exception e) {
                    // Ignoramos el error silenciosamente
                }
            }
        }
    }

    /* =========================================================================
       CONTROL DE PILA: Navegación segura
       ========================================================================= */
    private void irAPantalla(Class<?> claseDestino) {
        Intent intent = new Intent(this, claseDestino);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}