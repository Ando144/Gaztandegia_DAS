package com.example.das_gaztandegia;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.DatePicker;
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
import java.util.Calendar;
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

        if (navVerLotes != null) navVerLotes.setOnClickListener(v -> irAPantalla(ListActivity.class));
        if (navStats != null) navStats.setOnClickListener(v -> irAPantalla(StatsActivity.class));
        if (navAjustes != null) navAjustes.setOnClickListener(v -> irAPantalla(SettingsActivity.class));

        // 3. ENLAZAMOS LOS CAMPOS DEL FORMULARIO
        TextView txtIdLote = findViewById(R.id.txtIdLote);
        TextInputEditText inputFecha = findViewById(R.id.inputFecha);
        TextInputEditText inputTempPast = findViewById(R.id.inputTempPast);
        TextInputEditText inputTempCuajado = findViewById(R.id.inputTempCuajado);
        EditText inputTiempoManual = findViewById(R.id.inputTiempoManual);
        Slider sliderTiempo = findViewById(R.id.sliderTiempo);
        TextInputEditText inputPhCorte = findViewById(R.id.inputPhCorte);
        TextInputEditText inputPhFinal = findViewById(R.id.inputPhFinal);
        TextInputEditText inputObservaciones = findViewById(R.id.inputObservaciones);
        MaterialButton btnGuardarLote = findViewById(R.id.btnGuardarLote);

        /* =========================================================================
           NOVEDAD 1: CALENDARIO INTERACTIVO Y FECHA
           ========================================================================= */
        if (inputFecha != null) {
            // Ponemos la fecha de hoy por defecto
            String fechaHoy = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            inputFecha.setText(fechaHoy);

            // Ponemos un texto genérico en el título
            if (txtIdLote != null) {
                txtIdLote.setText("Creando Nuevo Lote...");
            }

            // Bloqueamos la caja para que no salga el teclado
            inputFecha.setFocusable(false);
            inputFecha.setClickable(true);

            // Al tocar la caja, abrimos el calendario
            inputFecha.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar calendario = Calendar.getInstance();
                    int anio = calendario.get(Calendar.YEAR);
                    int mes = calendario.get(Calendar.MONTH);
                    int dia = calendario.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog dialogoFecha = new DatePickerDialog(NuevoLoteActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            // Formateamos para que siempre sea dd/MM/yyyy
                            String fechaElegida = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, (month + 1), year);
                            inputFecha.setText(fechaElegida);
                        }
                    }, anio, mes, dia);

                    dialogoFecha.show();
                }
            });
        }

        /* =========================================================================
           4. SINCRONIZAR SLIDER Y CAJA DE TEXTO
           StackOverflow ref: https://stackoverflow.com/questions/28699668/updating-seekbar-after-changing-edittext
           ========================================================================= */
        if (sliderTiempo != null && inputTiempoManual != null) {
            sliderTiempo.addOnChangeListener(new Slider.OnChangeListener() {
                @Override
                public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                    if (fromUser) {
                        inputTiempoManual.setText(String.valueOf((int) value));
                    }
                }
            });

            inputTiempoManual.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().isEmpty()) {
                        try {
                            float valorElegido = Float.parseFloat(s.toString());
                            if (valorElegido >= sliderTiempo.getValueFrom() && valorElegido <= sliderTiempo.getValueTo()) {
                                sliderTiempo.setValue(valorElegido);
                            }
                        } catch (NumberFormatException e) {
                            // Ignoramos la excepción
                        }
                    }
                }
            });
        }

        // 5. PROGRAMAMOS EL BOTÓN DE GUARDAR
        if (btnGuardarLote != null) {
            btnGuardarLote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Sacamos los textos de TODAS las cajas de forma segura
                    String fecha = inputFecha != null ? inputFecha.getText().toString() : "";
                    String tempPastStr = inputTempPast != null ? inputTempPast.getText().toString() : "";
                    String tempCuajStr = inputTempCuajado != null ? inputTempCuajado.getText().toString() : "";
                    String tiempoStr = inputTiempoManual != null ? inputTiempoManual.getText().toString() : "";
                    String phCorteStr = inputPhCorte != null ? inputPhCorte.getText().toString() : "";
                    String phFinalStr = inputPhFinal != null ? inputPhFinal.getText().toString() : "";
                    String observaciones = inputObservaciones != null ? inputObservaciones.getText().toString() : "";

                    // Comprobación de que no hay campos vitales vacíos
                    if (fecha.isEmpty() || tempPastStr.isEmpty() || tempCuajStr.isEmpty() || tiempoStr.isEmpty() || phCorteStr.isEmpty() || phFinalStr.isEmpty()) {
                        Toast.makeText(NuevoLoteActivity.this, "Faltan campos por rellenar", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        // Convertimos los textos a los números que la BD exige
                        double tempPast = Double.parseDouble(tempPastStr);
                        double tempCuaj = Double.parseDouble(tempCuajStr);
                        int tiempo = Integer.parseInt(tiempoStr);
                        double phCorte = Double.parseDouble(phCorteStr);
                        double phFinal = Double.parseDouble(phFinalStr);

                        /* =========================================================================
                           NOVEDAD 2: ID ÚNICO BASADO EN EL TIEMPO
                           Así puedes crear múltiples quesos el mismo día sin que choquen en SQLite
                           ========================================================================= */
                        int idGenerado = (int) (System.currentTimeMillis() / 1000);

                        // --- LEEMOS EL ID DEL TRABAJADOR DE SHAREDPREFERENCES ---
                        android.content.SharedPreferences preferencias = getSharedPreferences("MisPreferenciasQueseria", MODE_PRIVATE);
                        int idTrabajadorReal = preferencias.getInt("ID_TRABAJADOR_ACTUAL", 1);

                        // Conectamos con la BD
                        DataBaseHelper gestorDB = new DataBaseHelper(NuevoLoteActivity.this, "gaztandegia.db", null, 1);

                        // Metemos todos los datos bien ordenados y convertidos
                        boolean exito = gestorDB.insertarLote(
                                idGenerado,
                                fecha,
                                tempPast,
                                tempCuaj,
                                tiempo,
                                phCorte,
                                phFinal,
                                observaciones,
                                idTrabajadorReal
                        );

                        if (exito) {
                            Toast.makeText(NuevoLoteActivity.this, "¡Queso guardado con éxito!", Toast.LENGTH_SHORT).show();

                            // Si todo va bien, viajamos a la lista
                            irAPantalla(ListActivity.class);
                        } else {
                            Toast.makeText(NuevoLoteActivity.this, "Error de base de datos al guardar", Toast.LENGTH_LONG).show();
                        }

                    } catch (NumberFormatException e) {
                        // Por si el usuario ha puesto comas en vez de puntos en los decimales y el Double.parseDouble falla
                        Toast.makeText(NuevoLoteActivity.this, "Error: Usa puntos para los decimales (Ej: 5.2)", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    // Método para navegar
    private void irAPantalla(Class<?> claseDestino) {
        Intent intent = new Intent(this, claseDestino);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}