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

    // NOVEDAD: Sustituimos los dos ArrayLists sueltos por una única lista del objeto Lote
    private ArrayList<Lote> listaLotesData;
    private LoteAdapter adaptadorPersonalizado;

    // Variables globales para recordar qué estamos buscando/filtrando en cada momento
    private String busquedaActual = "";
    private float notaMinimaActual = 0f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflamos el diseño de tu fragment_lista_lotes.xml
        View view = inflater.inflate(R.layout.fragment_lista_lotes, container, false);

        // 2. Enlazamos la lista y los nuevos componentes de búsqueda usando los IDs del XML
        listaVisual = view.findViewById(R.id.listaLotes);
        SearchView buscadorLotes = view.findViewById(R.id.buscadorLotes);
        Spinner spinnerFiltroNota = view.findViewById(R.id.spinnerFiltroNota);

        // Instanciamos la base de datos usando getContext()
        gestorDB = new DataBaseHelper(getContext(), "gaztandegia.db", null, 1);

        // 3. Configuramos las opciones del Spinner (Filtro de notas)
        String[] opcionesFiltro = {"Todas", "Solo ★ 5", "★ 4 o más", "★ 3 o más"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, opcionesFiltro);
        spinnerFiltroNota.setAdapter(adapterSpinner);

        /* =========================================================================
           Quitar el foco automático del buscador para que no salte el teclado
           StackOverflow ref: https://stackoverflow.com/questions/15543186/how-do-i-stop-the-keyboard-from-popping-up-on-activity-start
           ========================================================================= */
        buscadorLotes.clearFocus();

        /* =========================================================================
           BUSCADOR EN TIEMPO REAL Y FILTRO COMBINADO
           Nota: La estrategia de guardar el estado del query y el spinner en variables
           globales para lanzar la he sacado de StackOverflow:
           https://stackoverflow.com/questions/30398247/how-to-filter-a-listview-with-searchview-and-spinner-together
           ========================================================================= */

        // A. Cuando el usuario escribe en la barra de búsqueda...
        buscadorLotes.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                busquedaActual = newText;
                cargarLista(busquedaActual, notaMinimaActual); // Recargamos con el nuevo texto
                return true;
            }
        });

        // B. Cuando el usuario cambia el desplegable de las estrellas...
        spinnerFiltroNota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0: notaMinimaActual = 0f; break; // Todas
                    case 1: notaMinimaActual = 5f; break; // Solo 5
                    case 2: notaMinimaActual = 4f; break; // 4 o más
                    case 3: notaMinimaActual = 3f; break; // 3 o más
                }
                cargarLista(busquedaActual, notaMinimaActual); // Recargamos con la nueva nota mínima
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 4. Cargamos los datos por primera vez al abrir la pantalla
        // Nota: Movido también a onResume para actualizar al volver de otras pantallas.

        // 5. Lógica de borrado al mantener pulsado
        listaVisual.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                // Recuperamos el objeto Lote que se ha clicado
                Lote loteClicado = listaLotesData.get(position);

                // Evitamos errores si la lista está vacía o si hemos clicado en el texto de "No hay lotes"
                if (listaLotesData.isEmpty() || loteClicado.getIdLote() == -1) return false;

                // Sacamos el ID real de la base de datos del queso seleccionado
                int idLoteABorrar = loteClicado.getIdLote();

                // Mostramos la ventana emergente de confirmación
                AlertDialog.Builder constructorDialogo = new AlertDialog.Builder(requireActivity());
                constructorDialogo.setTitle("Borrar Queso");
                constructorDialogo.setMessage("¿Estás seguro de que quieres borrar este lote de queso?");

                // Si dice que SÍ
                constructorDialogo.setPositiveButton("Sí, borrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gestorDB.borrarLote(idLoteABorrar);
                        Toast.makeText(getContext(), "Lote eliminado correctamente", Toast.LENGTH_SHORT).show();

                        // Recargamos la lista aplicando los filtros que estén puestos en ese momento
                        cargarLista(busquedaActual, notaMinimaActual);
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
           6. ABRIR DETALLES DEL LOTE (CLIC NORMAL)
           Nota: La lógica para detectar si estamos en vertical u horizontal buscando
           el contenedor de detalle por ID es el patrón Master-Detail.
           StackOverflow ref: https://stackoverflow.com/questions/17495914/how-to-implement-master-detail-flow
           ========================================================================= */
        listaVisual.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Recuperamos el objeto Lote que se ha clicado
                Lote loteClicado = listaLotesData.get(position);

                // Evitamos crasheos si la lista no tiene quesos reales
                if (listaLotesData.isEmpty() || loteClicado.getIdLote() == -1) return;

                // 1. Sacamos el ID del lote que hemos tocado
                int idLoteSeleccionado = loteClicado.getIdLote();

                // 2. Preparamos el fragmento de los detalles
                DetalleLoteFragment fragmentDetalle = new DetalleLoteFragment();

                // Le pasamos el ID del queso "empaquetado" en un Bundle
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
                    // VERTICAL: Cambiamos toda la pantalla y permitimos volver atrás
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.contenedor_maestro, fragmentDetalle)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        return view;
    }

    // NOVEDAD: Al volver a esta pantalla desde la creación de un lote, forzamos la recarga de la lista
    @Override
    public void onResume() {
        super.onResume();
        cargarLista(busquedaActual, notaMinimaActual);
    }

    // Método auxiliar modificado para usar POO y el LoteAdapter
    private void cargarLista(String busqueda, float notaMinima) {
        // Inicializamos la lista de objetos
        listaLotesData = new ArrayList<>();

        Cursor cursor = gestorDB.obtenerLotesFiltrados(busqueda, notaMinima);

        // Buscamos en qué posición exacta están las columnas que necesitamos
        int idIndex = cursor.getColumnIndex("id_lote");
        int fechaIndex = cursor.getColumnIndex("fecha");
        int notaIndex = cursor.getColumnIndex("nota_calidad");

        while (cursor.moveToNext()) {
            // Sacamos los datos de forma segura (si no encuentra el índice usa el fallback)
            int id = cursor.getInt(idIndex != -1 ? idIndex : 0);
            String fecha = cursor.getString(fechaIndex != -1 ? fechaIndex : 1);
            String nota = cursor.getString(notaIndex != -1 ? notaIndex : 8); // ¡La nota ahora es la columna 8!

            String textoNota = (nota != null && !nota.isEmpty()) ? "★ " + nota : "Sin puntuar";

            // Creamos un objeto Lote nuevo y lo añadimos a nuestra lista
            listaLotesData.add(new Lote(id, fecha, textoNota));
        }
        cursor.close();

        // Si la búsqueda no devuelve resultados, creamos un Lote "falso" para avisar al usuario
        if (listaLotesData.isEmpty()) {
            listaLotesData.add(new Lote(-1, "No se encontraron lotes.", ""));
        }

        // Le pasamos la lista de objetos a nuestro nuevo Adaptador
        adaptadorPersonalizado = new LoteAdapter(getContext(), listaLotesData);
        listaVisual.setAdapter(adaptadorPersonalizado);
    }
}