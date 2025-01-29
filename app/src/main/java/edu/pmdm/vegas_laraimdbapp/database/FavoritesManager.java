package edu.pmdm.vegas_laraimdbapp.database;

import android.content.Context;
import android.util.Log;

import java.util.List;

import edu.pmdm.vegas_laraimdbapp.models.Movie;

public class FavoritesManager {

    private static FavoritesManager instance;
    private FavoriteDatabase fBD;

    private FavoritesManager(Context context) {
        fBD = new FavoriteDatabase(context);
    }

    public static synchronized FavoritesManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoritesManager(context);
        }
        return instance;
    }

    public boolean addFavorite(Movie movie, String userId) {
        if (movie.getId() == null || movie.getId().isEmpty()) {
            Log.e("FavoritesManager", "El ID de la película es nulo o vacío.");
            return false;
        }

        // **Asegurar que se verifica el usuario antes de agregar**
        if (fBD.movieExists(movie.getId(), userId)) {
            Log.i("FavoritesManager", "Película ya en favoritos de usuario: " + userId);
            return true;
        } else {
            fBD.addFavorite(movie, userId);
            Log.i("FavoritesManager", "Película añadida a favoritos de usuario: " + userId);
            return false;
        }
    }

    public void removeFavorite(Movie movie, String userId) {
        fBD.removeFavorite(movie.getId(), userId);
    }

    public List<Movie> getFavoriteMovies(String userId) {
        // **Obtener SOLO las películas del usuario actual**
        return fBD.getAllFavorites(userId);
    }
}
