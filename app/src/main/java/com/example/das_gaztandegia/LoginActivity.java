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

                    // Ahora la BD nos devuelve un número entero
                    int idDelUsuarioLogueado = gestorDB.comprobarLogin(email, password);

                    if (idDelUsuarioLogueado != -1) { // Si es diferente a -1, es que existe

                        // --- AQUÍ USAMOS LAS SHARED o esas PARA GUARDAR EL ID DEL TRABAJADOR ACTUAL ---
                        android.content.SharedPreferences preferencias = getSharedPreferences("MisPreferenciasQueseria", MODE_PRIVATE);
                        android.content.SharedPreferences.Editor editor = preferencias.edit();
                        editor.putInt("ID_TRABAJADOR_ACTUAL", idDelUsuarioLogueado);
                        editor.apply(); // Guardamos los cambios

                        Toast.makeText(LoginActivity.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                        startActivity(intent);
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
                // Viajamos a la pantalla de registro
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

            }
        });
    }
}