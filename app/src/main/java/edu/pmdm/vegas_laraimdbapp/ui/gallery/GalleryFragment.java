package edu.pmdm.vegas_laraimdbapp.ui.gallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.pmdm.vegas_laraimdbapp.MovieDetailsActivity;
import edu.pmdm.vegas_laraimdbapp.R;
import edu.pmdm.vegas_laraimdbapp.adapter.MovieAdapter;
import edu.pmdm.vegas_laraimdbapp.database.FavoritesManager;
import edu.pmdm.vegas_laraimdbapp.databinding.FragmentGalleryBinding;
import edu.pmdm.vegas_laraimdbapp.models.Movie;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        // Obtener el ID del usuario desde las preferencias compartidas
        String userId = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getString("userId", "default_user");

        // Cargar las películas favoritas del usuario
        FavoritesManager favoritesManager = FavoritesManager.getInstance(getContext());
        List<Movie> favoriteMovies = favoritesManager.getFavoriteMovies(userId);

        // Configurar el adaptador con las películas favoritas
        movieAdapter = new MovieAdapter(getContext(), favoriteMovies, this::onMovieClick);
        movieAdapter.setOnMovieLongClickListener(movie -> onMovieLongClick(movie, userId, favoritesManager, movieAdapter));

        recyclerView.setAdapter(movieAdapter);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void onMovieClick(Movie movie) {
        // Manejar el clic en una película
        Intent intent = new Intent(getContext(), MovieDetailsActivity.class);
        intent.putExtra("id", movie.getId());
        intent.putExtra("title", movie.getTitle());
        intent.putExtra("imageUrl", movie.getImage());
        intent.putExtra("releaseDate", movie.getReleaseDate());
        intent.putExtra("plot", movie.getDescription());
        startActivity(intent);
    }

    private void onMovieLongClick(Movie movie, String userId, FavoritesManager favoritesManager, MovieAdapter adapter) {
        // Eliminar la película de favoritos
        favoritesManager.removeFavorite(movie, userId);
        adapter.removeMovie(movie); // Eliminar del adaptador
        adapter.notifyDataSetChanged(); // Actualizar la vista

        // Mostrar un mensaje al usuario
        Toast.makeText(getContext(), "Película eliminada de favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
    }

}