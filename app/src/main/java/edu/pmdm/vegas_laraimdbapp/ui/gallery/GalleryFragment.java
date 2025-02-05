package edu.pmdm.vegas_laraimdbapp.ui.gallery;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.pmdm.vegas_laraimdbapp.MovieDetailsActivity;
import edu.pmdm.vegas_laraimdbapp.R;
import edu.pmdm.vegas_laraimdbapp.adapter.MovieAdapter;
import edu.pmdm.vegas_laraimdbapp.bluetooth.BluetoothSimulator;
import edu.pmdm.vegas_laraimdbapp.database.FavoritesManager;
import edu.pmdm.vegas_laraimdbapp.models.Movie;

/**
 * Fragmento que muestra las películas favoritas del usuario.
 */
public class GalleryFragment extends Fragment {

    // Declaración de variables
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private ActivityResultLauncher<Intent> enableBluetoothLauncher;
    private FavoritesManager favoritesManager;
    private String userId;
    private List<Movie> favoriteMovies;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = root.findViewById(R.id.recyclerView); // Asignamos el RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // Asignamos el LayoutManager en 2 columnas

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // Obtener el adaptador Bluetooth

        // Inicializar el launcher para habilitar Bluetooth
        enableBluetoothLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == -1) {
                        Toast.makeText(getContext(), "Bluetooth habilitado.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "No se puede compartir sin Bluetooth.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Obtener el ID del usuario actual
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("USER_ID", "default_user"); // Ahora usa el ID real en lugar del email

        Log.d("GalleryFragment", "Obtenido userId: " + userId);

        // Cargar películas favoritas del usuario actual
        favoritesManager = FavoritesManager.getInstance(getContext());
        favoriteMovies = favoritesManager.getFavoriteMovies(userId);

        movieAdapter = new MovieAdapter(getContext(), favoriteMovies, this::onMovieClick); // Asignamos el adaptador
        movieAdapter.setOnMovieLongClickListener(this::onMovieLongClick); // Asignamos el listener para eliminar
        recyclerView.setAdapter(movieAdapter); // Asignamos el adaptador al RecyclerView
        movieAdapter.notifyDataSetChanged(); // Asegurar que la UI se actualiza

        // Botón para compartir
        Button shareButton = root.findViewById(R.id.btnShare);
        shareButton.setOnClickListener(v -> checkBluetoothAndShare(favoriteMovies)); // Llamamos al método para compartir

        return root; // Devolvemos la vista
    }

    /**
     * Método que se llama cuando el fragmento se reanuda.
     */
    @Override
    public void onResume() {
        super.onResume();
        favoriteMovies = favoritesManager.getFavoriteMovies(userId); // Actualizar las películas favoritas
        movieAdapter.updateMovies(favoriteMovies); // Actualizar el adaptador con las películas favoritas
    }

    /**
     * Método que se llama cuando se hace clic en una película.
     * @param movie Película seleccionada.
     */
    private void onMovieClick(Movie movie) {
        // Navegar a la actividad de detalles de la película
        Intent intent = new Intent(getContext(), MovieDetailsActivity.class);
        intent.putExtra("id", movie.getId());
        intent.putExtra("title", movie.getTitle());
        intent.putExtra("imageUrl", movie.getImage());
        intent.putExtra("releaseDate", movie.getReleaseDate());
        intent.putExtra("plot", movie.getPlot());
        startActivity(intent);
    }

    /**
     * Método que se llama cuando se hace un clic largo en una película.
     * @param movie Película seleccionada.
     */
    private void onMovieLongClick(Movie movie) {
        // Eliminar la película de la base de datos
        favoritesManager.removeFavorite(movie, userId);

        // Eliminar la película de la lista del adaptador
        List<Movie> updatedFavorites = favoritesManager.getFavoriteMovies(userId);
        movieAdapter.updateMovies(updatedFavorites); // Actualizar el adaptador con las películas favoritas

        // Notificar al RecyclerView que los datos cambiaron
        movieAdapter.notifyDataSetChanged();

        Toast.makeText(getContext(), "Película eliminada de favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
    }


    /**
     * Método que comprueba si el dispositivo tiene Bluetooth y si está habilitado.
     * @param favoriteMovies Lista de películas favoritas.
     */
    private void checkBluetoothAndShare(List<Movie> favoriteMovies) {

        // Comprobamos si hay películas favoritas
        if (favoriteMovies.isEmpty()) {
            Toast.makeText(getContext(), "No hay películas favoritas para compartir.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobamos si el dispositivo tiene Bluetooth
        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Este dispositivo no soporta Bluetooth.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobamos si el Bluetooth está habilitado
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBluetoothLauncher.launch(enableBluetoothIntent);
        } else {
            BluetoothSimulator bluetoothSimulator = new BluetoothSimulator(requireActivity());
            bluetoothSimulator.simulateBluetoothConnection(favoriteMovies);
        }
    }
}
