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

        // TODO: Aquí meteremos la lógica de Fragments cuando conectemos la BBDD
    }

    private void irAPantalla(Class<?> claseDestino) {
        Intent intent = new Intent(this, claseDestino);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}