package org.phonen.fitguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "FB tag";
    private FirebaseAuth mAuth;
    //view
    EditText password;
    EditText email;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.emailTextInput);
        password = findViewById(R.id.passwordTextInput);
        login = findViewById(R.id.loginButton);
        login.setOnClickListener(v -> {
            String em = email.getText().toString();
            String pas = password.getText().toString();
            if(validateForm(em,pas))
            {
                signIn(em,pas);
            }

        });
    }
    private  Boolean validateForm(String email, String password)
    {
        Pattern pattern = Pattern.compile("([a-z0-9]+(\\.?[a-z0-9])*)+@(([a-z]+)\\.([a-z]+))+");
     if(email !=null && password!= null)
     {
         if(!email.isEmpty()&& !password.isEmpty())
         {
             Matcher mather = pattern.matcher(email);
             if(mather.find() && password.length()>5)
             {
                 return true;
             }
             else
             {
                 if(!mather.find())
                 {
                     this.email.setError("Correo inválido");
                 }
                 if(password.length()<5)
                 {
                     this.password.setError("La contraseña debe de tener mas de 5 caracteres");
                 }
             }
         }

     }
     return false;

    }
    private void signIn(String email,String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(mAuth.getCurrentUser());

    }

    private void updateUI(FirebaseUser currentUser)
    {
        if(currentUser!=null)
        {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        }
        else
        {
            email.setText("");
            password.setText("");
        }
    }
}