package com.devinsure.carwash.android.operador.providers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.devinsure.carwash.android.operador.models.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;


public class TokenProvider {

    DatabaseReference mDatabase;

    public TokenProvider(){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("tokens");
    }

    public void create(final String idUser){

        //si es nulo el id
        if (idUser == null) return;

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isComplete()){
                    Token token = new Token(task.getResult());
                    Log.d("CREATE","Token: " + task.getResult());
                    mDatabase.child(idUser).setValue(token);
                }
            }
        });
    }

    /*public void create(final String idUser) {
        if (idUser == null) return;
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                Token token = new Token(instanceIdResult.getToken());
                mDatabase.child(idUser).setValue(token);
            }
        });
    }*/

    public DatabaseReference getToken(String idUser){
        return mDatabase.child(idUser);
    }
}
