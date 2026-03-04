package com.example.das_gaztandegia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class ListaLoteFragments extends Fragment {

    private DataBaseHelper gestorDB;
    private ListView listaVisual;
    private ArrayList<String> textosQuesos;
    private ArrayList<Integer> idsQuesos;
    private ArrayAdapter<String> adaptador;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflamos el diseño de tu fragment_lista_lotes.xml
        View view = inflater.inflate(R.layout.fragment_lista_lotes, container, false);

        // 2. Enlazamos la lista usando el ID real de tu XML
        listaVisual = view.findViewById(R.id.listaLotes);

        // Instanciamos la base de datos usando getContext() (porque estamos en un Fragment)
        gestorDB = new DataBaseHelper(getContext(), "gaztandegia.db", null, 1);

        // 3. Cargamos los datos
        cargarLista();

        // 4. Lógica de borrado al mantener pulsado
        listaVisual.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                if (idsQuesos.isEmpty()) return false;

                // Sacamos el ID real de la base de datos del queso seleccionado
                int idLoteABorrar = idsQuesos.get(position);

                // Mostramos la ventana emergente de confirmación (usando requireActivity())
                AlertDialog.Builder constructorDialogo = new AlertDialog.Builder(requireActivity());
                constructorDialogo.setTitle("Borrar Queso");
                constructorDialogo.setMessage("¿Estás seguro de que quieres borrar este lote de queso?");

                // Si dice que SÍ
                constructorDialogo.setPositiveButton("Sí, borrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gestorDB.borrarLote(idLoteABorrar);
                        Toast.makeText(getContext(), "Lote eliminado correctamente", Toast.LENGTH_SHORT).show();

                        // Recargamos la lista para que desaparezca al instante sin recargar toda la Activity
                        cargarLista();
                    }
                });

                // Si dice que NO
                constructorDialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                constructorDialogo.show();
                return true;
            }
        });

        return view;
    }

    // Método auxiliar para leer de SQLite y refrescar la lista
    private void cargarLista() {
        textosQuesos = new ArrayList<>();
        idsQuesos = new ArrayList<>();

        Cursor cursor = gestorDB.obtenerTodosLosLotes();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0); // id_lote
            String fecha = cursor.getString(1); // fecha
            String nota = cursor.getString(5); // nota_calidad

            textosQuesos.add("Lote #" + id + " - " + fecha + " (" + nota + ")");
            idsQuesos.add(id);
        }
        cursor.close();

        if (textosQuesos.isEmpty()) {
            textosQuesos.add("No hay lotes registrados.");
        }

        // Usamos ArrayAdapter básico de momento. (Veo que tienes un LoteAdapter.java vacío preparado para el futuro)
        adaptador = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, textosQuesos);
        listaVisual.setAdapter(adaptador);
    }
}