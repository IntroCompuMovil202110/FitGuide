package org.phonen.fitguide;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.phonen.fitguide.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.phonen.fitguide.utils.References.PATH_USERS;

public class RegisterActivity extends AppCompatActivity {

    final private Calendar calendarInstance = Calendar.getInstance();

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    DatabaseReference myRef;
    EditText name;
    EditText lastName;
    EditText email;
    EditText date;
    EditText userName;
    EditText phone;
    EditText password;
    EditText height;
    EditText weight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        DatePickerDialog.OnDateSetListener dateDialog = (view, year, month, dayOfMonth) -> {
            calendarInstance.set(Calendar.YEAR, year);
            calendarInstance.set(Calendar.MONTH, month);
            calendarInstance.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(PATH_USERS);

        name = findViewById(R.id.editTextTextName);
        lastName = findViewById(R.id.editTextLastName);
        email = findViewById(R.id.editTextTextEmail);
        userName = findViewById(R.id.editTextTextUsername);
        phone = findViewById(R.id.editTextPhone);
        password = findViewById(R.id.editTextTextPassword);
        height = findViewById(R.id.editTexHeight);
        weight = findViewById(R.id.editTextWeight);
        date = findViewById(R.id.editTextDate);

        date.setOnClickListener(v -> new DatePickerDialog(RegisterActivity.this, dateDialog,
                calendarInstance.get(Calendar.YEAR),
                calendarInstance.get(Calendar.MONTH),
                calendarInstance.get(Calendar.DAY_OF_MONTH))
                    .show());


    }

    private void updateLabel() {
        String format = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());

        date.setText(simpleDateFormat.format(calendarInstance.getTime()));
    }

    private Boolean validateForm(String emails, String passwords, String names, String lastNames,
                                 String userNames, String phones, String he, String we, String date) {
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
            if (passwords.isEmpty()) {
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
            if (date.isEmpty()) {
                this.date.setError("información obligatoria");
                ret = false;
            }
            if (ret) {
                if (mather.find() && passwords.length() > 5 && dateValidation(date)) {
                    return true;
                } else {
                    if (!mather.find()) {
                        this.email.setError("Correo inválido");
                    }
                    if (passwords.length() < 5) {
                        this.password.setError("La contraseña debe de tener mas de 5 caracteres");
                    }
                    if (!dateValidation(date)) {
                        this.date.setError("formato de fecha incorrecto");
                    }
                    ret = false;
                }

            }
        } else {
            ret = false;
        }


        return ret;
    }

    public void register(View view) {
        String names = name.getText().toString();
        String lastNames = lastName.getText().toString();
        String emails = email.getText().toString();
        String userNames = userName.getText().toString();
        String phones = phone.getText().toString();
        String passwords = password.getText().toString();
        String he = height.getText().toString();
        String we = weight.getText().toString();
        String date = this.date.getText().toString();
        if (validateForm(emails, passwords, names, lastNames, userNames, phones, he, we, date)) {
            mAuth.createUserWithEmailAndPassword(emails, passwords).addOnCompleteListener(task -> {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    User userN = new User();
                    userN.setName(names);
                    userN.setLastName(lastNames);
                    userN.setPhone(phones);
                    userN.setDate(date);
                    userN.setHeight(he);
                    userN.setUserName(userNames);
                    userN.setWeight(we);
                    String key = user.getUid();
                    myRef = database.getReference(PATH_USERS + key);
                    myRef.setValue(userN);
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                }

            });


        }


    }

    public static boolean dateValidation(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setLenient(false);
            dateFormat.parse(date);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

}