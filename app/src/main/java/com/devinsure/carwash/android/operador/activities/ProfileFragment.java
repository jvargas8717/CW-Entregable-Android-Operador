package com.devinsure.carwash.android.operador.activities;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devinsure.carwash.android.operador.R;
import com.devinsure.carwash.android.operador.databinding.FragmentProfileBinding;
import com.devinsure.carwash.android.operador.models.Driver;
import com.devinsure.carwash.android.operador.providers.AuthProvider;
import com.devinsure.carwash.android.operador.providers.DriverProvider;
import com.devinsure.carwash.android.operador.providers.ImagesProvider;
import com.devinsure.carwash.android.operador.utils.FileUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private ImageView mImageViewProfile;
    private Button mButtonUpdate;
    private TextView mTextViewName;
    private TextView mTextViewVehicleBrand;
    private TextView mTextViewVehiclePlate;

    private DriverProvider mDriverProvider;
    private AuthProvider mAuthProvider;
    private ImagesProvider mImagesProvider;

    private File mImageFile;
    private String mImage;

    private String mName;
    private String mVehicleBrand;
    private String mVehiclePlate;

    private final int GALLERY_REQUEST = 1;
    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_profile, container, false);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mImageViewProfile = root.findViewById(R.id.imageViewProfileDriver);
        mButtonUpdate = root.findViewById(R.id.btnUpdateProfileDriver);
        mTextViewName = root.findViewById(R.id.textInputNombreCompletoDriver);
        mTextViewVehicleBrand = root.findViewById(R.id.textInputVehicleBrand);
        mTextViewVehiclePlate = root.findViewById(R.id.textInputVehiclePlate);

        mDriverProvider = new DriverProvider();
        mAuthProvider = new AuthProvider();
        mImagesProvider = new ImagesProvider("driver_images");

        mProgressDialog = new ProgressDialog(getContext());

        getDriverInfo();

        mImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });

        return root;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && requestCode == RESULT_OK) {
            try {
                mImageFile = FileUtil.from(getContext(), data.getData());
                mImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            } catch (Exception e) {
                Log.d("ERROR", "Mensaje: " + e.getMessage());
            }
        }
    }

    private void getDriverInfo(){
        //addListenerForSingleValueEvent evento para obtener una sola vez los datos solicitados
        mDriverProvider.getDriver(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue().toString();
                    String vehicleBrand = snapshot.child("vehicleBrand").getValue().toString();
                    String vehiclePlate = snapshot.child("vehiclePlate").getValue().toString();
                    String image = "";

                    if (snapshot.hasChild("image")){
                        image = snapshot.child("image").getValue().toString();
                        Picasso.with(getContext()).load(image).into(mImageViewProfile);
                    }
                    mTextViewName.setText(name);
                    mTextViewVehicleBrand.setText(vehicleBrand);
                    mTextViewVehiclePlate.setText(vehiclePlate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateProfile() {
        mName = mTextViewName.getText().toString();
        mVehicleBrand = mTextViewVehicleBrand.getText().toString();
        mVehiclePlate = mTextViewVehiclePlate.getText().toString();

        if (!mName.equals("") && mImageFile != null) {
            mProgressDialog.setMessage("Espere un momento...");
            //para que usuario no pueda cancelar el proceso
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            saveImage();
        }
        else {
            Toast.makeText(getContext(), "Ingresa la imagen y el nombre", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage() {
        mImagesProvider.saveImage(getContext(), mImageFile, mAuthProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    mImagesProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String image = uri.toString();
                            Driver driver = new Driver();
                            driver.setImage(image);
                            driver.setName(mName);
                            driver.setVehicleBrand(mVehicleBrand);
                            driver.setVehiclePlate(mVehiclePlate);
                            driver.setId(mAuthProvider.getId());
                            mDriverProvider.update(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(getContext(), "Su información se actualizó correctamente", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
                else {
                    Toast.makeText(getContext(), "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}