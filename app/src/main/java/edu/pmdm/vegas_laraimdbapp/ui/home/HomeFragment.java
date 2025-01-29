package edu.pmdm.vegas_laraimdbapp.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.vegas_laraimdbapp.MovieDetailsActivity;
import edu.pmdm.vegas_laraimdbapp.R;
import edu.pmdm.vegas_laraimdbapp.adapter.MovieAdapter;
import edu.pmdm.vegas_laraimdbapp.api.ApiClient;
import edu.pmdm.vegas_laraimdbapp.api.IMDBApiService;
import edu.pmdm.vegas_laraimdbapp.database.FavoritesManager;
import edu.pmdm.vegas_laraimdbapp.models.Movie;
import edu.pmdm.vegas_laraimdbapp.models.MovieResponse;
import edu.pmdm.vegas_laraimdbapp.ui.gallery.GalleryFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private IMDBApiService apiService;
    private List<Movie> movieList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla el diseño del fragmento
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Configura el RecyclerView
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        movieAdapter = new MovieAdapter(getContext(), movieList, this::onMovieClick);
        movieAdapter.setOnMovieLongClickListener(this::onMovieLongClick);
        recyclerView.setAdapter(movieAdapter);

        // Inicializa el servicio de la API
        apiService = ApiClient.getClient().create(IMDBApiService.class);

        // Carga las películas
        loadTop10Movies();

        return root;
    }

    private void loadTop10Movies() {
        Call<MovieResponse> call = apiService.getTopMovies("ALL");
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieResponse.Edge> edges = response.body().getData().getTopMeterTitles().getEdges();
                    movieList.clear();
                    // Limitar a las 10 primeras películas con mejor ranking
                    int limit = Math.min(edges.size(), 10);
                    for (int i = 0; i < limit; i++) {
                        MovieResponse.Node node = edges.get(i).getNode();
                        if (node != null) {
                            movieList.add(new Movie(
                                    node.getId(),
                                    node.getImageUrl(),
                                    node.getTitleText(),
                                    node.getPlotText(),
                                    node.getRating(),
                                    node.getReleaseDateString()
                            ));
                        }
                    }
                    movieAdapter.notifyDataSetChanged();
                } else {
                    Log.e("API_ERROR", "Código de respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("API_ERROR", "Error al cargar las películas", t);
            }
        });
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

    private void onMovieLongClick(Movie movie) {
        // 🔹 Obtener el `userId` ACTUALIZADO de SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", null);

        Log.d("HomeFragment", "Descripción: " + movie.getDescription());

        if (userId == null) {
            Log.e("HomeFragment", "No se encontró un ID de usuario válido en SharedPreferences");
            Toast.makeText(getContext(), "Error: No se ha encontrado un usuario válido", Toast.LENGTH_SHORT).show();
            return;
        }

        FavoritesManager favoritesManager = FavoritesManager.getInstance(getContext());
        if (favoritesManager.addFavorite(movie, userId)) {
            Log.i("HomeFragment", "Película ya estaba en favoritos para usuario: " + userId);
            Toast.makeText(getContext(), "Película ya estaba en favoritos", Toast.LENGTH_SHORT).show();
        } else {
            Log.i("HomeFragment", "Película agregada a favoritos para usuario: " + userId);
            Toast.makeText(getContext(), "Película agregada a favoritos", Toast.LENGTH_SHORT).show();
        }
    }



}
