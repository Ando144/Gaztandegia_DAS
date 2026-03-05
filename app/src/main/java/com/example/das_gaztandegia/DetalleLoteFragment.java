package com.example.das_gaztandegia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class DetalleLoteFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Enlazamos este Java con tu diseño XML real
        View view = inflater.inflate(R.layout.fragment_detalle_lote, container, false);

        // 1. Buscamos TODOS los componentes por sus IDs exactos del XML
        TextView txtId = view.findViewById(R.id.txtDetalleId);
        TextView txtFecha = view.findViewById(R.id.txtDetalleFecha);
        TextView txtTempPast = view.findViewById(R.id.txtDetalleTempPast);
        TextView txtTempCuaj = view.findViewById(R.id.txtDetalleTempCuaj);
        TextView txtTiempoCuaj = view.findViewById(R.id.txtDetalleTiempoCuaj);
        TextView txtPhCorte = view.findViewById(R.id.txtDetallePhCorte);
        TextView txtPhFinal = view.findViewById(R.id.txtDetallePhFinal);
        TextView txtObs = view.findViewById(R.id.txtDetalleObs);

        RatingBar ratingCalidad = view.findViewById(R.id.ratingCalidad);
        MaterialButton btnGuardarNota = view.findViewById(R.id.btnGuardarNota);

        // 2. Recuperamos el ID del queso que nos manda la lista
        int idLote = -1;
        if (getArguments() != null) {
            idLote = getArguments().getInt("ID_LOTE", -1);
        }

        // Guardamos el ID en una variable final para poder usarla dentro del botón luego
        final int idLoteFinal = idLote;

        // 3. Si el ID es válido, cargamos los datos de la base de datos
        if (idLote != -1) {
            DataBaseHelper gestorDB = new DataBaseHelper(getContext(), "gaztandegia.db", null, 1);
            Cursor cursor = gestorDB.obtenerLotePorId(idLote);

            if (cursor.moveToFirst()) {
                // Sacamos los datos de SQLite de forma segura (usando el nombre de la columna)
                // OJO: Si alguna columna se llama distinto en tu BD, cámbialo aquí en el segundo parámetro
                String fecha = obtenerStringSeguro(cursor, "fecha");
                String tempPast = obtenerStringSeguro(cursor, "temperatura_pasteurizacion");
                String tempCuaj = obtenerStringSeguro(cursor, "temperatura");
                String tiempoCuaj = obtenerStringSeguro(cursor, "tiempo_cuajado");
                String phCorte = obtenerStringSeguro(cursor, "ph_corte");
                String phFinal = obtenerStringSeguro(cursor, "ph");
                String notaGuardada = obtenerStringSeguro(cursor, "nota_calidad");
                String observaciones = obtenerStringSeguro(cursor, "observaciones");

                // Los pintamos en la pantalla (ponemos "--" si algún dato está vacío)
                if (txtId != null) txtId.setText("Lote: " + idLote);
                if (txtFecha != null) txtFecha.setText("Fecha: " + (fecha.isEmpty() ? "--/--/----" : fecha));
                if (txtTempPast != null) txtTempPast.setText("Temp. Pasteurización: " + (tempPast.isEmpty() ? "--" : tempPast) + " ºC");
                if (txtTempCuaj != null) txtTempCuaj.setText("Temp. Cuajado: " + (tempCuaj.isEmpty() ? "--" : tempCuaj) + " ºC");
                if (txtTiempoCuaj != null) txtTiempoCuaj.setText("Tiempo de Cuajado: " + (tiempoCuaj.isEmpty() ? "--" : tiempoCuaj) + " min");
                if (txtPhCorte != null) txtPhCorte.setText("PH Corte: " + (phCorte.isEmpty() ? "--" : phCorte));
                if (txtPhFinal != null) txtPhFinal.setText("PH Final: " + (phFinal.isEmpty() ? "--" : phFinal));

                // Mostramos las observaciones (si la columna de observaciones no existe, usamos la de la nota)
                if (txtObs != null) txtObs.setText("Observaciones: " + (observaciones.isEmpty() ? notaGuardada : observaciones));

                // Si ya le habíamos puesto nota antes, cargamos las estrellas guardadas
                if (ratingCalidad != null && notaGuardada != null && !notaGuardada.isEmpty()) {
                    try {
                        ratingCalidad.setRating(Float.parseFloat(notaGuardada));
                    } catch (NumberFormatException e) {
                        // Si la nota original era texto, ignoramos el error y dejamos la barra a cero.
                    }
                }
            }
            cursor.close();
            gestorDB.close();
        }

        // 4. Programamos el botón de GUARDAR NOTA
        if (btnGuardarNota != null && ratingCalidad != null && idLoteFinal != -1) {
            btnGuardarNota.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Sacamos cuántas estrellas ha marcado el usuario (ej: 4.5)
                    float estrellas = ratingCalidad.getRating();

                    // --- VENTANA EMERGENTE (ALERTDIALOG) ---
                    AlertDialog.Builder constructorDialogo = new AlertDialog.Builder(requireContext());
                    constructorDialogo.setTitle("Confirmar puntuación");
                    constructorDialogo.setMessage("¿Estás seguro de que quieres asignar " + estrellas + " estrellas a este lote?");

                    // Si le da a guardar...
                    constructorDialogo.setPositiveButton("Sí, guardar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Conectamos con la BD y guardamos el número como texto
                            DataBaseHelper gestorDB = new DataBaseHelper(getContext(), "gaztandegia.db", null, 1);
                            gestorDB.actualizarNotaLote(idLoteFinal, String.valueOf(estrellas));

                            Toast.makeText(getContext(), "¡Nota guardada! (" + estrellas + " estrellas)", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Si le da a cancelar...
                    constructorDialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss(); // Solo cerramos la ventana emergente
                        }
                    });

                    // Mostramos la ventana
                    constructorDialogo.show();
                }
            });
        }

        return view;
    }

    // Método de seguridad para leer los datos de la BD sin que la app explote
    // si alguna columna se llama de forma distinta o no existe.
    private String obtenerStringSeguro(Cursor cursor, String nombreColumna) {
        int index = cursor.getColumnIndex(nombreColumna);
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getString(index);
        }
        return "";
    }
}