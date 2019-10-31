package com.choicemartstore.computerzone.dtracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class signin extends AppCompatActivity {
View log;
    private static final String TAG = signin.class.getSimpleName();
    private ProgressDialog dialog;

EditText use,pw;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        mAuth = FirebaseAuth.getInstance();
        log = (View)findViewById(R.id.signin);
        use = (EditText)findViewById(R.id.email);
        pw = (EditText)findViewById(R.id.pw);

        log.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ProgressDialog pd = new ProgressDialog(signin.this);
            pd.setMessage("loading");
            pd.show();
         signinn();

        }
    });



    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(mAuth.getCurrentUser() != null){

            // Finishing current Login Activity.
            finish();

            // Opening UserProfileActivity .
            Intent intent = new Intent(signin.this, Home.class);
            startActivity(intent);
        }

    }
public void signinn(){

    String users = use.getText().toString();
    String paswords = pw.getText().toString();
    if(!users.isEmpty() && !paswords.isEmpty()){    mAuth.signInWithEmailAndPassword(users, paswords)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        startActivity(new Intent(signin.this,Home.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(signin.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();

                    }


                }
            });

}
    }

}
