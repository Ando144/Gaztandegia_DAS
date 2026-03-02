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
                // Sacamos el texto que ha escrito
                String email = cajaEmail.getText().toString();
                String password = cajaPassword.getText().toString();

                // Validación básica por ahora (luego meteremos aquí lo de SQLite)
                if (email.isEmpty() || password.isEmpty()) {
                    // Mensajito flotante para avisar de que falta algo
                    Toast.makeText(LoginActivity.this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    // Intent explícito para ir al menú principal (Teoría diapo 12)
                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    startActivity(intent);

                    // ¡OBLIGATORIO! Matamos la Activity de login.
                    // Así el usuario no puede darle al botón "Atrás" del móvil para entrar de nuevo.
                    finish();
                }
            }
        });

        // 4. Programamos el clic del botón Registrarse
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Veo en tus archivos que tienes un activity_register.xml, así que viajaremos ahí
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

                // OJO: Aquí NO ponemos finish(). Si el usuario se arrepiente de registrarse
                // sí que queremos que pueda darle a "Atrás" y volver a este Login.
            }
        });
    }
}