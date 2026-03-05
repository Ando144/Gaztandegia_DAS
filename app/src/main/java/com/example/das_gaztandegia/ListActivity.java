package com.example.das_gaztandegia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // 1. Botón de volver al menú (Flecha inteligente)
        ImageButton btnVolver = findViewById(R.id.btnCerrarSesion);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /* =========================================================================
                       LÓGICA DE LA FLECHA DE VOLVER CON FRAGMENTS
                       Nota: Como la flecha normal me sacaba al menú principal aunque estuviera
                       viendo un detalle de un queso, he buscado como cerrar solo el fragmento actual.
                      En  StackOverflow dice que hay que contar la "pila" de fragmentos (BackStack)
                       y hacer un "pop" para cerrarlo.
                       Fuente: https://stackoverflow.com/questions/52217114/adding-back-button-to-fragment
                       ========================================================================= */
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack(); // Cierra el detalle y vuelve a la lista
                    } else {
                        irAPantalla(MenuActivity.class); // Viaja al menú principal
                    }
                }
            });
        }

        LinearLayout navNuevoLote = findViewById(R.id.navNuevoLote);
        LinearLayout navStats = findViewById(R.id.navEstadisticas);
        LinearLayout navAjustes = findViewById(R.id.navAjustes);

        if (navNuevoLote != null) {
            navNuevoLote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    irAPantalla(NuevoLoteActivity.class);
                }
            });
        }

        if (navStats != null) {
            navStats.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    irAPantalla(StatsActivity.class);
                }
            });
        }

        if (navAjustes != null) {
            navAjustes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    irAPantalla(SettingsActivity.class);
                }
            });
        }

        /* =========================================================================
           LÓGICA DE FRAGMENTS (MAESTRO - DETALLE)
           ========================================================================= */
        if (savedInstanceState == null) {

            // 1. Siempre cargamos la lista en el hueco principal (maestro)
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contenedor_maestro, new ListaLoteFragments())
                    .commit();

            // 2. Comprobamos si el móvil está en horizontal buscando el contenedor derecho
            View huecoDerecho = findViewById(R.id.contenedor_detalle);
            if (huecoDerecho != null) {
                // Si existe, metemos el fragmento vacío de inicio para que no se vea en blanco
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contenedor_detalle, new FragmentVacio())
                        .commit();
            }
        }
    }

    private void irAPantalla(Class<?> claseDestino) {
        Intent intent = new Intent(this, claseDestino);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}