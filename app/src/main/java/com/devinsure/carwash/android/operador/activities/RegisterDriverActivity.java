package com.devinsure.carwash.android.operador.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.devinsure.carwash.android.operador.R;
import com.devinsure.carwash.android.operador.includes.MyToolbar;
import com.devinsure.carwash.android.operador.models.Driver;
import com.devinsure.carwash.android.operador.providers.AuthProvider;
import com.devinsure.carwash.android.operador.providers.DriverProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import dmax.dialog.SpotsDialog;

public class RegisterDriverActivity extends AppCompatActivity {

    SharedPreferences mPref;

    AuthProvider mAuthProvider;
    DriverProvider mDriverProvider;

    //views
    Button mButtonRegister;
    TextInputEditText mTextInputName;
    TextInputEditText mTextInputEmail;
    // TextInputEditText mTextInputPassword;
    TextInputEditText mTextInputVehicleBrand;
    TextInputEditText mTextInputVehiclePlate;

    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);

        MyToolbar.show(this, "Registro de lavador", false);

        mDialog = new SpotsDialog.Builder().setContext(RegisterDriverActivity.this).setMessage("Espere un momento").build();

        mAuthProvider = new AuthProvider();
        mDriverProvider = new DriverProvider();

        mButtonRegister = findViewById(R.id.btnRegister);
        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickRegister();
            }
        });

        mTextInputEmail = findViewById(R.id.textInputEmail);
        //mTextInputPassword = findViewById(R.id.textInputPassword);
        mTextInputName = findViewById(R.id.textInputNombreCompleto);
        mTextInputVehicleBrand = findViewById(R.id.textInputVehicleBrand);
        mTextInputVehiclePlate = findViewById(R.id.textInputVehiclePlate);

    }

    private void clickRegister() {
        String name = mTextInputName.getText().toString();
        String email = mTextInputEmail.getText().toString();
        //String password = mTextInputPassword.getText().toString();
        String vehicleBrand = mTextInputVehicleBrand.getText().toString();
        String vehiclePlate = mTextInputVehiclePlate.getText().toString();

        if (!name.isEmpty() && !email.isEmpty() && !vehicleBrand.isEmpty() && !vehiclePlate.isEmpty()){
            mDialog.show();
            Driver driver = new Driver();
            driver.setId(mAuthProvider.getId());
            driver.setName(name);
            driver.setVehicleBrand(vehicleBrand);
            driver.setVehiclePlate(vehiclePlate);
            driver.setEmail(email);
            update(driver);

            //register(name, email, password, vehicleBrand, vehiclePlate);
        }
        else {
            Toast.makeText(this, "Debe ingresar todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    private void register(final String name, final String email, String password, final String vehicleBrand, final String vehiclePlate) {
        mAuthProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                mDialog.hide();

                if (task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                }
                else {
                    Toast.makeText(RegisterDriverActivity.this, "No se pudo guardar el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void update(Driver driver){
        mDriverProvider.update(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Toast.makeText(RegisterDriverActivity.this, "El registro se generó exitosamente", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterDriverActivity.this, MapDriverActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(RegisterDriverActivity.this, "No se pudo registrar el cliente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}