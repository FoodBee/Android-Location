package com.prudhvir3ddy.android_location;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PlacesAutoCompleteAdapter extends RecyclerView.Adapter<PlacesAutoCompleteAdapter.PlacesAutoCompleteHolder> {

    private final ArrayList<PlaceAutocomplete> mResultList;

    private final Context mContext;
    private final PlacesClient placesClient;
    private final ClickListener clickListener;

    public PlacesAutoCompleteAdapter(Context context,ArrayList<PlaceAutocomplete> mResultList,ClickListener clickListener) {
        mContext = context;
        this.mResultList = mResultList;
        this.clickListener = clickListener;
        placesClient = com.google.android.libraries.places.api.Places.createClient(context);
    }


    @NonNull
    @Override
    public PlacesAutoCompleteHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = layoutInflater.inflate(R.layout.place_recycler_item_layout, viewGroup, false);
        return new PlacesAutoCompleteHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlacesAutoCompleteHolder mPredictionHolder, final int i) {
        mPredictionHolder.address.setText(mResultList.get(i).address);
        mPredictionHolder.area.setText(mResultList.get(i).area);

    }

    @Override
    public int getItemCount() {
        return mResultList.size();
    }

    /**
     * Holder for Places Geo Data Autocomplete API results.
     */
    public static class PlaceAutocomplete {

        final String placeId;
        final String address;
        final String area;

        PlaceAutocomplete(String placeId, String area, String address) {
            this.placeId = placeId;
            this.area = area;
            this.address = address;
        }

        @Override
        public String toString() {
            return area.toString();
        }
    }

    public interface ClickListener {
        void click(Place place);
    }

    public class PlacesAutoCompleteHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView address;
        private final TextView area;

        PlacesAutoCompleteHolder(View itemView) {
            super(itemView);
            area = itemView.findViewById(R.id.place_area);
            address = itemView.findViewById(R.id.place_address);
            CardView mRow = itemView.findViewById(R.id.place_item_view);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            PlaceAutocomplete item = mResultList.get(getAdapterPosition());
            if (v.getId() == R.id.place_item_view) {

                String placeId = String.valueOf(item.placeId);

                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
                FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(request).addOnSuccessListener(response -> {
                    Place place = response.getPlace();
                    clickListener.click(place);

                }).addOnFailureListener(exception -> {
                    if (exception instanceof ApiException) {
                        Toast.makeText(mContext, exception.getMessage() + "", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}