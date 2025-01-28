package edu.pmdm.vegas_laraimdbapp.database;

import android.content.Context;
import android.util.Log;

import java.util.List;

import edu.pmdm.vegas_laraimdbapp.models.Movie;

public class FavoritesManager {

    private static FavoritesManager instance; // Instancia única
    private FavoriteDatabase fBD; // Referencia a la base de datos

    private FavoritesManager(Context context) {
        fBD = new FavoriteDatabase(context); // Inicializamos la base de datos
    }

    public static synchronized FavoritesManager getInstance(Context context) {
        // Patrón Singleton: si no existe la instancia, la creamos
        if (instance == null) {
            instance = new FavoritesManager(context);
        }
        return instance;
    }

    public boolean addFavorite(Movie movie, String userId) {
        // Verificamos si la película tiene un ID válido antes de guardarla
        if (movie.getId() == null || movie.getId().isEmpty()) {
            Log.e("FavoritesManager", "El ID de la película es nulo o vacío. No se puede guardar.");
            return false;
        }

        // Comprobar si la película ya está en favoritos
        List<Movie> currentFavorites = fBD.getAllFavorites(userId);
        if (currentFavorites.contains(movie)) {
            Log.i("FavoritesManager", "La película ya está en favoritos: " + movie.getTitle());
            return true; // Ya estaba guardada
        } else {
            fBD.addFavorite(movie, userId);
            Log.i("FavoritesManager", "Película agregada a favoritos: " + movie.getTitle());
            return false; // Se agregó correctamente
        }
    }

    public void removeFavorite(Movie movie, String userId) {
        // Eliminar una película de favoritos
        fBD.removeFavorite(movie.getId(), userId);
    }

    public List<Movie> getFavoriteMovies(String userId) {
        // Obtener todas las películas favoritas del usuario
        return fBD.getAllFavorites(userId);
    }

}
