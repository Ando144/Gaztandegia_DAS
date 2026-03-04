package com.example.das_gaztandegia;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Enlazamos las cajas de texto
        EditText cajaNombre = findViewById(R.id.RegName);
        EditText cajaEmail = findViewById(R.id.RegEmail);
        EditText cajaPassword = findViewById(R.id.RegPassword);
        EditText cajaRepPassword = findViewById(R.id.RegRepPassword);

        // 2. Enlazamos los botones
        Button btnRegistrar = findViewById(R.id.RegAccesBtn);
        Button btnVolverLogin = findViewById(R.id.RegLogginBtn);

        // 3. Programamos el botón de Registrarse (RegAccesBtn)
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sacamos lo que ha escrito el usuario
                String nombre = cajaNombre.getText().toString();
                String email = cajaEmail.getText().toString();
                String password = cajaPassword.getText().toString();
                String repPassword = cajaRepPassword.getText().toString();

                // Validación 1: Que no haya campos vacíos
                if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || repPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
                }
                // Validación 2: Que las contraseñas coincidan
                else if (!password.equals(repPassword)) {
                    Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                }
                else {
                    // --- AQUÍ EMPIEZA LA CONEXIÓN CON LA BASE DE DATOS ---

                    // Instanciamos nuestra base de datos
                    DataBaseHelper gestorDB = new DataBaseHelper(RegisterActivity.this, "gaztandegia.db", null, 1);

                    // Llamamos al método que inserta al usuario y guardamos si ha tenido éxito (true) o no (false)
                    boolean exito = gestorDB.insertarUsuario(nombre, email, password);

                    if (exito) {
                        Toast.makeText(RegisterActivity.this, "¡Registro completado con éxito!", Toast.LENGTH_SHORT).show();
                        // Como ya se ha registrado, destruimos esta pantalla para devolverle al Login
                        finish();
                    } else {
                        // Si devuelve false, es porque SQLite ha rechazado el insert (probablemente email repetido)
                        Toast.makeText(RegisterActivity.this, "Error: Ya existe una cuenta con este email", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        // 4. Programamos el botón de "Log in" (por si ya tiene cuenta)
        btnVolverLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Como venimos de la pantalla de Login, la forma más limpia
                // de volver sin amontonar pantallas es simplemente cerrar esta (Pila LIFO)
                finish();
            }
        });
    }
}