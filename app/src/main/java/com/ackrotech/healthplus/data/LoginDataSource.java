package com.ackrotech.healthplus.data;

import android.util.Log;
import android.widget.Toast;

import com.ackrotech.healthplus.SaveSharedPreference;
import com.ackrotech.healthplus.data.model.LoggedInUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.concurrent.Executor;

import javax.security.auth.login.LoginException;

import androidx.annotation.NonNull;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private FirebaseAuth mAuth;
    private String TAG = "LoginDataSource";
    private LoggedInUser loggedInUser;


    public Result<LoggedInUser> login(final String email, String password) {

        try {
            // TODO: handle loggedInUser authentication
            mAuth = FirebaseAuth.getInstance();
            final boolean[] flag = {true};

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                loggedInUser = new LoggedInUser(user.getUid(), user.getDisplayName());
                                flag[0] = true;
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                flag[0] = false;
                            }
                        }

                    });
            if(flag[0]){
                return new Result.Success<>(loggedInUser);
            }else {
                return new Result.Error(new LoginException());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}
