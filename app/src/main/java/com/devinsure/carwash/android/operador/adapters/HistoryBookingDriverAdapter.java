package com.devinsure.carwash.android.operador.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devinsure.carwash.android.operador.R;
import com.devinsure.carwash.android.operador.activities.HistoryBookingDetailDriverActivity;
import com.devinsure.carwash.android.operador.models.HistoryBooking;
import com.devinsure.carwash.android.operador.providers.ClientProvider;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class HistoryBookingDriverAdapter extends FirebaseRecyclerAdapter<HistoryBooking, HistoryBookingDriverAdapter.ViewHolder> {

    private ClientProvider mClientProvider;
    private Context mContext;

    public HistoryBookingDriverAdapter(FirebaseRecyclerOptions<HistoryBooking> options, Context context){
        super(options);
        mClientProvider = new ClientProvider();
        mContext = context;
    }

    //metodo para establecer valores del carview
    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull HistoryBooking historyBooking) {

        final String idHistory = getRef(position).getKey();

        holder.textViewOrigin.setText(historyBooking.getOrigin());
        //AQUI DESTINO
        //holder.textViewDestination.setText(historyBooking.getDestination());
        holder.textViewCalification.setText(String.valueOf(historyBooking.getCalificationDriver()));
        mClientProvider.getClient(historyBooking.getIdClient()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    String name = snapshot.child("name").getValue().toString();
                    String image = "";

                    if (snapshot.hasChild("image")){

                        image = snapshot.child("image").getValue().toString();
                        Picasso.with(mContext).load(image).into(holder.imageViewHistoryBooking);

                    }

                    holder.textViewName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, HistoryBookingDetailDriverActivity.class);
                intent.putExtra("idHistoryBooking", idHistory);
                mContext.startActivity(intent);
            }
        });
    }

    //metodo para instanciar el layout creado
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_history_booking, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView textViewName;
        private TextView textViewOrigin;
        private TextView textViewCalification;
        private ImageView imageViewHistoryBooking;
        private View mView;
        //AQUI
        //private TextView textViewDestination;

        public ViewHolder(View view){
            super(view);
            mView = view;
            textViewName = view.findViewById(R.id.textViewName);
            textViewOrigin = view.findViewById(R.id.textViewOrigin);
            textViewCalification = view.findViewById(R.id.textViewCalification);
            imageViewHistoryBooking = view.findViewById(R.id.imageViewHistoryBooking);

            //AQUI DESTINO
            // textViewDestination = view.findViewById(R.id.textViewDestination);
        }
    }
}
