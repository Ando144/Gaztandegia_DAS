package com.example.das_gaztandegia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView; // IMPORTANTE: Añadido para el texto
import android.widget.Toast;    // IMPORTANTE: Añadido para el error

import androidx.appcompat.app.AppCompatActivity;

// IMPORTANTE: Librerías para leer ficheros
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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

        /* =========================================================================
           NUEVO: CARGAR EL MANUAL DESDE RAW
           ========================================================================= */
        TextView tvTextoRaw = findViewById(R.id.tvTextoRaw);

        /* =========================================================================
           LEER FICHERO DE TEXTO DESDE RES/RAW
           Nota: La lectura eficiente de un InputStream en Android usando BufferedReader
           y StringBuilder está basada en este hilo de StackOverflow:
           https://stackoverflow.com/questions/15912825/how-to-read-file-from-res-raw-by-name
           ========================================================================= */
        try {
            // Abrimos el archivo res/raw/manual.txt
            InputStream fraw = getResources().openRawResource(R.raw.manual);
            BufferedReader brin = new BufferedReader(new InputStreamReader(fraw));

            // Usamos StringBuilder para concatenar las líneas sin sobrecargar la memoria
            StringBuilder textoCompleto = new StringBuilder();
            String linea = brin.readLine();

            while (linea != null) {
                textoCompleto.append(linea).append("\n");
                linea = brin.readLine();
            }
            fraw.close();

            // Metemos el texto en la pantalla
            if (tvTextoRaw != null) {
                tvTextoRaw.setText(textoCompleto.toString());
            }
        } catch (Exception ex) {
            // Si el fichero no se encuentra o hay otro error, avisamos sin que crasheee
            Toast.makeText(this, "Error al cargar el manual", Toast.LENGTH_SHORT).show();
        }
    }

    private void irAPantalla(Class<?> claseDestino) {
        Intent intent = new Intent(this, claseDestino);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}