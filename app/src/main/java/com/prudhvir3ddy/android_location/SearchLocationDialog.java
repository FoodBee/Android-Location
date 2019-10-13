package com.prudhvir3ddy.android_location;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SearchLocationDialog extends BottomSheetDialogFragment implements PlacesAutoCompleteAdapter.ClickListener, Filterable {


    private static final String PLACE_EXTRA_LATITUDE = "latitude";
    private static final String PLACE_EXTRA_LONGITUDE = "longitude";
    private static final String PLACE_EXTRA_ADDRESS = "address";
    private ArrayList<PlacesAutoCompleteAdapter.PlaceAutocomplete> mResultList;
    private static final String TAG = SearchLocationDialog.class.getSimpleName();

    private PlacesClient placesClient;
    private PlacesAutoCompleteAdapter placesAutoCompleteAdapter;
    private ProgressBar locBar;
    private RecyclerView recyclerView;
    private TextView mGetLocationTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_custom_ui, container, false);

        placesClient = com.google.android.libraries.places.api.Places.createClient(getContext());
        mGetLocationTextView = view.findViewById(R.id.get_user_location);
        EditText mSearchLocationEditText = view.findViewById(R.id.search);
        locBar = view.findViewById(R.id.pb_location);

        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        placesAutoCompleteAdapter = new PlacesAutoCompleteAdapter(getContext(),mResultList,this);


        mGetLocationTextView.setOnClickListener(v -> ((LocationMainActivity) getActivity()).getLocation());

        mSearchLocationEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                mGetLocationTextView.setVisibility(View.VISIBLE);
                if (s.toString().length() > 2) {
                    locBar.setVisibility(View.VISIBLE);
                    getFilter().filter(s.toString());
                }
            }
        });
        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void click(Place place) {
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        intent.putExtra(PLACE_EXTRA_LATITUDE, place.getLatLng().latitude);
        intent.putExtra(PLACE_EXTRA_LONGITUDE, place.getLatLng().longitude);
        intent.putExtra(PLACE_EXTRA_ADDRESS, place.getAddress());
        startActivity(intent);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                // Skip the autocomplete query if no constraints are given.
                if (constraint != null) {
                    // Query the autocomplete API for the (constraint) search string.
                    mResultList = new ArrayList<>();
                    mResultList = getPredictions(constraint);

                    if (mResultList != null) {
                        // The API successfully returned results.
                        results.values = mResultList;
                        results.count = mResultList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    placesAutoCompleteAdapter = new PlacesAutoCompleteAdapter(getContext(), mResultList, SearchLocationDialog.this);

                    recyclerView.setAdapter(placesAutoCompleteAdapter);
                    locBar.setVisibility(View.GONE);
                    mGetLocationTextView.setVisibility(View.GONE);

                } else {
                    // The API did not return any results, invalidate the data set.
                    //notifyDataSetInvalidated();
                }
            }
        };
    }

    private ArrayList<PlacesAutoCompleteAdapter.PlaceAutocomplete> getPredictions(CharSequence constraint) {

        final ArrayList<PlacesAutoCompleteAdapter.PlaceAutocomplete> resultList = new ArrayList<>();

        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                //.setLocationBias(bounds)
                //.setCountry("BD")
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(constraint.toString())
                .build();

        Task<FindAutocompletePredictionsResponse> autocompletePredictions = placesClient.findAutocompletePredictions(request);

        // This method should have been called off the main UI thread. Block and wait for at most
        // 60s for a result from the API.
        try {

            Tasks.await(autocompletePredictions, 60, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }

        if (autocompletePredictions.isSuccessful()) {
            FindAutocompletePredictionsResponse findAutocompletePredictionsResponse = autocompletePredictions.getResult();
            if (findAutocompletePredictionsResponse != null)
                for (AutocompletePrediction prediction : findAutocompletePredictionsResponse.getAutocompletePredictions()) {
                    Log.i(TAG, prediction.getPlaceId());
                    resultList.add(new PlacesAutoCompleteAdapter.PlaceAutocomplete(prediction.getPlaceId(), prediction.getPrimaryText(null).toString(), prediction.getFullText(null).toString()));
                }

            return resultList;
        } else {
            return resultList;
        }

    }
}
