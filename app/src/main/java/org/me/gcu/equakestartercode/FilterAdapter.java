package org.me.gcu.equakestartercode;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * @author Madani Napaul | S1903342
 */

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.Holder> {
    private String TAG = "theF";
    private Context context;
    private List<Item> itemList;
    private int maxIndex;
    private int minIndex;
    private int maxLatId;
    private int minLatID;
    private int maxLongId;
    private int minLongId;
    private boolean isFiltered;

    public FilterAdapter(boolean isFiltered, Context context, List<Item> itemList, int maxIndex, int minIndex, int maxLatId, int minLatID, int maxLongId, int minLongId) {
        Log.d(getTAG(), "FilterAdapter: itemList: " + itemList.size());
        this.setFiltered(isFiltered);
        this.setContext(context);
        this.setItemList(itemList);
        this.setMaxIndex(maxIndex);
        this.setMinIndex(minIndex);
        this.setMaxLatId(maxLatId);
        this.setMinLatID(minLatID);
        this.setMaxLongId(maxLongId);
        this.setMinLongId(minLongId);
    }

    /*
     It creates a new ViewHolder object whenever the RecyclerView needs a new one. At this stage the row layout
     is inflated and passed to the ViewHolder object where each child view is found and stored.
    */
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_filter, parent, false);
        return new Holder(view);
    }

    /*
      It takes the ViewHolder object and set a proper list data for the particular row on the views inside it.
      As such, the list is sorted by magnitude, and for each magnitude range, a specific color is set to a card
      which is a tag to display the earthquake's intensity.

      If the list has been filtered by date, then it will display the appropriate information while maintaining
      the color-coded approach to the cards. When card is clicked, a dialog shows up with more details about that
      specific earthquake.

      NOTE: The values used here are simply for demonstration purposes (Show colors), otherwise the appropriate
      and factual ranges should be used.
    */
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

//        set values on textViews
        holder.textViewMagnitude.setText("Mw " + getItemList().get(position).getMagnitude());
        holder.textViewArea.setText(getItemList().get(position).getLocation());

//        change background color according to magnitude
        double magnitude = getItemList().get(position).getMagnitude();
        if (magnitude <= 1.0 && magnitude > 0.0) {
            holder.textViewTag.setText("LOW");
            holder.cardTag.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.light));
        } else if (magnitude <= 2.0 && magnitude >= 1.1) {
            holder.textViewTag.setText("MODERATE");
            holder.cardTag.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.moderate));
        } else if (magnitude <= 3.0 && magnitude >= 2.1) {
            holder.textViewTag.setText("STRONG");
            holder.cardTag.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.strong));
        } else if (magnitude >= 3.1) {
            holder.textViewTag.setText("MAJOR");
            holder.cardTag.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.great));
        } else {
            holder.cardTag.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.minor));
        }

        if (isFiltered()) {
            if (getItemList().get(position).getId() == getMinIndex()) {
                holder.textViewDepth.setVisibility(View.VISIBLE);
                holder.textViewDepth.setText("SHALLOWEST: " + getItemList().get(position).getDepth() + " KM");
            } else if (getItemList().get(position).getId() == getMaxIndex()) {
                holder.textViewDepth.setVisibility(View.VISIBLE);
                holder.textViewDepth.setText("DEEPEST: " + getItemList().get(position).getDepth() + " KM");
            } else {
                holder.textViewDepth.setVisibility(View.GONE);
            }

            if (getItemList().get(position).getId() == getMaxLatId()) {
                holder.textViewLatLong.setVisibility(View.VISIBLE);
                holder.textViewLatLong.setText("MOST NORTHERLY");
            } else if (getItemList().get(position).getId() == getMinLatID()) {
                holder.textViewLatLong.setVisibility(View.VISIBLE);
                holder.textViewLatLong.setText("MOST SOUTHERLY");
            } else if (getItemList().get(position).getId() == getMaxLongId()) {
                holder.textViewLatLong.setVisibility(View.VISIBLE);
                holder.textViewLatLong.setText("MOST EASTERLY");
            } else if (getItemList().get(position).getId() == getMinLongId()) {
                holder.textViewLatLong.setVisibility(View.VISIBLE);
                holder.textViewLatLong.setText("MOST WESTERLY");
            } else {
                holder.textViewLatLong.setVisibility(View.GONE);
            }
        } else {
            holder.textViewDepth.setVisibility(View.GONE);
            holder.textViewLatLong.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(view -> showDialog(getItemList().get(position)));
    }

    /*
    It returns the total number of the list size. The list values are passed by the constructor.
    */
    @Override
    public int getItemCount() {
        return getItemList().size();
    }

    /*
        When a user clicks on a card, a modal pops up containing the map with the marker set on the location
        where the earthquake took place. The values for the earthquake selected are fetched and displayed to the
        user. Using the latitude and longitude data, the market pinned to the location and zoom is enabled.
        A user can click on the market to see the location and the earthquake's magnitude.
    */
    public void showDialog(Item item) {
        TextView tvDate, tvLocation, tvMagnitude, tvDepth, tvLatLong;
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_detail);

