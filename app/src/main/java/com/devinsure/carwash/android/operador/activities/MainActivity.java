package com.devinsure.carwash.android.operador.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devinsure.carwash.android.operador.R;
import com.devinsure.carwash.android.operador.providers.AuthProvider;
import com.hbb20.CountryCodePicker;

public class MainActivity extends AppCompatActivity {

    Button mButtonGoToLogin;
    CountryCodePicker mCountryCode;
    EditText mEditTextPhone;
    AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_CarwashOperador);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option_auth);

        /*mCountryCode = findViewById(R.id.ccp);
        mEditTextPhone = findViewById(R.id.editTextPhone);*/

        //mButtonGoToLogin = findViewById(R.id.btnGoToLogin);

        mAuthProvider = new AuthProvider();

        /*mButtonGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLogin();
            }
        });*/

    }

    /*private void goToLogin() {
        String code = mCountryCode.getSelectedCountryCodeWithPlus();
        String phone = mEditTextPhone.getText().toString();

        if (!phone.equals("")){
            Intent intent = new Intent(MainActivity.this, PhoneAuthActivity.class);
            intent.putExtra("phone", code + phone);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Debes ingresar el teléfono", Toast.LENGTH_SHORT).show();
        }
    }*/

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuthProvider.existSession()){
            Intent intent = new Intent(MainActivity.this, MapDriverActivity.class);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }
}