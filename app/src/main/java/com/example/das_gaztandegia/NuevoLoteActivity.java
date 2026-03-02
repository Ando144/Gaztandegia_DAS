package com.example.das_gaztandegia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class NuevoLoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lotecreation); // El XML de crear lote

        // 1. Botón de volver al menú (Flecha arriba a la izquierda)
        ImageButton btnVolver = findViewById(R.id.btnCerrarSesion);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    irAPantalla(MenuActivity.class);
                }
            });
        }

        // 2. Barra inferior (Bottom Navigation)
        LinearLayout navVerLotes = findViewById(R.id.navVerLotes);
        LinearLayout navStats = findViewById(R.id.navEstadisticas);
        LinearLayout navAjustes = findViewById(R.id.navAjustes);

        if (navVerLotes != null) {
            navVerLotes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    irAPantalla(ListActivity.class);
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
    }

    // Método para navegar (Aplicando teoría diapo 32)
    private void irAPantalla(Class<?> claseDestino) {
        Intent intent = new Intent(this, claseDestino);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}