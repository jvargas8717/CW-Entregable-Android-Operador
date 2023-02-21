package com.devinsure.carwash.android.operador.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.devinsure.carwash.android.operador.R;
import com.devinsure.carwash.android.operador.models.Driver;
import com.devinsure.carwash.android.operador.providers.AuthProvider;
import com.devinsure.carwash.android.operador.providers.DriverProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class PhoneAuthActivity extends AppCompatActivity {

    Button mButtonCodeVerification;
    EditText mEditTextCodeVerification;
    String mExtraPhone;

    String mVerificationId;

    AuthProvider mAuthProvider;
    DriverProvider mDriverProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        mButtonCodeVerification = findViewById(R.id.btnCodeVerification);
        mEditTextCodeVerification = findViewById(R.id.editTextCodeVerification);

        mAuthProvider = new AuthProvider();
        mDriverProvider = new DriverProvider();

        mExtraPhone = getIntent().getStringExtra("phone");
        Toast.makeText(this, "Telefono: " + mExtraPhone, Toast.LENGTH_SHORT).show();

        mAuthProvider.sendCodeVerification(mExtraPhone, mCallbacks, PhoneAuthActivity.this);

        mButtonCodeVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = mEditTextCodeVerification.getText().toString();

                if (!code.equals("") && code.length() >= 6) {
                    signIn(code);
                } else {
                    Toast.makeText(PhoneAuthActivity.this, "Debes ingresar el código", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            //CUANDO LA AUTENTICACIÓN SE REALIZA EXITOSAMENTE
            // EL USUARIO HAYA INSERTADO CORRECTAMENTE EL CODIGO DE VERIFICACIÓN
            // O EN CASO DE QUE NUESTRO DISPOSITIVO MOVIL HAYA DETECTADO AUTOMATICAMENTE EL CODIGO

            String code = phoneAuthCredential.getSmsCode();

            if (code != null) {
                mEditTextCodeVerification.setText(code);
                signIn(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            // CUANDO EL ENVIO DEL SMS FALLA
            Toast.makeText(PhoneAuthActivity.this, "Se produjó un error " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            // CODIGO DE VERIFICACIÓN SE ENVIA A TRAVÉS DE SMS DE TEXTO
            super.onCodeSent(verificationId, forceResendingToken);
            Toast.makeText(PhoneAuthActivity.this, "El código se envió", Toast.LENGTH_LONG).show();
            mVerificationId = verificationId;
        }
    };

    private void signIn(String code) {
        mAuthProvider.signInPhone(mVerificationId, code).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    //SIGNIFICA QUE EL USUARIO INICIO SESIÓN EXITOSAMENTE

                    mDriverProvider.getDriver(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {
                                Intent intent = new Intent(PhoneAuthActivity.this, MapDriverActivity.class);
                                startActivity(intent);
                            }
                            else {
                                createInfo();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } else {
                    Toast.makeText(PhoneAuthActivity.this, "No se pudo iniciar sesión", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createInfo() {
        Driver driver = new Driver();
        driver.setId(mAuthProvider.getId());
        driver.setPhone(mExtraPhone);
        mDriverProvider.create(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> taskCreate) {

                if (taskCreate.isSuccessful()) {
                    Intent intent = new Intent(PhoneAuthActivity.this, RegisterDriverActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(PhoneAuthActivity.this, "No se pudo crear la información", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}