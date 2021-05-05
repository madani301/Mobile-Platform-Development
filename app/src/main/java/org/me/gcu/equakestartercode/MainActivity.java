package org.me.gcu.equakestartercode;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Madani Napaul | S1903342
 */

public class MainActivity extends AppCompatActivity {
    public static List<Item> mainItemList = new ArrayList<>();
    Button startButton;
    RecyclerView recyclerView;
    FilterAdapter filterAdapter;
    ProgressDialog progressDialog;
    String text;
    String urlSource = "http://quakes.bgs.ac.uk/feeds/MhSeismology.xml";
    boolean isFiltered = false;
    boolean isAscending = false;
    int maxIndexId, minIndexId, maxLatId, minLatId, maxLongId, minLongId;

    /*
    The state of the application can be saved in a bundled, which is typically non-persistent, and
    dynamic data in onSaveInstanceState. It can then be passed back to onCreate if the activity has
    to be recreated, for example when changing orientation. When data is not supplied, savedInstanceState
    is null.

    The content view is set to its respective XML layout view; activity_main.xml. The appropriate widgets
    are initialized; the asynchronous task is called when the button is clicked, which is responsible for
    fetching the data from the source; BGS.
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Initialize the required widgets; startButton & the recyclerView
        startButton = findViewById(R.id.startButton);
        recyclerView = findViewById(R.id.recycler_view);

        requestForURL();

        startButton.setOnClickListener(view -> {
            isFiltered = false;
            mainItemList = new ArrayList<>();
            new AsyncTaskExample(false).execute(urlSource);
        });

//        Check if there is already supplied data; when changing orientation for instance
        if (savedInstanceState != null) {
            mainItemList.clear();
            mainItemList.addAll(savedInstanceState.getParcelableArrayList("key"));

//            Now we set the itemAdapter on the recyclerView
            setFilterAdapter(mainItemList);
        }
    }

    /*
    This method is typically called before or after onStop() is called. It is called to retrieve per-instance
    state from an activity before being killed so that the state can be restored in onCreate(Bundle) or
    onRestoreInstanceState(Bundle).

    In this case, the list can be restored onRestoreInstanceState(Bundle).
    */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("key", new ArrayList<Item>(mainItemList));

    }


    /*
    As the system starts to stop an activity, the onSaveInstanceState() is called, allowing us to specific additional
    state data to save in case the activity should be recreated. In this case, custom objects (Items) are saved in a
    bundle when the parcelable interface is implemented. The bundle passes the state data via the parcelable interface
    to onRestoreInstanceState() method.
    */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mainItemList.clear();
        mainItemList.addAll(savedInstanceState.getParcelableArrayList("key"));
    }

    /*
    It is used to specify the options menu for the activity, where the menu resource is inflated into the Menu
    provided in the callback. The items are then added to the action if ever they are present.
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    /*
        After inflating the menu with the items, we handle the action to be performed when they are selected.
        In this case, if the user clicks on the filter, the filter dialog is displayed, or if the user selects
        the sort option, the items are sorted in both descending and ascending order.
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_filter) {
            Log.d("theF", "onOptionsItemSelected: " + mainItemList.size());
            showDialog();
            return true;
        } else if (item.getItemId() == R.id.item_swap) {
            isAscending = !isAscending;
            Collections.sort(mainItemList, (obj1, obj2) -> {
                if (isAscending) {
                    // # Ascending order
                    return Double.compare(obj1.getMagnitude(), obj2.getMagnitude());
                } else {
                    // # Descending order
                    return Double.compare(obj2.getMagnitude(), obj1.getMagnitude());
                }
            });
            filterAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    In this state, the activity comes to the foreground where the system invokes the onResume() callback. This is
    the state in which the app interacts with the user, and it will stay in this state until for instance the device's
    screen turns off.
    */
    @Override
    protected void onResume() {
        if (filterAdapter == null) {
            setFilterAdapter(mainItemList);
        }
        super.onResume();

    }

    /*
        It starts a timer when the user fetches data from the source, and once the timer expires, it auto-fetches
        data again from the source. It allows auto-updating the data wherever the user is in the app.
    */
    public void requestForURL() {
        final Handler handler = new Handler();
        final int delay = 60000; // 1000 milliseconds == 1 second

        handler.postDelayed(new Runnable() {
            public void run() {
                isFiltered = false;
                mainItemList = new ArrayList<>();
                new AsyncTaskExample(false).execute(urlSource);
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    /*
        If data has been supplied, the items is sorted in descending order, where the earthquake with the
        highest magnitude will be at the top. Then the FilterAdapter is set on the recyclerView.
    */
    public void setFilterAdapter(List<Item> itemList) {
        Log.d("theF", "setFilterAdapter: " + itemList.size());
        if (itemList.size() > 0) {
            arrangeList(itemList);
            filterAdapter = new FilterAdapter(isFiltered, MainActivity.this, itemList, maxIndexId, minIndexId, maxLatId, minLatId, maxLongId, minLongId);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(filterAdapter);
        }
    }

    /*
        The widgets for the filter function are initialized here, the checkboxes for choosing a filter for a specific
        date or a date range. A data range is displayed only when date range is selected, it takes care of this.
        When the user selects the specific date or date range fields, a date picker is rendered; basically calling the
        function which displays the date picker. Based on the date(s), the data is retrieved, or if the user omitted a
        required field, then an error message is displayed to prompt the user to fill the required field.
    */
    public void showDialog() {
        CheckBox cbClear;
        TextView tvSpecificDate, tvRangeDate1, tvRangeDate2;
        Button btnApply;
        RadioButton rbSpecific, rbRange;

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_filter);
        ImageView imgClose = dialog.findViewById(R.id.img_close);

        dialog.show();

        imgClose.setOnClickListener(view -> dialog.dismiss());

        //        Initialize the required widgets; checkbox, radio buttons, text fields, and the apply button.
        cbClear = dialog.findViewById(R.id.checkboxClear);
        rbSpecific = dialog.findViewById(R.id.radioSpecific);
        rbRange = dialog.findViewById(R.id.radioRange);
        tvSpecificDate = dialog.findViewById(R.id.textViewSpecificDate);
        tvRangeDate1 = dialog.findViewById(R.id.textViewStartDate);
        tvRangeDate2 = dialog.findViewById(R.id.textViewEndDate);
        btnApply = dialog.findViewById(R.id.filterApplyButton);

        //        ClickListeners on radio buttons
        rbSpecific.setOnClickListener(view -> {
            tvSpecificDate.setVisibility(View.VISIBLE);
            tvRangeDate1.setVisibility(View.GONE);
            tvRangeDate2.setVisibility(View.GONE);
            rbRange.setChecked(false);
        });

        rbRange.setOnClickListener(view -> {
            tvSpecificDate.setVisibility(View.GONE);
            tvRangeDate1.setVisibility(View.VISIBLE);
            tvRangeDate2.setVisibility(View.VISIBLE);
            rbSpecific.setChecked(false);
        });

        //        ClickListeners on textViews for getting date
        tvSpecificDate.setOnClickListener(view -> datePickerDialog(tvSpecificDate));
        tvRangeDate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog(tvRangeDate1);
            }
        });
        tvRangeDate2.setOnClickListener(view -> datePickerDialog(tvRangeDate2));

        btnApply.setOnClickListener(view -> {
            if (cbClear.isChecked()) {
                isFiltered = false;
                Log.d("theF", "onClick: mainItemList: " + mainItemList.size());
                List<Item> itemList = new ArrayList<>();
                itemList.addAll(mainItemList);
                Log.d("theF", "onClick: " + itemList.size());
                setFilterAdapter(itemList);
            } else {
                isFiltered = true;
                if (rbSpecific.isChecked()) {
                    if (TextUtils.isEmpty(tvSpecificDate.getText().toString())) {
                        Toast.makeText(MainActivity.this, "Select a specific date", Toast.LENGTH_SHORT).show();
                    } else {
                        //                        get specific date data
                        getSingleDateData(tvSpecificDate);
                    }
                } else if (rbRange.isChecked()) {
                    if (TextUtils.isEmpty(tvRangeDate1.getText().toString())) {
                        Toast.makeText(MainActivity.this, "Select a start date", Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(tvRangeDate2.getText().toString())) {
                        Toast.makeText(MainActivity.this, "Select an end date", Toast.LENGTH_SHORT).show();
                    } else {
                        //                        get range data
                        getRangeDateData(tvRangeDate1, tvRangeDate2);
                    }
                }
            }
            dialog.dismiss();
        });

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    /*
     The date picker dialog is created here, which is a monthly calendar allowing the user to pick a date.
     By default, today's date is always selected when the user open the date picker dialog.
    */
    public void datePickerDialog(TextView textView) {
        DatePickerDialog mDatePicker;
        final Calendar mCalendar = Calendar.getInstance();

        mDatePicker = new DatePickerDialog(MainActivity.this, (datePicker, year, monthOfYear, dayOfMonth) -> {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, monthOfYear);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String formattedDate = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(mCalendar.getTime());
            textView.setText(formattedDate);
        }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
        mDatePicker.show();
    }

    /*
    Check if the dates are equal or not
    */
    public boolean isDateEquals(String eqDateStr, String newDateStr) {
        boolean result = false;
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy");
        try {
            Date eqDate = sdf.parse(eqDateStr);
            Date newDate = sdf.parse(newDateStr);
            result = newDate.equals(eqDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
    For the date range, check if the dates exist and are present in between the specified range.
    */
    public boolean isDateEqualToRange(String eqDateStr, String startDateStr, String endDateStr) {
        boolean result = false;
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy");
        try {
            Date eqDate = sdf.parse(eqDateStr);
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);
            result = (eqDate.equals(startDate) || eqDate.after(startDate)) && (eqDate.equals(endDate) || eqDate.before(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    /*
        When the user enters a specific date, this method retrieves the date entered by the user. If there was an
        earthquake recorded on that day, it will be displayed, else a message dialog will be displayed to inform
        the user that there were no earthquakes on that specific date.
    */
    public void getSingleDateData(TextView tvSpecificDate) {
        List<Item> itemList = new ArrayList<>();
        String eqDateStr = tvSpecificDate.getText().toString();

        Log.d("theF", "getSingleDateData: mainItemList: " + mainItemList.size());
        for (Item item : mainItemList) {
            if (isDateEquals(item.getPubDate(), eqDateStr)) {
                itemList.add(item);
            }
        }
        Log.d("theF", "getSingleDateData: itemList: " + itemList.size());
        if (itemList.size() > 0) {

            //            arrange List according to magnitude
            arrangeList(itemList);
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("No earthquakes found on " + eqDateStr);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    (dialog, which) -> dialog.dismiss());
            alertDialog.show();
            itemList.clear();
        }

        setFilterAdapter(itemList);

    }

    /*
        The start date and end date for date range is retrieved here, and all earthquakes which took place
        between those two dates are added to the list. The list is then sorted by magnitude, and if there were
        no earthquakes during that period, then a message dialog is displayed to the user.
    */
    public void getRangeDateData(TextView tvRangeDate1, TextView tvRangeDate2) {
        Log.d("theF", "getRangeDateData: mainItemList: " + mainItemList.size());
        List<Item> itemList = new ArrayList<>();
        String startDateStr = tvRangeDate1.getText().toString();
        String endDateStr = tvRangeDate2.getText().toString();

        for (Item item : mainItemList) {
            if (isDateEqualToRange(item.getPubDate(), startDateStr, endDateStr)) {
                itemList.add(item);
            }
        }
        Log.d("theF", "getRangeDateData: itemList: " + itemList.size());
        if (itemList.size() > 0) {
            //      arrange List according to magnitude
            arrangeList(itemList);
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("No earthquakes found between " + startDateStr + " , " + endDateStr);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    (dialog, which) -> dialog.dismiss());
            alertDialog.show();
            itemList.clear();
        }

        setFilterAdapter(itemList);

    }

    /*
    This method sorts the list in either ascending order or descending order according to the magnitude.
    */
    public void arrangeList(List<Item> itemList) {
        Log.d("theF", "arrangeList: itemList: " + itemList.size() + " isFiltered: " + isFiltered);
        Collections.sort(itemList, (obj1, obj2) -> {
            if (isAscending) {
                // # Ascending order
                return Double.compare(obj1.getMagnitude(), obj2.getMagnitude());
            } else {
                // # Descending order
                return Double.compare(obj2.getMagnitude(), obj1.getMagnitude());
            }
        });

        getMaxIndex();

        getMinIndex();

        getLatitudeId();

        getLongitudeId();

        Log.d("theI", "arrangeList: maxIndexId: " + maxIndexId + " minIndexId: " + minIndexId);
        Log.d("theI", "arrangeList: maxLatId: " + maxLatId + " minLatID: " + minLatId +
                " maxLongId: " + maxLongId + " minLongId: " + minLongId);
        if (isFiltered) {
            List<Item> itemList1 = new ArrayList<>();

            for (int i = 0; i < itemList.size(); i++) {
                int id = itemList.get(i).getId();
                if (id == maxIndexId || id == minIndexId ||
                        id == maxLatId || id == minLatId || id == maxLongId || id == minLongId) {
                    itemList1.add(itemList.get(i));
                    Log.d("theI", "arrangeList: i: " + i + " id: " + id);
                }
            }
            itemList.clear();
            itemList.addAll(itemList1);
        }
    }

    /*
    This method retrieves the earthquake with the higher index value, which is the maximum depth.
    */
    public void getMaxIndex() {
        double highest = mainItemList.get(0).getDepth();
        maxIndexId = mainItemList.get(0).getId();
        for (int s = 1; s < mainItemList.size(); s++) {
            double curValue = mainItemList.get(s).getDepth();
            if (curValue > highest) {
                highest = curValue;
                maxIndexId = mainItemList.get(s).getId();
            }
        }
    }

    /*
    This method retrieves the earthquake with the lowest index value, which is the lowest depth.
    */
    public void getMinIndex() {
        double lowest = mainItemList.get(0).getDepth();
        minIndexId = mainItemList.get(0).getId();
        for (int s = 1; s < mainItemList.size(); s++) {
            double curValue = mainItemList.get(s).getDepth();
            if (curValue < lowest) {
                lowest = curValue;
                minIndexId = mainItemList.get(s).getId();
            }
        }
    }

    /*
    This method retrieves both the highest and lowest latitude value for each earthquake in the list.
    */
    public void getLatitudeId() {
        double highest = mainItemList.get(0).getLatitude();
        double lowest = mainItemList.get(0).getLatitude();
        maxLatId = mainItemList.get(0).getId();
        minLatId = mainItemList.get(0).getId();

        for (int s = 1; s < mainItemList.size(); s++) {
            double curValue = mainItemList.get(s).getLatitude();
            if (curValue > highest) {
                highest = curValue;
                maxLatId = mainItemList.get(s).getId();
            }
            if (curValue < lowest) {
                lowest = curValue;
                minLatId = mainItemList.get(s).getId();
            }
        }
    }

    /*
    This method retrieves both the highest and lowest longitude value for each earthquake in the list.
    */
    public void getLongitudeId() {

        double highest = mainItemList.get(0).getLongitude();
        double lowest = mainItemList.get(0).getLongitude();
        maxLongId = mainItemList.get(0).getId();
        minLongId = mainItemList.get(0).getId();

        for (int s = 1; s < mainItemList.size(); s++) {
            double curValue = mainItemList.get(s).getLongitude();
            if (curValue > highest) {
                highest = curValue;
                maxLongId = mainItemList.get(s).getId();
            }
            if (curValue < lowest) {
                lowest = curValue;
                minLongId = mainItemList.get(s).getId();
            }
        }
    }

    /*
        An asynchronous class is creating which is responsible for fetching the data from the source.
        When the user clicks on the startButton, a dialog pops up to inform the user that the data is loading.
        While this is running, a second thread (doInBackground) fetches the data from the sources and processes it,
        then once the execution is over, the itemAdapter is set on the recyclerView which then displays all data retrieved.
    */
    private class AsyncTaskExample extends AsyncTask<String, String, List<Item>> {
        boolean openMap;

        public AsyncTaskExample(boolean openMap) {
            this.openMap = openMap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Fetching Data from BGS");
            progressDialog.setMessage("Loading...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected List<Item> doInBackground(String... strings) {
            int i = 0;
            Item item = null;
            URL url;
            URLConnection urlConnection;
            BufferedReader in = null;

            try {
                Log.e("MyTag", "in try");
                url = new URL(strings[0]);
                urlConnection = url.openConnection();
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                Log.e("MyTag", "after ready");

                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(urlConnection.getInputStream(), null);
                    int eventType = parser.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        String tagName = parser.getName();
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                if (tagName.equalsIgnoreCase("item")) {
                                    item = new Item();
                                }
                                break;

                            case XmlPullParser.TEXT:
                                text = parser.getText();
                                break;

                            case XmlPullParser.END_TAG:
                                if (item != null) {
                                    if (tagName.equalsIgnoreCase("title")) {
                                        item.setTitle(text);
                                    } else if (tagName.equalsIgnoreCase("description")) {
                                        String[] stringSet = text.split(";");
                                        String location = stringSet[1].split(":")[1].trim();
                                        String[] latLong = stringSet[2].split(":")[1].trim().split(",");
                                        String depth = stringSet[3].replaceAll("[^\\d.]", "").replaceAll(":", "");
                                        String magnitude = stringSet[4].replaceAll("[^\\d.]", "").replaceAll(":", "");
                                        item.setDescription(text);
                                        item.setLocation(location);
                                        item.setDepth(Double.parseDouble(depth));
                                        item.setMagnitude(Double.parseDouble(magnitude));
                                        item.setLatitude(Double.parseDouble(latLong[0]));
                                        item.setLongitude(Double.parseDouble(latLong[1]));
                                    } else if (tagName.equalsIgnoreCase("link")) {
                                        item.setLink(text);
                                    } else if (tagName.equalsIgnoreCase("pubDate")) {
                                        item.setPubDate(text);
                                    } else if (tagName.equalsIgnoreCase("category")) {
                                        item.setCategory(text);
                                    } else if (tagName.equalsIgnoreCase("item")) {
                                        i++;
                                        item.setId(i);
                                        Log.d("theS", "doInBackground: " + item.toString());
                                        mainItemList.add(item);
                                    }
                                }

                                break;

                            default:
                                break;
                        }
                        eventType = parser.next();
                    }

                    Collections.sort(mainItemList, (obj1, obj2) -> {
                        // # Descending order
                        return Double.compare(obj2.getMagnitude(), obj1.getMagnitude());
                    });
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
                in.close();
            } catch (IOException ae) {
                Log.e("MyTag", "ioexception in run");
            }
            return mainItemList;
        }

        @Override
        protected void onPostExecute(List<Item> itemList) {
            super.onPostExecute(itemList);
            if (itemList != null) {
                progressDialog.hide();

                setFilterAdapter(itemList);
            } else {
                progressDialog.show();
            }
        }
    }
}