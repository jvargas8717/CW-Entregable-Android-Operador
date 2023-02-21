package com.devinsure.carwash.android.operador.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.devinsure.carwash.android.operador.R;
import com.devinsure.carwash.android.operador.databinding.FragmentConfigurationBinding;
import com.devinsure.carwash.android.operador.providers.AuthProvider;

public class ConfigurationFragment extends Fragment {

    AuthProvider mAuthProvider;
    private FragmentConfigurationBinding binding;
    private Button mButtonCerrarSesion;
    private View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_configuration, container, false);

        binding = FragmentConfigurationBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        mAuthProvider = new AuthProvider();

        mButtonCerrarSesion = root.findViewById(R.id.btnCerrarSesion);

        mButtonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        return root;
    }

    public void logout() {
        // mTokenProvider.deleteToken(mAuthProvider.getId());
        mAuthProvider.logout();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }
}