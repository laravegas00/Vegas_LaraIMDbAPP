package edu.pmdm.vegas_laraimdbapp.api;

import edu.pmdm.vegas_laraimdbapp.models.MovieOverviewResponse;
import edu.pmdm.vegas_laraimdbapp.models.MovieResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface IMDBApiService {
    // Cabeceras necesarias para autenticar con la API de IMDb
    @Headers({
            "x-rapidapi-host: imdb-com.p.rapidapi.com",
            "x-rapidapi-key: 9c04b1a854msh2056acaabc5ce24p142c9djsnbdd53d066b65" // Clave de la API
    })
    @GET("title/get-top-meter") // Endpoint para obtener las películas más populares
    Call<MovieResponse> getTopMovies(@Query("topMeterTitlesType") String type);
    // Llamamos a este método para obtener el ranking de películas.
    // `type` define el tipo de ranking que queremos (por ejemplo, "ALL" para todas las categorías).

    @Headers({
            "x-rapidapi-host: imdb-com.p.rapidapi.com",
            "x-rapidapi-key: 9c04b1a854msh2056acaabc5ce24p142c9djsnbdd53d066b65"
    })
    @GET("title/get-overview") // Endpoint para obtener los detalles de una película
    Call<MovieOverviewResponse> getMovieDetails(@Query("tconst") String movieId);
    // Aquí pasamos el ID de la película (`tconst`), y la API nos devuelve información detallada sobre ella.

}
