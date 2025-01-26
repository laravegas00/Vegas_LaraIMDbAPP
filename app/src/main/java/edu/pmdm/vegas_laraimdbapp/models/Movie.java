package edu.pmdm.vegas_laraimdbapp.models;
import android.os.Parcel;
import android.os.Parcelable;

public class Movie{

    private String id;
    private String image;
    private String title;
    private String description;
    private double rating;
    private String releaseDate;

    public Movie(String id, String image, String title, String description, double rating, String releaseDate) {
        this.id = id;
        this.image = image;
        this.title = title;
        this.description = description;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getRating() {
        return rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }


    @Override
    public boolean equals(Object obj) {
        // Comparamos las películas basándonos en su ID
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Movie movie = (Movie) obj;
        return id.equals(movie.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode(); // Genera un hash basado en el ID de la película
    }

}