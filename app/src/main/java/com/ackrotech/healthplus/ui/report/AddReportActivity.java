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
import androidx.appcompat.widget.Toolbar;

import com.ackrotech.healthplus.R;
import com.ackrotech.healthplus.Utility.DBHelper;
import com.ackrotech.healthplus.Utility.VolleyUtility;
import com.ackrotech.healthplus.data.model.UserReport;
import com.ackrotech.healthplus.ui.main.HomeFragment;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddReportActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQ = 234;
    private TextView tvDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private EditText etCentreName;
    private Spinner testResult;
    private String positiveString = "Positive";
    private String negativeString = "Negative";
    private  String[] items = {positiveString, negativeString};
    private Button btnChooseFile;
    private Button btnSubmit;
    private Button btnUploadFile;
    private Uri filePath;
    private TextView tvFilePathShow;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;
    private static final String KEY_RESULT = "result";
    private static final String KEY_DATE = "date";
    private static final String KEY_TESTCENTRE = "testCentre";
    private String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private Uri imgUri;
    private DatabaseReference ref;
    private UserReport userReport;
    private StorageTask uploadTask;
    private static final String TAG = AddReportActivity.class.getSimpleName();
    private DBHelper dbHelper;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        dbHelper = DBHelper.getInstance(this);
        mAuth = FirebaseAuth.getInstance();


        tvFilePathShow = (TextView) findViewById(R.id.tvFilePath);
        etCentreName = (EditText) findViewById(R.id.etCentreName);
        btnChooseFile = (Button) findViewById(R.id.btnChooseFile);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        tvDate = (TextView) findViewById(R.id.tvDateSelect);
        testResult = (Spinner) findViewById(R.id.test_result);
        userReport = new UserReport();

        ref = FirebaseDatabase.getInstance().getReference().child("users");
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
                String testRes = testResult.getSelectedItem().toString();
                userReport.setCentreName(etCentreName.getText().toString().trim());
                userReport.setDate(tvDate.getText().toString());
                userReport.setReportResult(testResult.getSelectedItem().toString());

                DatabaseReference reportsRef = ref.child(userUid).child("reports").push();
                reportsRef.setValue(userReport);

                if(testRes.equals(positiveString)){
                    notifyAllContactedUsers();
                    updateFirebaseDb(mAuth.getCurrentUser().getUid());
                }

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
        testResult.setAdapter(adapter);


        //date set
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


    private void notifyAllContactedUsers(){
        Log.d(TAG,"in notify all");
        List<String> users = dbHelper.getAllContactedUser();
        for(String userId : users){
            Log.d(TAG,userId);
            sendNotification(userId);
            updateFirebaseDb(userId);
        }
    }

    private void updateFirebaseDb(String userId){
        ref.child(userId).child("in_contact").setValue(true);
    }

    private void sendNotification(String userId) {
        Log.d(TAG, "Sending notification to "+userId);

        String FCM_API = "https://fcm.googleapis.com/fcm/send";
        String serverKey = "key=" + "AAAAoyvohpA:APA91bH_jI6RrT1xCPXPRc-jjrnuU7TyLYhZ-2E3HlwUgAQsC1dBqmfULNeRAYBbn8dGuRAnE2Avux2Ivxtbrog50huSkHNu10wOtVwSrA0jc3-clW5Robde6pW72_9swz00aBQ-HiOP";
        String contentType = "application/json";
        String TOPIC = "/topics/"+userId;
        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            notifcationBody.put("title", "Covid Contact alert");
            notifcationBody.put("message", "You have been in contact with someone who has been tested COVID positive.");

            notification.put("to", TOPIC);
            notification.put("data", notifcationBody);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG, "onResponse: " + response.toString());

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "Request error", Toast.LENGTH_LONG).show();
                            Log.i(TAG, "onErrorResponse: Didn't work");
                        }
                    }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Authorization", serverKey);
                    params.put("Content-Type", contentType);
                    return params;
                }
            };
            VolleyUtility.getInstance(this).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e.getMessage() );
        }
    }

}