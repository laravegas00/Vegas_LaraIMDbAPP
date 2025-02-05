package edu.pmdm.vegas_laraimdbapp.bluetooth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.pmdm.vegas_laraimdbapp.models.Movie;

/**
 * Clase para simular la conexión Bluetooth y compartir datos.
 */
public class BluetoothSimulator {

    //Declarar variable del Contexto de la aplicación
    private Context context;

    /**
     * Constructor con contexto.
     * @param context Contexto de la aplicación.
     */
    public BluetoothSimulator(Context context) {
        this.context = context;
    }

    /**
     * Simular la conexión Bluetooth y compartir datos.
     * @param favoriteMovies Lista de películas favoritas.
     */
    public void simulateBluetoothConnection(List<Movie> favoriteMovies) {
        if (favoriteMovies == null || favoriteMovies.isEmpty()) {
            Toast.makeText(context, "No hay películas favoritas para compartir", Toast.LENGTH_SHORT).show();
            return;
        }

        //Formatear la lista de películas para que se vea más legible
        StringBuilder formattedMovies = new StringBuilder("Películas Favoritas:\n\n");

        //Recorrer la lista de películas y formatear
        int count = 1;
        for (Movie movie : favoriteMovies) {
            formattedMovies.append(count)
                    .append(". ").append(movie.getTitle()).append("\n")
                    .append("   - ID: ").append(movie.getId()).append("\n\n")
                    .append("   - Fecha de lanzamiento: ").append(movie.getReleaseDate()).append("\n")
                    .append("   - Poster URL: ").append(movie.getImage()).append("\n\n")
                    .append("   - Descripcion: ").append(movie.getPlot()).append("\n\n")
                    .append("   - Puntuación: ").append(movie.getRating()).append("\n\n");
            count++;
        }

        //Mostrar la información formateada en el AlertDialog
        new AlertDialog.Builder(context)
                .setTitle("Películas Favoritas")
                .setMessage(formattedMovies.toString())
                .setPositiveButton("Compartir", (dialog, which) -> {
                    dialog.dismiss();
                    shareJsonViaBluetooth(favoriteMovies);
                })
                .setNegativeButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }


    /**
     * Compartir datos en formato JSON a través de Bluetooth.
     * @param favoriteMovies Lista de películas favoritas.
     */
    private void shareJsonViaBluetooth(List<Movie> favoriteMovies) {
        try {
            Gson gson = new Gson(); // Crear una instancia de Gson
            String jsonFavorites = gson.toJson(favoriteMovies); // Convertir la lista de películas a JSON

            // Crear un archivo JSON en el directorio de caché de la aplicación
            File file = new File(context.getCacheDir(), "favorites.json");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(jsonFavorites);
                writer.flush();
            }

            // Crear un Intent para compartir el archivo JSON a través de Bluetooth
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            ));
            intent.setPackage("com.android.bluetooth");

            // Iniciar la actividad de selección de aplicaciones para compartir
            context.startActivity(Intent.createChooser(intent, "Compartir favoritos"));

        } catch (IOException e) {
            Log.e("BluetoothSimulator", "Error al crear el archivo JSON: " + e.getMessage());
            Toast.makeText(context, "Error al preparar los datos para compartir.", Toast.LENGTH_SHORT).show();
        }
    }

}
