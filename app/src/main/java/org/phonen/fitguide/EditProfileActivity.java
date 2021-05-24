package org.phonen.fitguide;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.phonen.fitguide.model.User;
import org.phonen.fitguide.utils.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditProfileActivity extends AppCompatActivity {

    User user;
    private String correoCop;
    private String uId;
    boolean changePass;
    boolean pasVerif;
    //firebase
    private FirebaseAuth mAuth;
    private FirebaseUser fUser;
    FirebaseDatabase database;
    DatabaseReference myRef;
    //components
    EditText name;
    EditText lastName;
    EditText email;
    EditText userName;
    EditText phone;
    EditText password;
    EditText password2;
    EditText height;
    EditText weight;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);
        //navBarSettings();
        //firebase
        mAuth = FirebaseAuth.getInstance();
        correoCop = mAuth.getCurrentUser().getEmail();
        database = FirebaseDatabase.getInstance();
        //-----------------------
        name = findViewById(R.id.editTextTextName);
        lastName = findViewById(R.id.editTextLastName);
        email = findViewById(R.id.editTextTextEmail);
        userName = findViewById(R.id.editTextTextUsername);
        phone = findViewById(R.id.editTextPhone);
        password = findViewById(R.id.editTextTextPassword);
        height = findViewById(R.id.editTexHeight);
        weight = findViewById(R.id.editTextWeight);
        password2 = findViewById(R.id.editTextConfirmPassword);

        uId = mAuth.getUid();
        myRef = database.getReference(Constants.USERS_PATH + uId);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                name.setText(user.getName());
                lastName.setText(user.getLastName());
                email.setText(mAuth.getCurrentUser().getEmail());
                userName.setText(user.getUserName());
                phone.setText(user.getPhone());
                // password.setText(mAuth.getCurrentUser().pa);
                height.setText(user.getHeight());
                weight.setText(user.getWeight());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void navBarSettings() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.profileActivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.challengesActivity:
                    startActivity(new Intent(getApplicationContext(), ChallengesActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.feedActivity:
                    startActivity(new Intent(getApplicationContext(), FeedActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.profileActivity:
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.startActivity:
                    startActivity(new Intent(getApplicationContext(), StartActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
    }


    private Boolean validateForm(String emails, String passwords, String names, String lastNames,
                                 String userNames, String phones, String he, String we, String password2) {
        Boolean ret = true;
        Pattern pattern = Pattern.compile("([a-z0-9]+(\\.?[a-z0-9])*)+@(([a-z]+)\\.([a-z]+))+");
        Matcher mather = pattern.matcher(emails);
        //validación de datos
        if (names != null && lastNames != null && emails != null && userNames != null
                && phones != null && passwords != null && he != null && we != null) {
            if (names.isEmpty()) {
                name.setError("Información obligatoria");
                ret = false;
            }
            if (lastNames.isEmpty()) {
                lastName.setError("Información obligatoria");
                ret = false;
            }
            if (emails.isEmpty()) {
                email.setError("Información obligatoria");
                ret = false;
            }
            if (userNames.isEmpty()) {
                userName.setError("Información obligatoria");
                ret = false;
            }
            if (phones.isEmpty()) {
                phone.setError("Información obligatoria");
                ret = false;
            }

            if (passwords.isEmpty() && !password2.isEmpty()) {
                password.setError("Información obligatoria");
                ret = false;
            }

            if (he.isEmpty()) {
                height.setError("Información obligatoria");
                //    float heights = Float.parseFloat(height.getText().toString());
                ret = false;

            }
            if (we.isEmpty()) {
                weight.setError("Información obligatoria");
                //float weights = Float.parseFloat(weight.getText().toString());
                ret = false;
            }
            if (!mather.find()) {
                this.email.setError("Correo inválido");
                ret = false;
            }

        }
        return ret;
    }
    public  boolean passwordVerificate(String passwords, String password2)
    {
        boolean passUpdate = false;
        if (passwords.isEmpty() && password2.isEmpty())
        {
            passUpdate = false;
        } else {
            if (password2.length() < 5) {
                this.password2.setError("La contraseña debe de tener mas de 5 carácteres");

            }
            if (passwords.length() < 5) {
                this.password.setError("La contraseña debe de tener mas de 5 carácteres");

            }
            if (passwords.length() > 5 && password2.length() > 5) {
                passUpdate = true;
            }
        }
        return  passUpdate;
    }

    public void editProfile(View view) {

        String names = name.getText().toString();
        String lastNames = lastName.getText().toString();
        String emails = email.getText().toString();
        String userNames = userName.getText().toString();
        String phones = phone.getText().toString();
        String passwords = password.getText().toString();
        String he = height.getText().toString();
        String we = weight.getText().toString();
        String password2 = this.password2.getText().toString();
        pasVerif= true;
         changePass =true;

        if (validateForm(emails, passwords, names, lastNames, userNames, phones, he, we, password2))
        {
            fUser = FirebaseAuth.getInstance().getCurrentUser();
            String email = fUser.getEmail();
            if (user != null) {

                user.setName(names);
                user.setLastName(lastNames);
                user.setPhone(phones);
                user.setHeight(he);
                user.setUserName(userNames);
                user.setWeight(we);

                //Cambio de contraseña
                if(!password2.isEmpty())
                { changePass=false;
                    if (passwordVerificate(passwords,password2))
                    {

                        AuthCredential credential = EmailAuthProvider.getCredential(email, passwords);
                        fUser.reauthenticate(credential).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                fUser.updatePassword(password2).addOnCompleteListener(task12 -> {
                                    if (!task12.isSuccessful()) {
                                        Toast.makeText(EditProfileActivity.this, "Autenticación fallida", Toast.LENGTH_SHORT).show();
                                        password.setError("Contraseña inválida");
                                    } else {
                                        changePass = true;
                                        Toast.makeText(EditProfileActivity.this, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(EditProfileActivity.this, "Autenticación fallida", Toast.LENGTH_SHORT).show();
                                password.setError("Contraseña inválida");
                            }
                        });
                }

                }
                //cambio de correo
                if (!email.equals(emails)) {
                    pasVerif=false;
                    if(passwordNotEmpty(passwords))
                    {
                        AuthCredential credential = EmailAuthProvider.getCredential(email, passwords);
                        if(!fUser.reauthenticate(credential).isSuccessful())
                        {
                            fUser.reauthenticate(credential).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    fUser.updateEmail(emails).addOnCompleteListener(task1 -> {
                                        pasVerif = true;
                                        if (!task1.isSuccessful()) {
                                            Toast.makeText(EditProfileActivity.this, "Autenticación fallida", Toast.LENGTH_SHORT).show();
                                        } else {
                                            pasVerif = true;
                                            Toast.makeText(EditProfileActivity.this, "Correo actualizado", Toast.LENGTH_SHORT).show();

                                        }
                                    });

                                } else {
                                    Toast.makeText(EditProfileActivity.this, "Autenticación fallida", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                        else
                        {
                            this.password.setError("Contraseña incorrecta");
                            this.email.setError("Por favor vuelva a ingresar el nuevo correo");
                            Toast.makeText(EditProfileActivity.this, "Datos incorrectos", Toast.LENGTH_SHORT).show();
                            this.email.setText(correoCop);
                        }

                    }

                }
                //Cambio de otros datos de usuario
                myRef = database.getReference(Constants.USERS_PATH + uId);
                myRef.setValue(user);
            }


        }

        if(pasVerif)
        {
            Log.i("email complete","pasverif");
        }
        if(changePass)
        {
            Log.i("psw complete","changepass");

        }
        if( changePass)
        {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        }

        overridePendingTransition(0, 0);
    }

    private boolean passwordNotEmpty(String passwords)
    {
        if(passwords.isEmpty())
        {
            email.setError("Debe llenar el espacio de CONTRASEÑA ACTUAL para cambiar el correo");
            return false;
        }
        return true;
    }
}