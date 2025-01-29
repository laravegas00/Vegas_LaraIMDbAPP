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
    private static final String DATABASE_NAME = "favoritesmovies.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_FAVORITES = "favorites";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_IMAGEURL = "imageUrl";
    private static final String COLUMN_RELEASEDATE = "releaseDate";
    private static final String COLUMN_PLOT = "description";
    private static final String COLUMN_RATING = "rating";
    private static final String COLUMN_USERID = "userId";

    public FavoriteDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                COLUMN_ID + " TEXT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_IMAGEURL + " TEXT, " +
                COLUMN_RELEASEDATE + " TEXT, " +
                COLUMN_PLOT + " TEXT, " +
                COLUMN_RATING + " REAL, " +
                COLUMN_USERID + " TEXT, " +
                "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_USERID + "))";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_FAVORITES + " ADD COLUMN " + COLUMN_USERID + " TEXT DEFAULT 'unknown_user'");
                Log.d("Database Upgrade", "Columna 'userid' añadida.");
            } catch (Exception e) {
                Log.e("Database Upgrade", "Error al agregar 'userid': " + e.getMessage());
            }
        }
    }

    /**
     * Obtener todas las películas favoritas de un usuario
     */
    public List<Movie> getAllFavorites(String userId) {
        List<Movie> favoriteMovies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String selection = COLUMN_USERID + " = ?";
            String[] selectionArgs = {userId};

            cursor = db.query(TABLE_FAVORITES, null, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Movie movie = new Movie();
                    movie.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    movie.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                    movie.setImage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGEURL)));
                    movie.setReleaseDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RELEASEDATE)));
                    movie.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLOT)));
                    movie.setRating(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RATING)));

                    //Log.d("FavoriteDatabase", "Película recuperada: " + movie.getTitle());
                    favoriteMovies.add(movie);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("FavoriteDatabase", "Error al obtener favoritos: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return favoriteMovies;
    }

    /**
     * Comprobar si una película ya está en la base de datos para ese usuario
     */
    public boolean movieExists(String movieId, String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_ID + " = ? AND " + COLUMN_USERID + " = ?";
        String[] selectionArgs = {movieId, userId};

        Cursor cursor = db.query(TABLE_FAVORITES, null, selection, selectionArgs, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);

        if (cursor != null) cursor.close();
        return exists;
    }

    /**
     * Agregar una película a la lista de favoritos
     */
    public void addFavorite(Movie movie, String userId) {
        if (movieExists(movie.getId(), userId)) {
            Log.d("FavoriteDatabase", "La película ya está en favoritos: " + movie.getId());
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, movie.getId());
        values.put(COLUMN_TITLE, movie.getTitle());
        values.put(COLUMN_IMAGEURL, movie.getImage());
        values.put(COLUMN_RELEASEDATE, movie.getReleaseDate());

        // 🔹 Guardar la descripción solo si no es null
        values.put(COLUMN_PLOT, (movie.getDescription() != null && !movie.getDescription().isEmpty())
                ? movie.getDescription() : "Descripción no disponible");

        // 🔹 Guardar el rating solo si es mayor a 0
        values.put(COLUMN_RATING, movie.getRating() > 0 ? movie.getRating() : -1.0);

        values.put(COLUMN_USERID, userId);

        try {
            db.insert(TABLE_FAVORITES, null, values);
            Log.d("FavoriteDatabase", "Película agregada con éxito: " + movie.getId());
        } catch (Exception e) {
            Log.e("FavoriteDatabase", "Error al agregar película: " + e.getMessage());
        }
    }

    /**
     * Eliminar una película de la lista de favoritos
     */
    public void removeFavorite(String movieId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String whereClause = COLUMN_ID + " = ? AND " + COLUMN_USERID + " = ?";
            String[] whereArgs = {movieId, userId};

            db.delete(TABLE_FAVORITES, whereClause, whereArgs);
            Log.d("FavoriteDatabase", "Película eliminada de favoritos: " + movieId);
        } catch (Exception e) {
            Log.e("FavoriteDatabase", "Error al eliminar película: " + e.getMessage());
        }
    }
}
