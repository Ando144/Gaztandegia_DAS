package com.example.das_gaztandegia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton btnVolver = findViewById(R.id.btnCerrarSesion);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    irAPantalla(MenuActivity.class);
                }
            });
        }

        LinearLayout navNuevoLote = findViewById(R.id.navNuevoLote);
        LinearLayout navVerLotes = findViewById(R.id.navVerLotes);
        LinearLayout navStats = findViewById(R.id.navEstadisticas);

        if (navNuevoLote != null) {
            navNuevoLote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    irAPantalla(NuevoLoteActivity.class);
                }
            });
        }

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
    }

    private void irAPantalla(Class<?> claseDestino) {
        Intent intent = new Intent(this, claseDestino);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}