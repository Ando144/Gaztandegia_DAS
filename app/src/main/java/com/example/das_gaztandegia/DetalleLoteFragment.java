package com.example.das_gaztandegia;

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

        // 1. Buscamos los componentes por sus IDs exactos del XML
        TextView txtId = view.findViewById(R.id.txtDetalleId);
        TextView txtTemp = view.findViewById(R.id.txtDetalleTemp);
        TextView txtTiempo = view.findViewById(R.id.txtDetalleTiempo);
        TextView txtPh = view.findViewById(R.id.txtDetallePh);
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
                // Sacamos los datos de SQLite
                double temperatura = cursor.getDouble(2); // Columna temperatura
                int tiempo = cursor.getInt(3);            // Columna tiempo
                double ph = cursor.getDouble(4);          // Columna ph
                String notaGuardada = cursor.getString(5);// Columna nota_calidad

                // Los pintamos en la pantalla
                if (txtId != null) txtId.setText("Lote: " + idLote);
                if (txtTemp != null) txtTemp.setText("Temp. Cuajado: " + temperatura + "ºC");
                if (txtTiempo != null) txtTiempo.setText("Tiempo: " + tiempo + " min");
                if (txtPh != null) txtPh.setText("PH Final: " + ph);

                // Si ya le habíamos puesto nota antes, cargamos las estrellas guardadas
                if (ratingCalidad != null && notaGuardada != null && !notaGuardada.isEmpty()) {
                    try {
                        ratingCalidad.setRating(Float.parseFloat(notaGuardada));
                    } catch (NumberFormatException e) {
                        // Si la nota original era texto (ej: "Mi primer queso"),
                        // ignoramos el error y dejamos la barra a cero.
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

                    // Conectamos con la BD y guardamos el número como texto
                    DataBaseHelper gestorDB = new DataBaseHelper(getContext(), "gaztandegia.db", null, 1);
                    gestorDB.actualizarNotaLote(idLoteFinal, String.valueOf(estrellas));

                    Toast.makeText(getContext(), "¡Nota guardada! (" + estrellas + " estrellas)", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }
}