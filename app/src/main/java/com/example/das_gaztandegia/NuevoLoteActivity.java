package com.example.das_gaztandegia;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

        // 3. ENLAZAMOS LOS CAMPOS DEL FORMULARIO
        TextView txtIdLote = findViewById(R.id.txtIdLote); // Para el título superior
        TextInputEditText inputFecha = findViewById(R.id.inputFecha);
        TextInputEditText inputTempCuajado = findViewById(R.id.inputTempCuajado);
        EditText inputTiempoManual = findViewById(R.id.inputTiempoManual);
        Slider sliderTiempo = findViewById(R.id.sliderTiempo); // La barra deslizante
        TextInputEditText inputPhFinal = findViewById(R.id.inputPhFinal);
        TextInputEditText inputObservaciones = findViewById(R.id.inputObservaciones);
        MaterialButton btnGuardarLote = findViewById(R.id.btnGuardarLote);

        // --- NOVEDAD 1: FECHA AUTOMÁTICA ---
        // Sacamos la fecha actual en formato dd/MM/yyyy
        String fechaHoy = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        if (inputFecha != null) {
            inputFecha.setText(fechaHoy); // La ponemos por defecto

            // Si el TextView del título existe, lo actualizamos para que se vea el ID
            if (txtIdLote != null) {
                txtIdLote.setText("Lote ID: " + fechaHoy.replace("/", ""));
            }
        }

        /* =========================================================================
           4. SINCRONIZAR SLIDER Y CAJA DE TEXTO
           Nota: Para hacer esto me he basado en la mayoria en la respuesta de estos usuarios en
           StackOverflow, me entraba en un bucle infinito y encontre esta solucion:
           https://stackoverflow.com/questions/28699668/updating-seekbar-after-changing-edittext
           (Adaptado de la solución de los usuarios user2440306 y Piotr Poprawski)
           ========================================================================= */
        if (sliderTiempo != null && inputTiempoManual != null) {

            // A. Cuando el usuario mueve la barra, actualizamos el texto
            sliderTiempo.addOnChangeListener(new Slider.OnChangeListener() {
                @Override
                public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                    // El "fromUser" (propuesto en el post de SO) evita un bucle infinito recursivo
                    if (fromUser) {
                        inputTiempoManual.setText(String.valueOf((int) value));
                    }
                }
            });

            // B. Cuando el usuario escribe manualmente en la caja, movemos la barra
            inputTiempoManual.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().isEmpty()) {
                        try { // Try-catch sacado de StackOverflow para evitar el NumberFormatException
                            float valorElegido = Float.parseFloat(s.toString());
                            if (valorElegido >= sliderTiempo.getValueFrom() && valorElegido <= sliderTiempo.getValueTo()) {
                                sliderTiempo.setValue(valorElegido);
                            }
                        } catch (NumberFormatException e) {
                            // Ignoramos la excepción si el usuario borra todo el texto de la caja
                        }
                    }
                }
            });
        }

        // 4. PROGRAMAMOS EL BOTÓN DE GUARDAR
        if (btnGuardarLote != null) {
            btnGuardarLote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Sacamos los textos de las cajas
                    String fecha = inputFecha.getText().toString();
                    String tempStr = inputTempCuajado.getText().toString();
                    String tiempoStr = inputTiempoManual.getText().toString();
                    String phStr = inputPhFinal.getText().toString();
                    String nota = inputObservaciones.getText().toString();

                    // Comprobación súper básica para que la app no explote
                    if (tempStr.isEmpty() || phStr.isEmpty() || tiempoStr.isEmpty() || fecha.isEmpty()) {
                        Toast.makeText(NuevoLoteActivity.this, "Faltan campos por rellenar", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Convertimos los textos a números
                    double temperatura = Double.parseDouble(tempStr);
                    int tiempo = Integer.parseInt(tiempoStr);
                    double ph = Double.parseDouble(phStr);

                    // --- NOVEDAD 3: EL ID DE LA FECHA ---
                    // Transformamos "04/03/2026" en el número 4032026 quitando las barras
                    String fechaSinBarras = fecha.replace("/", "");
                    int idGenerado = Integer.parseInt(fechaSinBarras);

                    // Conectamos con la Base de Datos
                    DataBaseHelper gestorDB = new DataBaseHelper(NuevoLoteActivity.this, "gaztandegia.db", null, 1);

                    // OJO: Le pasamos el "idGenerado" como primer parámetro al nuevo método de DataBaseHelper
                    boolean exito = gestorDB.insertarLote(idGenerado, fecha, temperatura, tiempo, ph, nota, 1);

                    if (exito) {
                        Toast.makeText(NuevoLoteActivity.this, "¡Queso guardado con ID " + idGenerado + "!", Toast.LENGTH_SHORT).show();

                        // Si todo ha ido bien, ¡viajamos automáticamente a la Lista para ver si aparece!
                        irAPantalla(ListActivity.class);
                    } else {
                        // Si devuelve false, probablemente sea porque ese ID (esa fecha) ya existe en SQLite
                        Toast.makeText(NuevoLoteActivity.this, "Error: Ya existe un lote con esta fecha", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    } // <-- AQUÍ TERMINA EL ONCREATE.

    // Método para navegar
    private void irAPantalla(Class<?> claseDestino) {
        Intent intent = new Intent(this, claseDestino);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}