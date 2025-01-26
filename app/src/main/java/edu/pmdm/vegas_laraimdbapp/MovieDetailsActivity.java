package edu.pmdm.vegas_laraimdbapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;

import android.Manifest;

import edu.pmdm.vegas_laraimdbapp.api.ApiClient;
import edu.pmdm.vegas_laraimdbapp.api.IMDBApiService;
import edu.pmdm.vegas_laraimdbapp.models.MovieOverviewResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailsActivity extends AppCompatActivity {

    private static final int REQUEST_SMS_PERMISSION = 100;
    private static final int REQUEST_CONTACT_PERMISSION = 101;

    private String movieDetails = "";

    private TextView titleTextView;
    private TextView plotTextView;
    private TextView releaseDateTextView;
    private TextView ratingTextView;
    private ImageView posterImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Inicialización de las vistas
        titleTextView = findViewById(R.id.titleTextView);
        posterImageView = findViewById(R.id.posterImageView);
        plotTextView = findViewById(R.id.plotTextView);
        releaseDateTextView = findViewById(R.id.releaseDateTextView);
        ratingTextView = findViewById(R.id.ratingTextView);

        // Recibir datos del Intent
        Intent intent = getIntent();
        String movieId = intent.getStringExtra("id");
        String title = intent.getStringExtra("title");
        String imageUrl = intent.getStringExtra("imageUrl");
        String releaseDate = intent.getStringExtra("releaseDate");

        // Validaciones iniciales
        if (movieId == null || movieId.isEmpty()) {
            showError("No se recibió un ID de película válido.");
            finish();
            return;
        }

        titleTextView.setText(title != null ? title : "Título no disponible");
        releaseDateTextView.setText("Fecha de lanzamiento: " + (releaseDate != null ? releaseDate : "No disponible"));

        if (imageUrl == null || imageUrl.isEmpty()) {
            Picasso.get().load(R.drawable.googlelogo).into(posterImageView);
        } else {
            Picasso.get().load(imageUrl).into(posterImageView);
        }

        // Llamada a la API para obtener los detalles
        fetchMovieDetails(movieId);

        // Configurar el botón para enviar SMS
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button sendSmsButton = findViewById(R.id.btn_send_sms);
        sendSmsButton.setOnClickListener(v -> checkContactPermission());
    }

    private void fetchMovieDetails(String movieId) {
        IMDBApiService apiService = ApiClient.getClient().create(IMDBApiService.class);

        apiService.getMovieDetails(movieId).enqueue(new Callback<MovieOverviewResponse>() {
            @Override
            public void onResponse(Call<MovieOverviewResponse> call, Response<MovieOverviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieOverviewResponse.Title movieTitle = response.body().getData().getTitle();
                    if (movieTitle != null) {
                        String plot = movieTitle.getPlotText();
                        double rating = movieTitle.getRating();

                        plotTextView.setText(plot != null ? plot : "Descripción no disponible");
                        ratingTextView.setText(String.format("Puntuación: %.1f", rating));

                        movieDetails = "¡No te pierdas esta película!\n" +
                                "Título: " + movieTitle.getTitleText() + "\n" +
                                "Descripción: " + (plot != null ? plot : "No disponible") + "\n" +
                                "Fecha de lanzamiento: " + movieTitle.getReleaseDateString() + "\n" +
                                "Puntuación: " + String.format("%.1f", rating);
                    } else {
                        showError("Error al cargar los detalles de la película.");
                    }
                } else {
                    showError("No se pudo obtener los detalles de la película. Código: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieOverviewResponse> call, Throwable t) {
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void checkContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSION);
        } else {
            pickContact();
        }
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        pickContactLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> pickContactLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri contactUri = result.getData().getData();
                            String contactNumber = getContactNumber(contactUri);

                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
                            } else {
                                sendSMS(contactNumber, movieDetails);
                            }
                        } else {
                            showError("No se seleccionó ningún contacto.");
                        }
                    });

    private String getContactNumber(Uri contactUri) {
        String number = "";
        try (Cursor cursor = getContentResolver().query(contactUri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                number = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            showError("Error al obtener el número de contacto: " + e.getMessage());
        }
        return number;
    }

    private void sendSMS(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            showError("Número de teléfono no válido.");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNumber));
        intent.putExtra("sms_body", message);
        try {
            startActivity(intent);
        } catch (Exception e) {
            showError("Error al enviar SMS: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CONTACT_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickContact();
        } else if (requestCode == REQUEST_SMS_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de SMS concedido. Selecciona un contacto nuevamente.", Toast.LENGTH_SHORT).show();
        } else {
            showError("Permiso denegado.");
        }
    }
}
