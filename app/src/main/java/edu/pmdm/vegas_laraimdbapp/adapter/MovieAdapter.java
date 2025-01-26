package edu.pmdm.vegas_laraimdbapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.vegas_laraimdbapp.R;
import edu.pmdm.vegas_laraimdbapp.models.Movie;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movieList;
    private final Context context;
    private OnMovieLongClickListener mlcl;
    private final OnMovieClickListener mcl;

    public MovieAdapter(Context context, List<Movie> movieList, OnMovieClickListener mcl) {
        this.context = context;
        this.movieList = movieList;
        this.mcl = mcl;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        // Mostrar el título de la película
        holder.imageView.setContentDescription(movie.getTitle());

        // Cargar la imagen de la película con Picasso
        String imageUrl = movie.getImage();
        if (imageUrl == null || imageUrl.isEmpty()) {
            holder.imageView.setImageResource(R.drawable.googlelogo);
            Log.w("MOVIE", "No hay imagen para mostrar");
        } else {
            Picasso.get()
                    .load(imageUrl)
                    //.placeholder(R.drawable.loading_image) // Imagen mientras carga
                    //.error(R.drawable.googlelogo) // Imagen si falla la carga
                    .into(holder.imageView);
        }

        // Manejar clics
        holder.itemView.setOnClickListener(v -> {
            if (mcl != null) {
                mcl.onMovieClick(movie);
            }
        });

        // Manejar clics largos
        holder.itemView.setOnLongClickListener(v -> {
            if (mlcl != null) {
                mlcl.onMovieLongClick(movie);
            }
            return true;
        });
    }

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie); // Método para manejar clics simples
    }

    public interface OnMovieLongClickListener {
        void onMovieLongClick(Movie movie); // Método que se ejecutará cuando se haga un clic largo en una película.
    }

    public void setOnMovieLongClickListener(OnMovieLongClickListener listener) {
        this.mlcl = listener; // Guardamos la referencia del listener para usarlo en el adaptador.
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public void updateMovies(List<Movie> movies) {
        this.movieList = movies != null ? movies : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.moviePoster);
        }
    }
}
