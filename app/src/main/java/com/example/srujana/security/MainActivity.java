package com.example.srujana.security;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    // Menu
    public static final int IDM_SETTINGS = 101;


    // Activities
    private static final int ACT_RESULT_CHOOSE_CONTACT = 1001;
    private static final int ACT_RESULT_SETTINGS = 1002;
    private static final int ACT_RESULT_REPO = 1004;

    // Dialogs
    private static final int SEND_SMS_DIALOG_ID = 0;
    private final static int SAVE_POINT_DIALOG_ID = 1;

    ProgressDialog mSMSProgressDialog;

    // My GPS states
    public static final int GPS_PROVIDER_DISABLED = 1;
    public static final int GPS_GETTING_COORDINATES = 2;
    public static final int GPS_GOT_COORDINATES = 3;
    //public static final int GPS_PROVIDER_UNAVIALABLE = 4;
    //public static final int GPS_PROVIDER_OUT_OF_SERVICE = 5;
    public static final int GPS_PAUSE_SCANNING = 6;

    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1001;

    // Location manager
    private LocationManager manager;

    // SMS thread
    ThreadSendSMS mThreadSendSMS;

    // Views
    ImageButton chooseContactBtn;
    ImageButton sendpbtn;
    ImageButton btnShare;
    ImageButton btnMap;
    ImageButton btnSave;

    EditText plainPh;
    Button enableGPSBtn;
    TextView GPSstate;
    Menu mMenu;

    // Globals
    private String coordsToSend;
    private String gAccuracy;
    private String phoneToSendSMS;
    ColorStateList gGpsStateColorDefault;

    // Database
    DBHelper dbHelper;

    // SMS send thread. Result handling
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String res_send = msg.getData().getString("res_send");
            dismissDialog(SEND_SMS_DIALOG_ID);
            DBHelper.ShowToastT(MainActivity.this, res_send,
                    Toast.LENGTH_SHORT);
        }
    };

    // Location events (we use GPS only)
    private LocationListener locListener = new LocationListener() {

        public void onLocationChanged(Location argLocation) {
            printLocation(argLocation, GPS_GOT_COORDINATES);
        }

        @Override
        public void onProviderDisabled(String arg0) {
            printLocation(null, GPS_PROVIDER_DISABLED);
        }

        @Override
        public void onProviderEnabled(String arg0) {
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        }

    };

    private void printLocation(Location loc, int state) {

        switch (state) {
            case GPS_PROVIDER_DISABLED:
                GPSstate.setText(R.string.gps_state_disabled);
                setGPSStateAccentColor();
                enableGPSBtn.setVisibility(View.VISIBLE);
                break;
            case GPS_GETTING_COORDINATES:
                GPSstate.setText(R.string.gps_state_in_progress);
                setGPSStateNormalColor();
                enableGPSBtn.setVisibility(View.VISIBLE);
                break;
            case GPS_PAUSE_SCANNING:
                GPSstate.setText("");
                enableGPSBtn.setVisibility(View.VISIBLE);
                break;
            case GPS_GOT_COORDINATES:
                if (loc != null) {

                    // Accuracy
                    if (loc.getAccuracy() < 0.0001) {
                        gAccuracy = "?";
                    } else if (loc.getAccuracy() > 99) {
                        gAccuracy = "> 99";
                    } else {
                        gAccuracy = String.format(Locale.US, "%2.0f",
                                loc.getAccuracy());
                    }

                    String separ = System.getProperty("line.separator");

                    String la = String
                            .format(Locale.US, "%2.7f", loc.getLatitude());
                    String lo = String.format(Locale.US, "%3.7f",
                            loc.getLongitude());

                    coordsToSend = la + "," + lo;

                    GPSstate.setText(getString(R.string.info_print1) + ": "
                            + gAccuracy + " " + getString(R.string.info_print2)
                            + separ + getString(R.string.info_latitude) + ": " + la
                            + separ + getString(R.string.info_longitude) + ": " + lo);

                    setGPSStateNormalColor();

                    btnShare.setVisibility(View.VISIBLE);

                    btnMap.setVisibility(View.VISIBLE);
                    btnSave.setVisibility(View.VISIBLE);


                    ShowSendButton();
                    enableGPSBtn.setVisibility(View.VISIBLE);

                } else {
                    GPSstate.setText(R.string.gps_state_unavialable);
                    setGPSStateAccentColor();
                    enableGPSBtn.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        mMenu = menu;

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);


        menu.add(Menu.NONE, IDM_SETTINGS, Menu.NONE,
                R.string.menu_item_settings);



        return (super.onCreateOptionsMenu(menu));
    }

    // Dialogs
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {

            case SEND_SMS_DIALOG_ID:
                mSMSProgressDialog = new ProgressDialog(MainActivity.this);
                // mCatProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mSMSProgressDialog.setCanceledOnTouchOutside(false);
                mSMSProgressDialog.setCancelable(false);
                mSMSProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mSMSProgressDialog.setMessage(getString(R.string.info_please_wait)
                        + " " + phoneToSendSMS);
                return mSMSProgressDialog;

            case SAVE_POINT_DIALOG_ID:
                LayoutInflater inflater_sp = getLayoutInflater();
                View layout_sp = inflater_sp.inflate(R.layout.repo_save_point_dialog,
                        (ViewGroup) findViewById(R.id.repo_save_point_dialog_layout));

                AlertDialog.Builder builder_sp = new AlertDialog.Builder(this);
                builder_sp.setView(layout_sp);

                final EditText lPointName = (EditText) layout_sp
                        .findViewById(R.id.point_edit_text);

                builder_sp.setPositiveButton(getString(R.string.save_btn_txt),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dbHelper = new DBHelper(MainActivity.this);
                                dbHelper.insertMyCoord(lPointName.getText()
                                        .toString(), coordsToSend);
                                dbHelper.close();
                                lPointName.setText(""); // Чистим
                                DBHelper.ShowToast(MainActivity.this,
                                        R.string.point_saved, Toast.LENGTH_LONG);
                            }
                        });

                builder_sp.setNegativeButton(getString(R.string.cancel_btn_txt),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                lPointName.setText(""); // Чистим
                                dialog.cancel();
                            }
                        });

                builder_sp.setCancelable(true);
                AlertDialog dialog = builder_sp.create();
                dialog.setTitle(getString(R.string.save_point_dlg_header));
                // show keyboard automatically
                dialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                return dialog;

        }
        return null;
    }

    // Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case IDM_SETTINGS:
                Intent sett_intent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    sett_intent = new Intent(this, PreferencesActivity.class);
                } else {
                    sett_intent = new Intent(this, PreferencesLegacyActivity.class);
                }
                startActivityForResult(sett_intent, ACT_RESULT_SETTINGS);
                break;
            case R.id.action_sms_regexp:
                Intent repo_intent = new Intent(this, SlideTabsActivity.class);
                startActivityForResult(repo_intent, ACT_RESULT_REPO);
                break;




            default:
                return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED
                        && grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                    this.startLocation();
                } else {
                    // permission denied
                    DBHelper.ShowToast(MainActivity.this, R.string.text_insufficient_privileges, Toast.LENGTH_LONG);
                    finish();
                }
                return;
            }
        }
    }

    private void stopLocation() {
        try {
            manager.removeUpdates(locListener);
        } catch (SecurityException e) {
        }
    }

    private void startLocation() {
        // Возобновляем работу с GPS-приемником
        try {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
            btnShare.setVisibility(View.VISIBLE);

            btnMap.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);


            HideSendButton();

            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                printLocation(null, GPS_GETTING_COORDINATES);
            }
        } catch (SecurityException e) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Держать ли экран включенным?
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPrefs.getBoolean("prefKeepScreen", true)) {
            getWindow()
                    .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // For Android 6.0 ask for permissions in runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (GpsHelper.canAccessLocation(MainActivity.this))
                this.startLocation();
        } else
            this.startLocation();

    }

    @Override
    protected void onPause() {
        super.onPause();

        // For Android 6.0 ask for permissions in runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (GpsHelper.canAccessLocation(MainActivity.this))
                this.stopLocation();
        } else
            this.stopLocation();
    }

    public void showSelectedNumber(String number) {
        plainPh.setText(number);
        plainPh.setSelection(plainPh.getText().length());
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (ACT_RESULT_CHOOSE_CONTACT):
                String number;
                String name;
                // int type = 0;
                if (data != null) {
                    Uri uri = data.getData();

                    if (uri != null) {
                        Cursor c = null;
                        try {

                            c = getContentResolver()
                                    .query(uri,
                                            new String[]{
                                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                                    ContactsContract.CommonDataKinds.Phone.TYPE,
                                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                                            null, null, null);

                            if (c != null && c.moveToFirst()) {
                                number = c.getString(0);
                                number = number.replace("-", "").replace(" ", "")
                                        .replace("(", "").replace(")", "").replace(".", "");
                                // type = c.getInt(1);
                                name = c.getString(2);
                                showSelectedNumber(number);

                                // update saved number
                                dbHelper = new DBHelper(MainActivity.this);
                                dbHelper.setSlot(0, name, number);
                                dbHelper.close();

                            }
                        } finally {
                            if (c != null) {
                                c.close();
                            }
                        }
                    }
                }

                break;

        }
    }

    public void chooseContact(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, ACT_RESULT_CHOOSE_CONTACT);
        //IncomingSms.sendNotification(MainActivity.this, "56.5555555,56.7777777");
    }

    public void showGpsSystemDialog(View v) {
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }



    public void openOnMap(View v) {
        GpsHelper.openOnMap(getApplicationContext(), coordsToSend);
    }

    public void saveCoordinates(View v) {
        showDialog(SAVE_POINT_DIALOG_ID);
    }



    // ------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Setting up app language. This code MUST BE placed BEFORE setContentView!

        // EOF Setting up app language

        setContentView(R.layout.activity_main);

        // Plain phone number
        plainPh = (EditText) findViewById(R.id.editText1);
        plainPh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String lPhone = ((EditText) v).getText().toString().replace("-", "")
                        .replace(" ", "").replace("(", "").replace(")", "").replace(".", "");

                if (lPhone.length() >= 5) {

                    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(lPhone));
                    String[] projection = new String[]{"display_name"};
                    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

                    if (cursor.moveToFirst()) {
                        DBHelper.ShowToastT(MainActivity.this, cursor.getString(0),
                                Toast.LENGTH_SHORT);
                    }
                    cursor.close();
                }
            }
        });

        // Select contact
        chooseContactBtn = (ImageButton) findViewById(R.id.choose_contact);

        // Stored phone number -> to EditText
        dbHelper = new DBHelper(this);
        showSelectedNumber(dbHelper.getSlot(0, "phone"));
        dbHelper.close();

        // GPS init
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Enable GPS button
        enableGPSBtn = (Button) findViewById(R.id.button3);
        enableGPSBtn.setVisibility(View.VISIBLE);

        // Share buttons
        btnShare = (ImageButton) findViewById(R.id.btnShare);

        btnMap = (ImageButton) findViewById(R.id.btnMap);
        btnSave = (ImageButton) findViewById(R.id.btnSave);

        btnShare.setVisibility(View.VISIBLE);

        btnMap.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);




        btnShare.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GpsHelper.shareCoordinates(MainActivity.this, GpsHelper.getShareBody(MainActivity.this, coordsToSend, gAccuracy));
            }
        });

        // Send buttons
        sendpbtn = (ImageButton) findViewById(R.id.send_plain);
        HideSendButton();

        // GPS-state TextView init
        GPSstate = (TextView) findViewById(R.id.textView1);
        gGpsStateColorDefault = GPSstate.getTextColors();
        setGPSStateNormalColor();


        // For Android 6.0 ask for All permissions in runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.SEND_SMS,
                    android.Manifest.permission.RECEIVE_SMS,
                    android.Manifest.permission.READ_SMS,
                    android.Manifest.permission.READ_PHONE_STATE,
                    android.Manifest.permission.READ_CONTACTS
            }, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }

    private void setGPSStateNormalColor() {
        GPSstate.setTextColor(gGpsStateColorDefault);
    }

    private void setGPSStateAccentColor() {
        GPSstate.setTextColor(DBHelper.determineAccendcolor(MainActivity.this));
    }

    protected String handleSMSText(String lMsg) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String tBefore = sharedPrefs.getString("prefTextBefore", "");
        String tAfter = sharedPrefs.getString("prefTextAfter", "");

        if (!tBefore.equalsIgnoreCase("")) {
            lMsg = tBefore + " " + lMsg;
        }

        if (!tAfter.equalsIgnoreCase("")) {
            lMsg = lMsg +" "+ tAfter;
        }

        return lMsg;
    }

    // ------------------------------------------------------------------------------------------

    public void sendSMS_Debug(String lMsg) {

        phoneToSendSMS = plainPh.getText().toString().replace("-", "")
                .replace(" ", "").replace("(", "").replace(")", "").replace(".", "");

        if (phoneToSendSMS.equalsIgnoreCase("")) {
            DBHelper.ShowToast(MainActivity.this,
                    R.string.error_no_phone_number, Toast.LENGTH_LONG);
        } else {
            try {


                // update saved number
                dbHelper = new DBHelper(MainActivity.this);
                dbHelper.setSlot(0, "", phoneToSendSMS);
                dbHelper.close();

                SmsManager smsManager = SmsManager.getDefault();
                ArrayList<String> parts = smsManager.divideMessage(lMsg);
                smsManager.sendMultipartTextMessage(phoneToSendSMS, null, parts, null, null);

                DBHelper.ShowToastT(MainActivity.this, getString(R.string.info_sms_sent),
                        Toast.LENGTH_SHORT);

            } catch (Exception e) {
                DBHelper.ShowToastT(MainActivity.this, e.getMessage(),
                        Toast.LENGTH_LONG);
            }

        }

    }

    protected void sendSMS(String lMsg) {

        phoneToSendSMS = plainPh.getText().toString().replace("-", "")
                .replace(" ", "").replace("(", "").replace(")", "").replace(".", "");

        if (phoneToSendSMS.equalsIgnoreCase("")) {
            DBHelper.ShowToast(MainActivity.this,
                    R.string.error_no_phone_number, Toast.LENGTH_LONG);
        } else {

            // update saved number
            dbHelper = new DBHelper(MainActivity.this);
            dbHelper.setSlot(0, "", phoneToSendSMS);
            dbHelper.close();

            showDialog(SEND_SMS_DIALOG_ID);

            // Запускаем новый поток для отправки SMS
            mThreadSendSMS = new ThreadSendSMS(handler, getApplicationContext());
            mThreadSendSMS.setMsg(handleSMSText(lMsg));
            mThreadSendSMS.setPhone(phoneToSendSMS);
            mThreadSendSMS.setState(ThreadSendSMS.STATE_RUNNING);
            mThreadSendSMS.start();
        }

    }

    public void initiateSMSSend(View v) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sendSMS(GpsHelper.getLinkByProvType(sharedPrefs.getString("prefSMSContent", "2"), coordsToSend));
    }

    protected void ShowSendButton() {
        sendpbtn.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) plainPh.getLayoutParams();
        params.addRule(RelativeLayout.LEFT_OF, R.id.send_plain);
        plainPh.setLayoutParams(params); //causes layout update
    }

    protected void HideSendButton() {
        sendpbtn.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) plainPh.getLayoutParams();
        params.addRule(RelativeLayout.LEFT_OF, 0);
        plainPh.setLayoutParams(params); //causes layout updatebt
    }

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

}