//        Initialize the required widgets
        tvDate = dialog.findViewById(R.id.textViewSpecificDate);
        tvLocation = dialog.findViewById(R.id.tv_location);
        tvMagnitude = dialog.findViewById(R.id.textViewMagnitude);
        tvDepth = dialog.findViewById(R.id.tv_depth);
        tvLatLong = dialog.findViewById(R.id.tv_lat_long);
        ImageView imgClose = dialog.findViewById(R.id.img_close);

//        Set the item value on textViews
        tvDate.setText("Date: " + item.getPubDate());
        tvLocation.setText(item.getLocation());
        tvMagnitude.setText("Magnitude: " + item.getMagnitude());
        tvDepth.setText("Depth: " + item.getDepth() + " km");
        tvLatLong.setText("Lat/long: " + item.getLatitude() + "," + item.getLongitude());

        MapView mMapView = (MapView) dialog.findViewById(R.id.mapView);
        MapsInitializer.initialize(getContext());

        mMapView.onCreate(dialog.onSaveInstanceState());
        mMapView.onResume();

        mMapView.getMapAsync(googleMap -> {
//        Create LatLng object
            LatLng latLng = new LatLng(item.getLatitude(), item.getLongitude());

            String mag = Double.toString(item.getMagnitude());
//        Add Marker
            googleMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .title(item.getLocation())
                            .snippet("Magnitude: " + mag)
                            .icon(getMarkerIcon(getPinColor(item.getMagnitude()))));
//        Animate Camera
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16F));
//        Set ZoomControls to Enabled
            googleMap.getUiSettings().setZoomControlsEnabled(true);
        });


        dialog.show();

        imgClose.setOnClickListener(view -> dialog.dismiss());


        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    /*
        Based on the magnitude of the earthquakes, the color of the market is set, similar to the cards.

        NOTE: The values used here are simply for demonstration purposes (Show colors), otherwise the appropriate
        and factual ranges should be used.
    */
    public int getPinColor(double magnitude) {
        if (magnitude >= 3.1) {
            return ContextCompat.getColor(getContext(), R.color.great);
        } else if (magnitude <= 3.0 && magnitude >= 2.1) {
            return ContextCompat.getColor(getContext(), R.color.strong);
        } else if (magnitude <= 2.0 && magnitude >= 1.0) {
            return ContextCompat.getColor(getContext(), R.color.moderate);
        } else {
            return ContextCompat.getColor(getContext(), R.color.light);
        }
    }

    /*
        Add the market icon to the map here.
    */
    public BitmapDescriptor getMarkerIcon(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    protected String getTAG() {
        return TAG;
    }

    protected void setTAG(String TAG) {
        this.TAG = TAG;
    }

    protected Context getContext() {
        return context;
    }

    protected void setContext(Context context) {
        this.context = context;
    }

    protected List<Item> getItemList() {
        return itemList;
    }

    protected void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    protected int getMaxIndex() {
        return maxIndex;
    }

    protected void setMaxIndex(int maxIndex) {
        this.maxIndex = maxIndex;
    }

    protected int getMinIndex() {
        return minIndex;
    }

    protected void setMinIndex(int minIndex) {
        this.minIndex = minIndex;
    }

    protected int getMaxLatId() {
        return maxLatId;
    }

    protected void setMaxLatId(int maxLatId) {
        this.maxLatId = maxLatId;
    }

    protected int getMinLatID() {
        return minLatID;
    }

    protected void setMinLatID(int minLatID) {
        this.minLatID = minLatID;
    }

    protected int getMaxLongId() {
        return maxLongId;
    }

    protected void setMaxLongId(int maxLongId) {
        this.maxLongId = maxLongId;
    }

    protected int getMinLongId() {
        return minLongId;
    }

    protected void setMinLongId(int minLongId) {
        this.minLongId = minLongId;
    }

    protected boolean isFiltered() {
        return isFiltered;
    }

    protected void setFiltered(boolean filtered) {
        isFiltered = filtered;
    }

    public class Holder extends RecyclerView.ViewHolder {
        CardView cardView, cardTag;
        TextView textViewMagnitude, textViewDepth, textViewLatLong, textViewArea, textViewTag;
//        View bar;

        public Holder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            textViewMagnitude = itemView.findViewById(R.id.textViewMagnitude);
            textViewDepth = itemView.findViewById(R.id.textViewDepth);
            textViewLatLong = itemView.findViewById(R.id.textViewLatLong);
            textViewArea = itemView.findViewById(R.id.textViewArea);
            textViewTag = itemView.findViewById((R.id.textViewTag));
            cardTag = itemView.findViewById(R.id.cardTag);
        }
    }

}
