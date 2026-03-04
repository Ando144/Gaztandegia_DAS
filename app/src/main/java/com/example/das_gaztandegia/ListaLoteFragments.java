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

        /* =========================================================================
           5. ABRIR DETALLES DEL LOTE (CLIC NORMAL)
           Nota: La lógica para detectar si estamos en vertical u horizontal buscando
           el contenedor de detalle por ID es el patrón Master-Detail clásico de Android.
           StackOverflow ref: https://stackoverflow.com/questions/17495914/how-to-implement-master-detail-flow
           ========================================================================= */
        listaVisual.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 1. Sacamos el ID del lote que hemos tocado
                int idLoteSeleccionado = idsQuesos.get(position);

                // 2. Preparamos el fragmento de los detalles
                // OJO: Cambia "DetalleLoteFragment" por el nombre real de tu clase
                DetalleLoteFragment fragmentDetalle = new DetalleLoteFragment();

                // Le pasamos el ID del queso "empaquetado" en un Bundle para que sepa cuál cargar
                Bundle paqueteDatos = new Bundle();
                paqueteDatos.putInt("ID_LOTE", idLoteSeleccionado);
                fragmentDetalle.setArguments(paqueteDatos);

                // 3. Comprobamos si el móvil está en vertical o horizontal
                View huecoDerecho = requireActivity().findViewById(R.id.contenedor_detalle);

                if (huecoDerecho != null) {
                    // HORIZONTAL: Cambiamos solo el trozo de la derecha
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.contenedor_detalle, fragmentDetalle)
                            .commit();
                } else {
                    // VERTICAL: Cambiamos toda la pantalla y le decimos a Android que nos deje volver atrás
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.contenedor_maestro, fragmentDetalle)
                            .addToBackStack(null) // Esto hace que si pulsas la flecha de "Atrás", vuelvas a la lista
                            .commit();
                }
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