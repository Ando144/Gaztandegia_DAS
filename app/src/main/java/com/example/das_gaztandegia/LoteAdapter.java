package com.example.das_gaztandegia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class LoteAdapter extends ArrayAdapter<Lote> {

    public LoteAdapter(Context context, List<Lote> lotes) {
        super(context, 0, lotes);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View fila = convertView;
        if (fila == null) {
            // Si la fila no existe, la "inflamos" desde nuestro XML
            fila = LayoutInflater.from(getContext()).inflate(R.layout.item_lote, parent, false);
        }

        // 1. Sacamos el objeto Lote que toca dibujar en esta posición
        Lote loteActual = getItem(position);

        // 2. Buscamos las cajas de texto dentro de nuestra fila XML
        TextView tvId = fila.findViewById(R.id.tvFilaId);
        TextView tvFecha = fila.findViewById(R.id.tvFilaFecha);
        TextView tvNota = fila.findViewById(R.id.tvFilaNota);

        // 3. Rellenamos los datos
        if (loteActual != null) {
            // Si el ID es -1, significa que es el mensaje de "No hay quesos"
            if (loteActual.getIdLote() == -1) {
                tvId.setText("Información:");
                tvFecha.setText(loteActual.getFecha()); // Aquí irá el texto "No se encontraron lotes"
                tvNota.setText("");
            } else {
                tvId.setText("Lote #" + loteActual.getIdLote());
                tvFecha.setText("Fecha: " + loteActual.getFecha());
                tvNota.setText(loteActual.getNota());
            }
        }

        return fila; // Devolvemos la fila ya dibujada
    }
}