package edu.pmdm.vegas_laraimdbapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.vegas_laraimdbapp.models.Movie;

public class FavoriteDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favoritesmovies.db"; // Nombre de la base de datos
    private static final int DATABASE_VERSION = 2; // Versión de la base de datos para actualizaciones

    // Nombre de la tabla y columnas
    private static final String TABLE_FAVORITES = "favorites";
    private static final String COLUMN_ID = "id"; // ID de la película
    private static final String COLUMN_TITLE = "title"; // Título de la película
    private static final String COLUMN_IMAGEURL = "imageUrl"; // Imagen de la película
    private static final String COLUMN_RELEASEDATE = "releaseDate"; // Fecha de lanzamiento
    private static final String COLUMN_PLOT = "description"; // Sinopsis
    private static final String COLUMN_RATING = "rating"; // Puntuación
    private static final String COLUMN_USERID = "userId"; // Usuario que ha guardado la película

    public FavoriteDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear la tabla de favoritos con clave compuesta (ID de película y usuario)
        String createTable = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                COLUMN_ID + " TEXT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_IMAGEURL + " TEXT, " +
                COLUMN_RELEASEDATE + " TEXT, " +
                COLUMN_PLOT + " TEXT, " +
                COLUMN_RATING + " REAL, " +
                COLUMN_USERID + " TEXT, " +
                "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_USERID + "))"; // Clave compuesta para evitar duplicados por usuario
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Si la versión es antigua, agregamos la columna "user_id" para asociar favoritos a cada usuario
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_FAVORITES + " ADD COLUMN " + COLUMN_USERID + " TEXT DEFAULT 'unknown_user'");
                Log.d("Database Upgrade", "Columna 'userid' añadida correctamente.");
            } catch (Exception e) {
                Log.e("Database Upgrade", "Error al agregar la columna 'userid': " + e.getMessage());
            }
        }
    }

    /**
     * Obtener todas las películas favoritas de un usuario
     * @param userId ID del usuario
     * @return Lista de películas favoritas
     */
    public List<Movie> getAllFavorites(String userId) {

        List<Movie> favoriteMovies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_USERID + " = ?";
        String[] selectionArgs = {userId};

        Cursor cursor = db.query(TABLE_FAVORITES, null, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Movie movie = new Movie();
                movie.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                movie.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                movie.setImage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGEURL)));
                movie.setReleaseDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RELEASEDATE)));
                movie.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLOT)));
                movie.setRating(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RATING)));
                favoriteMovies.add(movie);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return favoriteMovies;
    }

    /**
     * Comprobar si una película ya está en la base de datos para ese usuario
     * @param movieId ID de la película
     * @param userId ID del usuario
     * @return True si la película ya está en favoritos, false en caso contrario
     */
    private boolean movieExists(String movieId, String userId) {
        // Comprobar si una película ya está en la base de datos para ese usuario
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_ID + " = ? AND " + COLUMN_USERID + " = ?";
        String[] selectionArgs = {movieId, userId};

        Cursor cursor = db.query(TABLE_FAVORITES, null, selection, selectionArgs, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }

    /**
     * Agregar una película a la lista de favoritos
     * @param movie Película a agregar
     * @param userId ID del usuario
     */
    public void addFavorite(Movie movie, String userId) {
        // Verificar si la película ya está en favoritos para evitar duplicados
        if (movieExists(movie.getId(), userId)) {
            Log.d("FavoritesDatabaseHelper", "La película ya está en favoritos: " + movie.getId());
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, movie.getId());
        values.put(COLUMN_TITLE, movie.getTitle());
        values.put(COLUMN_IMAGEURL, movie.getImage());
        values.put(COLUMN_RELEASEDATE, movie.getReleaseDate());
        values.put(COLUMN_PLOT, movie.getDescription());
        values.put(COLUMN_RATING, movie.getRating());
        values.put(COLUMN_USERID, userId); // Guardar el ID del usuario

        // Insertar la película en la base de datos
        long result = db.insert(TABLE_FAVORITES, null, values);
        if (result == -1) {
            Log.e("FavoritesDatabaseHelper", "Error al insertar la película en la base de datos: " + movie.getId());
        } else {
            Log.d("FavoritesDatabaseHelper", "Película agregada con éxito: " + movie.getId());
        }
        db.close();
    }

    /**
     * Eliminar una película de la lista de favoritos
     * @param movieId ID de la película
     * @param userId ID del usuario
     */
    public void removeFavorite(String movieId, String userId) {
        // Eliminar una película de la lista de favoritos
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_ID + " = ? AND " + COLUMN_USERID + " = ?";
        String[] whereArgs = {movieId, userId};
        db.delete(TABLE_FAVORITES, whereClause, whereArgs);
        db.close();
    }


}
