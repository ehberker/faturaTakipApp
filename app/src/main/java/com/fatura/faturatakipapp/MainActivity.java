package com.fatura.faturatakipapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    EditText editTextTarih;
    EditText editTextTtutar;
    Spinner spinnerTur;
    Calendar myCalendar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final String a = "sdfdsf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myCalendar = Calendar.getInstance();

        editTextTarih = findViewById(R.id.faturatarihi);
        editTextTtutar = findViewById(R.id.faturatutari);
        spinnerTur = findViewById(R.id.faturaturu);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        editTextTarih.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        Spinner dropdown = findViewById(R.id.faturaturu);
        String[] items = new String[]{"Elektrik", "Doğalgaz", "Su", "İnternet", "Diğer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        Button button = findViewById(R.id.btnEkle);

        final TableLayout tl = findViewById(R.id.tableList);
        final LayoutInflater li = getLayoutInflater();


        View row = li.inflate(R.layout.fatura_row_layout, null);
        TextView tutar = row.findViewById(R.id.tutar);
        TextView tarih = row.findViewById(R.id.tarih);
        TextView tur = row.findViewById(R.id.tur);
        TextView durum = row.findViewById(R.id.durum);

        tutar.setText("TUTAR");
        tarih.setText("TARİH");
        tur.setText("TÜR");
        durum.setText("DURUM");

        tl.addView(row);


        CollectionReference dbCol = db.collection("faturalar");

        dbCol.orderBy("tarih", Query.Direction.ASCENDING).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {


                                View row = li.inflate(R.layout.fatura_row_layout, null);
                                TextView tutar = row.findViewById(R.id.tutar);
                                TextView tarih = row.findViewById(R.id.tarih);
                                TextView tur = row.findViewById(R.id.tur);
                                TextView durum = row.findViewById(R.id.durum);

                                tutar.setText(String.valueOf(document.get("tutar")));
                                tarih.setText(String.valueOf(document.get("tarih")));
                                tur.setText(String.valueOf(document.get("tur")));
                                durum.setText(String.valueOf(document.get("durum")));

                                tl.addView(row);

                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Bir Hata Oluştu", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                ///// db'ye at*****************************************************************************

                Map<String, Object> fatura = new HashMap<>();
                fatura.put("tutar", editTextTtutar.getText().toString());
                fatura.put("tarih", editTextTarih.getText().toString());
                fatura.put("tur", spinnerTur.getSelectedItem().toString());
                fatura.put("durum", "ÖDENMEDİ");

                db.collection("faturalar")
                        .add(fatura)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                                Log.d("", "DocumentSnapshot written with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("", "Error adding document", e);
                            }
                        });

            }
        });

    }

    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        editTextTarih.setText(sdf.format(myCalendar.getTime()));
    }

}
