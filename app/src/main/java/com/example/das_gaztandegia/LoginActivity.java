package com.example.das_gaztandegia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* =========================================================================
           APLICAR MODO OSCURO AL ARRANCAR LA APP
           Leemos las preferencias ANTES de cargar el diseño para que no haya parpadeos
           ========================================================================= */
        android.content.SharedPreferences prefAjustes = getSharedPreferences("AjustesGaztandegia", MODE_PRIVATE);
        boolean modoOscuroActivado = prefAjustes.getBoolean("MODO_OSCURO", false);

        if (modoOscuroActivado) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Una vez configurado el tema, ya podemos pintar la pantalla
        setContentView(R.layout.activity_login);

        // 1. Enlazamos las cajas de texto donde escribe el usuario
        EditText cajaEmail = findViewById(R.id.LogEmail);
        EditText cajaPassword = findViewById(R.id.LogContraseña);

        // 2. Enlazamos los botones
        Button btnAcceder = findViewById(R.id.accesBtn);
        Button btnRegistrar = findViewById(R.id.registerBtn);

        // 3. Programamos el clic del botón Acceder
        btnAcceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = cajaEmail.getText().toString();
                String password = cajaPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    DataBaseHelper gestorDB = new DataBaseHelper(LoginActivity.this, "gaztandegia.db", null, 1);

                    // La BD nos devuelve el ID del usuario
                    int idDelUsuarioLogueado = gestorDB.comprobarLogin(email, password);

                    if (idDelUsuarioLogueado != -1) { // Si es diferente a -1, es que existe

                        /* =========================================================================
                           Buscar el nombre en la BD y guardar ambos datos en SharedPreferences
                           ========================================================================= */
                        // Pedimos a la base de datos el nombre real de este usuario
                        String nombreDelTrabajador = gestorDB.obtenerNombreUsuario(idDelUsuarioLogueado);

                        // Abrimos las preferencias
                        android.content.SharedPreferences preferencias = getSharedPreferences("MisPreferenciasQueseria", MODE_PRIVATE);
                        android.content.SharedPreferences.Editor editor = preferencias.edit();

                        // Guardamos el ID (para enlazar los quesos) y el Nombre (para el menú visual)
                        editor.putInt("ID_TRABAJADOR_ACTUAL", idDelUsuarioLogueado);
                        editor.putString("NOMBRE_TRABAJADOR_ACTUAL", nombreDelTrabajador);
                        editor.apply(); // Guardamos los cambios

                        Toast.makeText(LoginActivity.this, "¡Bienvenido, " + nombreDelTrabajador + "!", Toast.LENGTH_SHORT).show();

                        // Usamos el nuevo método de navegación para saltar al menú
                        irAPantalla(MenuActivity.class);

                        // Cerramos el login para que no puedan darle a "Atrás" y volver aquí
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: Email o contraseña incorrectos", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        // 4. Programamos el clic del botón Registrarse
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Usamos el nuevo método de navegación para evitar abrir múltiples registros
                irAPantalla(RegisterActivity.class);
            }
        });

        /* =========================================================================
           CARGAR EL LOGO PERSONALIZADO (Si el usuario lo cambió en Ajustes)
           ========================================================================= */
        android.widget.ImageView imgLogoLogin = findViewById(R.id.LogAvatar);

        // La ruta de la foto ya se lee de prefAjustes (que instanciamos arriba del todo para el modo oscuro)
        String rutaFotoLogin = prefAjustes.getString("RUTA_LOGO_QUESERIA", "");

        if (!rutaFotoLogin.isEmpty() && imgLogoLogin != null) {
            try {
                imgLogoLogin.setImageURI(android.net.Uri.parse(rutaFotoLogin));
            } catch (Exception e) {
                // Ignoramos el error, se queda el quesito por defecto
            }
        }
    }

    /* =========================================================================
       NUEVO: Método para controlar la pila de actividades (Backstack)
       ========================================================================= */
    private void irAPantalla(Class<?> claseDestino) {
        Intent intent = new Intent(this, claseDestino);

        // Aplicando  CLEAR_TOP y SINGLE_TOP
        // para no abrir 500 veces la misma pantalla y reventar la memoria
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
    }
}