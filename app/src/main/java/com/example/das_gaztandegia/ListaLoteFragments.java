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
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class ListaLoteFragments extends Fragment {

    private DataBaseHelper gestorDB;
    private ListView listaVisual;

    private ArrayList<Lote> listaLotesData;
    private LoteAdapter adaptadorPersonalizado;

    private String busquedaActual = "";
    private float notaMinimaActual = 0f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista_lotes, container, false);

        listaVisual = view.findViewById(R.id.listaLotes);
        SearchView buscadorLotes = view.findViewById(R.id.buscadorLotes);
        Spinner spinnerFiltroNota = view.findViewById(R.id.spinnerFiltroNota);

        gestorDB = new DataBaseHelper(getContext(), "gaztandegia.db", null, 1);

        String[] opcionesFiltro = {"Todas", "Solo ★ 5", "★ 4 o más", "★ 3 o más"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, opcionesFiltro);
        spinnerFiltroNota.setAdapter(adapterSpinner);

        buscadorLotes.clearFocus();

        buscadorLotes.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                busquedaActual = newText;
                cargarLista(busquedaActual, notaMinimaActual);
                return true;
            }
        });

        spinnerFiltroNota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0: notaMinimaActual = 0f; break;
                    case 1: notaMinimaActual = 5f; break;
                    case 2: notaMinimaActual = 4f; break;
                    case 3: notaMinimaActual = 3f; break;
                }
                cargarLista(busquedaActual, notaMinimaActual);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        listaVisual.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                Lote loteClicado = listaLotesData.get(position);

                if (listaLotesData.isEmpty() || loteClicado.getIdLote() == -1) return false;

                int idLoteABorrar = loteClicado.getIdLote();

                AlertDialog.Builder constructorDialogo = new AlertDialog.Builder(requireActivity());
                constructorDialogo.setTitle("Borrar Queso");
                constructorDialogo.setMessage("¿Estás seguro de que quieres borrar este lote de queso?");

                constructorDialogo.setPositiveButton("Sí, borrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gestorDB.borrarLote(idLoteABorrar);
                        Toast.makeText(getContext(), "Lote eliminado correctamente", Toast.LENGTH_SHORT).show();

                        cargarLista(busquedaActual, notaMinimaActual);

                        // Si estábamos en horizontal y acabamos de borrar el queso que estábamos viendo,
                        // lo ideal sería volver a poner el fragmento vacío. Pero lo dejamos simple por ahora.
                    }
                });

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

        listaVisual.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Lote loteClicado = listaLotesData.get(position);

                if (listaLotesData.isEmpty() || loteClicado.getIdLote() == -1) return;

                int idLoteSeleccionado = loteClicado.getIdLote();

                DetalleLoteFragment fragmentDetalle = new DetalleLoteFragment();
                Bundle paqueteDatos = new Bundle();
                paqueteDatos.putInt("ID_LOTE", idLoteSeleccionado);
                fragmentDetalle.setArguments(paqueteDatos);

                View huecoDerecho = requireActivity().findViewById(R.id.contenedor_detalle);

                if (huecoDerecho != null) {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.contenedor_detalle, fragmentDetalle)
                            .commit();
                } else {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.contenedor_maestro, fragmentDetalle)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarLista(busquedaActual, notaMinimaActual);
    }

    private void cargarLista(String busqueda, float notaMinima) {
        listaLotesData = new ArrayList<>();
        Cursor cursor = gestorDB.obtenerLotesFiltrados(busqueda, notaMinima);

        int idIndex = cursor.getColumnIndex("id_lote");
        int fechaIndex = cursor.getColumnIndex("fecha");
        int notaIndex = cursor.getColumnIndex("nota_calidad");

        while (cursor.moveToNext()) {
            int id = cursor.getInt(idIndex != -1 ? idIndex : 0);
            String fecha = cursor.getString(fechaIndex != -1 ? fechaIndex : 1);
            String nota = cursor.getString(notaIndex != -1 ? notaIndex : 8);

            String textoNota = (nota != null && !nota.isEmpty()) ? "★ " + nota : "Sin puntuar";
            listaLotesData.add(new Lote(id, fecha, textoNota));
        }
        cursor.close();

        if (listaLotesData.isEmpty()) {
            listaLotesData.add(new Lote(-1, "No se encontraron lotes.", ""));
        }

        adaptadorPersonalizado = new LoteAdapter(getContext(), listaLotesData);
        listaVisual.setAdapter(adaptadorPersonalizado);

        /* =========================================================================
           NUEVO: CARGAR FRAGMENTO VACÍO EN HORIZONTAL
           Si el móvil está tumbado (existe contenedor_detalle) y no hay nada cargado,
           mostramos el fragmento vacío por defecto.
           ========================================================================= */
        View huecoDerecho = requireActivity().findViewById(R.id.contenedor_detalle);

        // Si el hueco derecho existe (estamos en horizontal)
        if (huecoDerecho != null) {
            // Comprobamos que el hueco esté vacío para no machacar la vista si el usuario ya había tocado un queso
            if (requireActivity().getSupportFragmentManager().findFragmentById(R.id.contenedor_detalle) == null) {

                // Instanciamos tu fragmento vacío (Asegúrate de que la clase se llama VacioFragment)
                FragmentVacio fragmentVacio = new FragmentVacio();

                // Lo inyectamos en la parte derecha de la pantalla
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contenedor_detalle, fragmentVacio)
                        .commit();
            }
        }
    }
}