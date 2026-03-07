package com.example.das_gaztandegia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView; // IMPORTANTE: Para el logo
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SettingsActivity extends AppCompatActivity {

    // Variables globales
    private SharedPreferences misPreferencias;
    private ImageView imgLogoAjustes;

    // El "Lanzador" moderno para recibir el resultado de la galería
    private androidx.activity.result.ActivityResultLauncher<Intent> lanzadorGaleria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        misPreferencias = getSharedPreferences("AjustesGaztandegia", MODE_PRIVATE);

        /* =========================================================================
           NUEVO: PREPARAR EL LANZADOR DE LA GALERÍA
           Esto se queda escuchando hasta que el usuario elige una foto y vuelve.
           ========================================================================= */
        /* =========================================================================
           PREPARAR EL LANZADOR DE LA GALERÍA Y COPIAR LA IMAGEN
           Nota (StackOverflow): Copiamos la imagen a getFilesDir() para evitar el
           SecurityException. Si solo guardamos el URI, Android nos revoca el permiso
           de lectura tras reiniciar o reinstalar la app (Auto Backup).
           Ref: https://stackoverflow.com/questions/41457178/android-how-to-save-an-image-to-internal-storage
           ========================================================================= */
        lanzadorGaleria = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                new androidx.activity.result.ActivityResultCallback<androidx.activity.result.ActivityResult>() {
                    @Override
                    public void onActivityResult(androidx.activity.result.ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            android.net.Uri uriImagen = result.getData().getData();

                            if (uriImagen != null) {
                                try {
                                    // 1. Abrimos la imagen original de la galería
                                    java.io.InputStream inputStream = getContentResolver().openInputStream(uriImagen);
                                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);

                                    // 2. Creamos un archivo dentro de la memoria privada de nuestra app
                                    java.io.File directorio = getFilesDir();
                                    java.io.File archivoFoto = new java.io.File(directorio, "logo_queseria.png");

                                    // 3. Guardamos la copia exacta en nuestro archivo
                                    java.io.FileOutputStream outputStream = new java.io.FileOutputStream(archivoFoto);
                                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream);
                                    outputStream.flush();
                                    outputStream.close();

                                    // 4. Ponemos la foto en el circulito de Ajustes
                                    imgLogoAjustes.setImageBitmap(bitmap);

                                    // 5. Guardamos la RUTA DE NUESTRA COPIA PRIVADA en SharedPreferences
                                    SharedPreferences.Editor editor = misPreferencias.edit();
                                    editor.putString("RUTA_LOGO_QUESERIA", archivoFoto.getAbsolutePath());
                                    editor.apply();

                                } catch (Exception e) {
                                    Toast.makeText(SettingsActivity.this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });

        /* =========================================================================
           CONFIGURACIÓN: FOTO DEL LOGO
           ========================================================================= */
        imgLogoAjustes = findViewById(R.id.imgLogoAjustes);
        ImageButton btnCambiarLogo = findViewById(R.id.btnCambiarLogo);

        // 1. Al abrir la pantalla, comprobamos si ya había una foto guardada de antes
        String rutaFotoGuardada = misPreferencias.getString("RUTA_LOGO_QUESERIA", "");
        if (!rutaFotoGuardada.isEmpty() && imgLogoAjustes != null) {
            try {
                imgLogoAjustes.setImageURI(android.net.Uri.parse(rutaFotoGuardada));
            } catch (Exception e) {
                // Si el usuario ha borrado la foto de su galería, ignoramos el error y se quedará el logo por defecto
            }
        }

        // 2. Programamos el botón de la cámara para abrir la galería
        if (btnCambiarLogo != null) {
            btnCambiarLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /* =========================================================================
                       USO DE INTENT IMPLÍCITO
                       Como no sabemos qué app de galería usa el usuario, lanzamos una petición
                       general al sistema (ACTION_OPEN_DOCUMENT) pidiendo solo imágenes.
                       ========================================================================= */
                    Intent intentGaleria = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intentGaleria.addCategory(Intent.CATEGORY_OPENABLE);
                    intentGaleria.setType("image/*"); // Solo queremos fotos, no PDFs ni vídeos

                    lanzadorGaleria.launch(intentGaleria);
                }
            });
        }

        /* =========================================================================
           NAVEGACIÓN (Botón volver y barra inferior)
           ========================================================================= */
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
           CONFIGURACIÓN: PERFIL DE LA QUESERÍA (Nombre)
           ========================================================================= */
        TextInputEditText inputNombreQueseria = findViewById(R.id.inputNombreQueseria);
        MaterialButton btnGuardarPerfil = findViewById(R.id.btnGuardarPerfil);

        if (inputNombreQueseria != null) {
            inputNombreQueseria.setText(misPreferencias.getString("NOMBRE_QUESERIA", "Gaztandegia SL"));
        }

        if (btnGuardarPerfil != null && inputNombreQueseria != null) {
            btnGuardarPerfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nuevoNombre = inputNombreQueseria.getText().toString();
                    if (!nuevoNombre.isEmpty()) {
                        SharedPreferences.Editor editor = misPreferencias.edit();
                        editor.putString("NOMBRE_QUESERIA", nuevoNombre);
                        editor.apply();
                        Toast.makeText(SettingsActivity.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettingsActivity.this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        /* =========================================================================
           CONFIGURACIÓN: PREFERENCIAS (Switches e Idiomas)
           ========================================================================= */
        SwitchMaterial switchModoOscuro = findViewById(R.id.switchModoOscuro);
        SwitchMaterial switchNotificaciones = findViewById(R.id.switchNotificaciones);
        Spinner spinnerIdioma = findViewById(R.id.spinnerIdioma);

        // AQUÍ ESTÁ LA MAGIA DEL MODO OSCURO ACTUALIZADA
        if (switchModoOscuro != null) {
            switchModoOscuro.setChecked(misPreferencias.getBoolean("MODO_OSCURO", false));
            switchModoOscuro.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                    // 1. Guardamos la decisión en la memoria
                    misPreferencias.edit().putBoolean("MODO_OSCURO", isChecked).apply();

                    // 2. Le decimos a Android que cambie el tema de TODA la app al instante
                    if (isChecked) {
                        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                    }
                }
            });
        }

        if (switchNotificaciones != null) {
            switchNotificaciones.setChecked(misPreferencias.getBoolean("NOTIFICACIONES", true));
            switchNotificaciones.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                    misPreferencias.edit().putBoolean("NOTIFICACIONES", isChecked).apply();
                }
            });
        }

        if (spinnerIdioma != null) {
            String[] idiomas = {"Español", "Euskera", "English"};
            ArrayAdapter<String> adapterIdiomas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, idiomas);
            spinnerIdioma.setAdapter(adapterIdiomas);
        }

        /* =========================================================================
           CARGAR EL MANUAL DESDE RAW
           ========================================================================= */
        TextView tvTextoRaw = findViewById(R.id.tvTextoRaw);
        try {
            InputStream fraw = getResources().openRawResource(R.raw.manual);
            BufferedReader brin = new BufferedReader(new InputStreamReader(fraw));
            StringBuilder textoCompleto = new StringBuilder();
            String linea = brin.readLine();

            while (linea != null) {
                textoCompleto.append(linea).append("\n");
                linea = brin.readLine();
            }
            fraw.close();

            if (tvTextoRaw != null) {
                tvTextoRaw.setText(textoCompleto.toString());
            }
        } catch (Exception ex) {
            Toast.makeText(this, "Error al cargar el manual", Toast.LENGTH_SHORT).show();
        }

        /* =========================================================================
           CONFIGURACIÓN: BORRADO DE HISTORIAL (Botón Rojo)
           ========================================================================= */
        MaterialButton btnBorrarHistorial = findViewById(R.id.btnBorrarHistorial);
        if (btnBorrarHistorial != null) {
            btnBorrarHistorial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialogoAlerta = new AlertDialog.Builder(SettingsActivity.this);
                    dialogoAlerta.setTitle("¡PELIGRO!");
                    dialogoAlerta.setMessage("Vas a borrar de forma irreversible TODOS los lotes de la base de datos. ¿Estás absolutamente seguro?");

                    dialogoAlerta.setPositiveButton("SÍ, BORRAR TODO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DataBaseHelper gestorDB = new DataBaseHelper(SettingsActivity.this, "gaztandegia.db", null, 1);
                            gestorDB.borrarTodoElHistorial();
                            Toast.makeText(SettingsActivity.this, "¡Historial borrado por completo!", Toast.LENGTH_LONG).show();
                        }
                    });

                    dialogoAlerta.setNegativeButton("Cancelar", null);
                    dialogoAlerta.show();
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