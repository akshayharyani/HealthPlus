package com.ackrotech.healthplus.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ackrotech.healthplus.ui.nearbyCentres.NearByTestCenterActivity;
import com.ackrotech.healthplus.R;
import com.ackrotech.healthplus.ui.report.AddReportActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private TextView  reportCountText, encounterCountText, encountersHeadingText;
    private FirebaseAuth mAuth;
    private static final String TAG = HomeFragment.class.getSimpleName();
    private DatabaseReference ref;
    private Boolean is_contacted = false;
    private LottieAnimationView animationView;
    private int reportsCount = 0, encountersCount = 0;
    private CardView reportCard, testingCentreCard, encounterCard;
    private ImageView encounterArrow;

    public static HomeFragment newInstance() {
        Bundle args = new Bundle();
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mAuth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();

        animationView = view.findViewById(R.id.animationView);

        reportCountText = view.findViewById(R.id.reports_count);
        encounterCountText = view.findViewById(R.id.encounters_count);
        encountersHeadingText = view.findViewById(R.id.encounters_heading_text);

        encounterArrow = view.findViewById(R.id.encounter_arrow);

        reportCard = view.findViewById(R.id.reports_card);
        reportCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddReportActivity.class));
            }
        });

        testingCentreCard = view.findViewById(R.id.testing_centre_card);
        testingCentreCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NearByTestCenterActivity.class));
            }
        });

        encounterCard = view.findViewById(R.id.encounters_card);

        try {
            String path = "users/"+mAuth.getCurrentUser().getUid();
            ref = FirebaseDatabase.getInstance().getReference(path);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    is_contacted = (Boolean) dataSnapshot.child("in_contact").getValue();
                    reportsCount = (int)  dataSnapshot.child("reports").getChildrenCount();
                    reportsCount -= 1;
                    if(is_contacted){
                        testingCentreCard.setVisibility(View.VISIBLE);
                        encounterCard.setCardBackgroundColor(getResources().getColor(R.color.encounter_bg_alert));
                        animationView.setAnimation(R.raw.health_error);
                        encountersHeadingText.setTextColor(getResources().getColor(R.color.encounter_header_alert));
                        encounterArrow.setColorFilter(getResources().getColor(R.color.encounter_header_alert));
                        encountersCount = 1;
                    }else{
                        animationView.setAnimation(R.raw.health_ok);
                        encounterCard.setCardBackgroundColor(getResources().getColor(R.color.encounter_bg));

                    }
                    animationView.playAnimation();
                    reportCountText.setText(String.valueOf(reportsCount));
                    encounterCountText.setText(String.valueOf(encountersCount));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

            });

        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }

    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }



}

