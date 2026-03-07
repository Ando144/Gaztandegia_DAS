package com.example.das_gaztandegia;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.content.SharedPreferences prefAjustes = getSharedPreferences("AjustesGaztandegia", MODE_PRIVATE);
        boolean modoOscuroActivado = prefAjustes.getBoolean("MODO_OSCURO", false);

        if (modoOscuroActivado) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_login);

        /* =========================================================================
           NUEVO: CREAR EL CANAL DE NOTIFICACIONES
           Lo creamos nada más arrancar la app.
           ========================================================================= */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel canal = new android.app.NotificationChannel(
                    "canal_gaztandegia",
                    "Notificaciones de la Quesería",
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
            );
            canal.setDescription("Avisos sobre nuevos lotes y puntuaciones pendientes");
            android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(canal);
        }

        EditText cajaEmail = findViewById(R.id.LogEmail);
        EditText cajaPassword = findViewById(R.id.LogContraseña);
        Button btnAcceder = findViewById(R.id.accesBtn);
        Button btnRegistrar = findViewById(R.id.registerBtn);

        btnAcceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = cajaEmail.getText().toString();
                String password = cajaPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    DataBaseHelper gestorDB = new DataBaseHelper(LoginActivity.this, "gaztandegia.db", null, 1);
                    int idDelUsuarioLogueado = gestorDB.comprobarLogin(email, password);

                    if (idDelUsuarioLogueado != -1) {
                        String nombreDelTrabajador = gestorDB.obtenerNombreUsuario(idDelUsuarioLogueado);

                        android.content.SharedPreferences preferencias = getSharedPreferences("MisPreferenciasQueseria", MODE_PRIVATE);
                        android.content.SharedPreferences.Editor editor = preferencias.edit();
                        editor.putInt("ID_TRABAJADOR_ACTUAL", idDelUsuarioLogueado);
                        editor.putString("NOMBRE_TRABAJADOR_ACTUAL", nombreDelTrabajador);
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "¡Bienvenido, " + nombreDelTrabajador + "!", Toast.LENGTH_SHORT).show();

                        /* =========================================================================
                           NUEVO: NOTIFICACIÓN DE LOTES SIN PUNTUAR AL ENTRAR
                           ========================================================================= */
                        // 1. Comprobamos si el usuario tiene las notificaciones activadas en Ajustes
                        if (prefAjustes.getBoolean("NOTIFICACIONES", true)) {
                            int lotesPendientes = gestorDB.contarLotesSinPuntuar();

                            if (lotesPendientes > 0) {
                                // Construimos y lanzamos la notificación
                                try {
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(LoginActivity.this, "canal_gaztandegia")
                                            .setSmallIcon(android.R.drawable.ic_dialog_info) // Icono por defecto de Android
                                            .setContentTitle("¡Tienes tareas pendientes!")
                                            .setContentText("Hay " + lotesPendientes + " lote(s) de queso sin puntuar en la bodega.")
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setAutoCancel(true);

                                    NotificationManagerCompat managerNotif = NotificationManagerCompat.from(LoginActivity.this);
                                    managerNotif.notify(1, builder.build()); // El 1 es el ID de esta notificación
                                } catch (SecurityException e) {
                                    // Si el usuario denegó el permiso, simplemente no hacemos nada
                                }
                            }
                        }

                        // Usamos tu método personalizado para navegar
                        irAPantalla(MenuActivity.class);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: Email o contraseña incorrectos", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Usamos tu método personalizado para evitar múltiples pantallas
                irAPantalla(RegisterActivity.class);
            }
        });

        /* =========================================================================
           CARGAR EL LOGO PERSONALIZADO (Desde la memoria interna)
           Nota (StackOverflow): Usamos BitmapFactory.decodeFile() en lugar de setImageURI()
           para evitar SecurityException con el Auto Backup de Google Drive.
           ========================================================================= */
        android.widget.ImageView imgLogoLogin = findViewById(R.id.LogAvatar);
        String rutaFotoLogin = prefAjustes.getString("RUTA_LOGO_QUESERIA", "");

        if (!rutaFotoLogin.isEmpty() && imgLogoLogin != null) {
            try {
                java.io.File archivoLogo = new java.io.File(rutaFotoLogin);
                // Si la copia de la foto existe, la ponemos. Si no, dejamos la del XML
                if (archivoLogo.exists()) {
                    android.graphics.Bitmap myBitmap = android.graphics.BitmapFactory.decodeFile(archivoLogo.getAbsolutePath());
                    imgLogoLogin.setImageBitmap(myBitmap);
                }
            } catch (Exception e) {
                // Ignoramos el error silenciosamente
            }
        }
    }

    /* =========================================================================
       CONTROL DE PILA: Navegación segura
       ========================================================================= */
    private void irAPantalla(Class<?> claseDestino) {
        Intent intent = new Intent(this, claseDestino);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}