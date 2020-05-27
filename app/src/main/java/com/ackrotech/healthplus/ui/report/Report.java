package com.ackrotech.healthplus.ui;

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
import com.ackrotech.healthplus.UserReport;
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

/**
 * Created by yashp on 5/21/2020.
 */
public class Report extends AppCompatActivity {
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
    ProgressDialog progressDialog;
    public String spinnerResult;
    private static final String KEY_RESULT = "result";
    private static final String KEY_DATE = "date";
    private static final String KEY_TESTCENTRE = "testCentre";

    //private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public Uri imgUri;
    DatabaseReference ref;
    UserReport userReport;
    StorageReference mStorageref;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();


        tvFilePathShow = (TextView) findViewById(R.id.tvFilePath);
        etCentreName = (EditText) findViewById(R.id.etCentreName);
        btnChooseFile = (Button) findViewById(R.id.btnChooseFile);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        tvDate = (TextView) findViewById(R.id.tvDateSelect);
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        userReport = new UserReport();

        ref = FirebaseDatabase.getInstance().getReference().child("UserReport");
        mStorageref = FirebaseStorage.getInstance().getReference("ImageReport");
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

                DatePickerDialog dialog = new DatePickerDialog(Report.this, android.R.style.Theme_Material_Dialog_MinWidth,
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
                ref.child("User1").setValue(userReport);
                Toast.makeText(Report.this, "Data added successfully", Toast.LENGTH_SHORT).show();
            }

        });

        btnUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadTask != null && uploadTask.isInProgress()) {
                    Toast.makeText(Report.this, "Upload in Progress", Toast.LENGTH_SHORT).show();
                } else {
                    saveData(filePath);
                }
            }
        });


        //spinner code for test results

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner1.setAdapter(adapter);

//        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                spinnerResult = parent.getItemAtPosition(position).toString();
//            }
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });

        //spinnerResult = spinner1.getSelectedItem().toString();


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

//        private String getExtension(Uri uri) {
//        ContentResolver cr = getContentResolver();
//        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
//        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
//    }
//
//    private void fileUploader(){
//        StorageReference sref = mStorageref.child(System.currentTimeMillis() + "," + getExtension(imgUri));
//
//        uploadTask = sref.putFile(imgUri)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        // Get a URL to the uploaded content
//                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                        Toast.makeText(Report.this, "Report Uploaded Successfully", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        // Handle unsuccessful uploads
//                        // ...
//                        Toast.makeText(Report.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//    }


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

    // SAME AS FILEUPLOADER
    public void saveData(Uri filePath) {
        //code to upload pdf to database
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
                                    Toast.makeText(Report.this, "File Successfully Uploaded", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(Report.this, "File Upload Unsuccessful", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Report.this, "File Upload Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });


//        String result = spinner1.getSelectedItem().toString();
//        String date = tvDate.getText().toString();
//        String testCentre = etCentreName.getText().toString();

//        Map<String, Object> map = new HashMap<>();
//        map.put(KEY_RESULT, spinnerResult);
//        map.put(KEY_DATE, date);
//        map.put(KEY_TESTCENTRE, testCentre);
//
//        db.collection("Reports").document("HealthPlus Report").set(map)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(Report.this,"Data Saved", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(Report.this,"Error!", Toast.LENGTH_SHORT).show();
//                    }
//                });
    }
}





