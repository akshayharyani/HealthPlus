package com.ackrotech.healthplus.ui.report;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ackrotech.healthplus.R;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ackrotech.healthplus.R;
import com.ackrotech.healthplus.data.model.UserReport;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddReportActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQ = 234;
    private TextView tvDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private EditText etCentreName;
    private Spinner spinner1;
    private static final String[] items = {"Positive", "Negative"};
    private Button btnChooseFile;
    private Button btnSubmit;
    private Button btnUploadFile;
    private Uri filePath;
    private TextView tvFilePathShow;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;
    private String spinnerResult;
    private static final String KEY_RESULT = "result";
    private static final String KEY_DATE = "date";
    private static final String KEY_TESTCENTRE = "testCentre";
    private String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private Uri imgUri;
    private DatabaseReference ref;
    private UserReport userReport;
    private StorageReference mStorageref;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();


        tvFilePathShow = (TextView) findViewById(R.id.tvFilePath);
        etCentreName = (EditText) findViewById(R.id.etCentreName);
        btnChooseFile = (Button) findViewById(R.id.btnChooseFile);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        tvDate = (TextView) findViewById(R.id.tvDateSelect);
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        userReport = new UserReport();

        ref = FirebaseDatabase.getInstance().getReference().child("users");
        mStorageref = FirebaseStorage.getInstance().getReference(userUid).child("reports").child("users");
        btnUploadFile = (Button) findViewById(R.id.btnUploadFile);

        //buttons
        //Choose file btn
        btnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        //Date
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(AddReportActivity.this, android.R.style.Theme_Material_Dialog_MinWidth,
                        mDateSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
                dialog.show();
            }
        });

        //Upload and Notify button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userReport.setCentreName(etCentreName.getText().toString().trim());
                userReport.setDate(tvDate.getText().toString());
                userReport.setReportResult(spinner1.getSelectedItem().toString());
                ref.child(userUid).child("reports").setValue(userReport);
                Toast.makeText(AddReportActivity.this, "Data added successfully", Toast.LENGTH_SHORT).show();
            }

        });

        btnUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadTask != null && uploadTask.isInProgress()) {
                    Toast.makeText(AddReportActivity.this, "Upload in Progress", Toast.LENGTH_SHORT).show();
                } else {
                    saveData(filePath);
                }
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner1.setAdapter(adapter);


        //date set code
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Log.d("MainActivity", "onDateSet: date: " + month + "/" + dayOfMonth + "/" + year);

                String date = month + "/" + dayOfMonth + "/" + year;
                tvDate.setText(date);

            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQ && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            String path = data.getData().getPath();
            tvFilePathShow.setText(path);
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an image"), PICK_IMAGE_REQ);

    }

    public void saveData(Uri filePath) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading file..");
        progressDialog.setProgress(0);
        progressDialog.show();

        final String fileName = System.currentTimeMillis() + ",";
        StorageReference storageReference = storage.getReference();

        storageReference.child("Reports").child(fileName).putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String url = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

                        DatabaseReference reference = database.getReference();

                        reference.child(fileName).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    Toast.makeText(AddReportActivity.this, "File Successfully Uploaded", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(AddReportActivity.this, "File Upload Unsuccessful", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddReportActivity.this, "File Upload Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });

    }
}
