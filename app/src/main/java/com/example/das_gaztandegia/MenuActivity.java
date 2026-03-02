package com.example.das_gaztandegia;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

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


        cardNuevoLote.setOnClickListener(v -> irAPantalla(NuevoLoteActivity.class));
        cardVerLotes.setOnClickListener(v -> irAPantalla(ListActivity.class));
        cardStats.setOnClickListener(v -> irAPantalla(StatsActivity.class));
        cardAjustes.setOnClickListener(v -> irAPantalla(SettingsActivity.class));
    }

    private void irAPantalla(Class<?> claseDestino) {
        Intent intent = new Intent(MenuActivity.this, claseDestino);

        // Aplicando  CLEAR_TOP y SINGLE_TOP
        // para no abrir 500 veces la misma pantalla y reventar la memoria
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
    }
}