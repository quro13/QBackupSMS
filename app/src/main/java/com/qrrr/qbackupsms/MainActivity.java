package com.qrrr.qbackupsms;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import androidx.activity.result.contract.ActivityResultContracts;

public class MainActivity extends AppCompatActivity {

    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    private static final int SEND_SMS_PERMISSIONS_REQUEST = 2;
    private static MainActivity inst;
    private EditText outputText;
    private TextView textStorageStat;
    private TextView textReadSMSStat;
    private TextView textSMSCount;
    private TextView textSMSExport;
    private static final int MY_EXTERNAL_STORAGE_PERMISSION = 1000;
    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;
    private static final int REQUEST_CODE_APP_CHOOSER = 112;
    private int smsExported;

    JSONArray companyList;
    JSONArray smsList;
    Resources res ;
    String app_name;
    String downloadFolder;
    Uri downloadUri;
    Uri smsUri;
    Uri smsUriInbox;
    ActivityResultLauncher<String[]> requestPermissionLauncher;
    ContentResolver contentReso;
    ProgressBar pgExportSMS;
    //    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static FileWriter file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();


        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                        permissions -> getPermissionStat()
                );
        this.outputText = this.findViewById(R.id.editText);
        this.textStorageStat = this.findViewById(R.id.tvStorageStat);
        this.textReadSMSStat = this.findViewById(R.id.tvReadSMSStat);
        this.textSMSCount = this.findViewById(R.id.tvSMSCount);
        this.textSMSExport = this.findViewById(R.id.tvSMSExport);
        this.pgExportSMS = this.findViewById(R.id.progressBarExportSMS);

        res = getResources();
        app_name = res.getString(R.string.app_name);
        downloadFolder = Environment.DIRECTORY_DOWNLOADS + File.separator + app_name;
        downloadUri = Uri.parse(downloadFolder);
        contentReso = this.getContentResolver();
        smsUriInbox = Telephony.Sms.Inbox.CONTENT_URI;
        smsUri = Telephony.Sms.CONTENT_URI;
        smsExported = 0;
        //openSMSappChooser();
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
//                != PackageManager.PERMISSION_GRANTED) {
//            getPermissionToReadSMS();
//        } else {
//            refreshSmsInbox();
//        }
//        int hasReadSmsPermission = checkSelfPermission(Manifest.permission.SEND_SMS);
//        if (hasReadSmsPermission != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSIONS_REQUEST);
//        }

        getPermissionStat();
    }

    /**
     * Check and display Permission status
     */
    public void checkPermission(){
//        getPermissionToReadSMS();
//        getPermissionToSendSMS();
//        askPermissionAndBrowseFile();
        askPermissionMultiple();
        getPermissionStat();
    }

    /**
     * Permission button
     * @param view View
     */
    public void checkPermission(View view){
        checkPermission();
    }

    /**
     * Open popup choose the SMS default app
     * */
    public void openSMSappChooser() {


//        Intent setSmsAppIntent =  new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
//        setSmsAppIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
//        startActivityForResult(setSmsAppIntent, 1);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            RoleManager roleManager = this.getSystemService(RoleManager.class);
            Intent roleRequestIntent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS);
            startActivityForResult(roleRequestIntent, REQUEST_CODE_APP_CHOOSER);
        } else {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
            startActivity(intent);
        }

    }

    /**
     * Create SMS button. Insert SMS into SMS box
     * @param view View
     */
    public void Create_SMS(View view) {
        getPermissionToSendSMS();
        openSMSappChooser();
//        int hasReadSmsPermission = checkSelfPermission(Manifest.permission.SEND_SMS);
//        if (hasReadSmsPermission != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, REQUEST_CODE_ASK_PERMISSIONS);
//            return;
//        }
//        try {
//            ContentValues values = new ContentValues();
//            values.put("_id", "777777");
//            values.put("thread_id", "999");
//            values.put("address", "+0909999000");//sender name
//            values.put("body", "this is my text 7");
//            values.put("date", "1611625995785");
//            contentReso.insert(smsUri, values);
//        }
//        catch (Exception ex){
//            ex.printStackTrace();
//        }
//
//        ContentValues values2 = new ContentValues();
//        values2.put("_id", "999998");
//        values2.put("thread_id", "998");
//        values2.put("address", "+0909999000");//sender name
//        values2.put("body", "this is my text 2");
//        values2.put("date", "1611625996790");
//        getContentResolver().insert(Telephony.Sms.Inbox.CONTENT_URI, values2);*/
        /*addSMSMulti(smsList);
        Toast.makeText(getBaseContext(), "Added SMS",
                Toast.LENGTH_SHORT).show();*/
        //outputText.setText(values.toString());

        deleteMultiSMS();
    }


    /**
     * Delete a SMS in inbox
     * @param id String - id of row to delete
     */
    public void deleteSMS(String id){
        getContentResolver().delete(
                Uri.parse(smsUri.toString()+'/'+ id), null, null);
    }

    public void deleteMultiSMS(){
//        Cursor c = getContentResolver().query(smsUri,
//                new String[] { "_id", "thread_id", "address",
//                        "person", "date", "body" }, null, null, null);
//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //Do something after 100ms
        deleteSMS("736111");
        deleteSMS("735111");
        deleteSMS("1000010");
        deleteSMS("1000012");
        deleteSMS("111111");
        deleteSMS("1000011");
        deleteSMS("1000013");
        deleteSMS("111112");
//            }
//        }, 1000);
    }

    /**
     * Insert a sms into SMS box
     * @param sms JSONObject contain sms info
     * @throws JSONException ex
     */
    public void addSMS(JSONObject sms) throws JSONException {
        JSONArray smsArray = sms.names();
        if(smsArray!=null) {
            ContentValues smsValues = new ContentValues();
            for (int i = 0; i < smsArray.length(); i++) {
                String key = smsArray.getString(i);
                if(!key.equals("_id")&&!key.equals("thread_id")) {
                    smsValues.put(key, sms.getString(key));
                }
            }

            //String contentReso2 = this.getContentResolver().toString();
            if (contentReso == null){
                contentReso = this.getContentResolver();
            }
            String type="1";
            try {
                type = sms.getString("type");
            }catch (Exception ignored){}
            if(type.equals("2")){
                smsUriInbox = Telephony.Sms.Sent.CONTENT_URI;
            }else {
                smsUriInbox = Telephony.Sms.Inbox.CONTENT_URI;
            }

            try {
                contentReso.insert(smsUriInbox, smsValues);
                smsValues.clear();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    /**
     * insert multiple sms into SMS box
     * @param smsArray JSONArray of JSONObject sms
     */
    public void addSMSMulti(JSONArray smsArray) {
        try {
            if (smsArray != null) {
                //for (int i = 0; i < smsArray.length(); i++) {
                for (int i = 0; i < 2; i++) {
                    addSMS(smsArray.getJSONObject(i));
                }
            }
        }
        catch (JSONException ignored){}

    }

//    public void refreshSmsInbox() {
////        ContentResolver contentResolver = getContentResolver();
////        Cursor smsInboxCursor = contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI /*Uri.parse("content://sms/inbox")*/, null, null, null, null);
////        int indexBody = smsInboxCursor.getColumnIndex("body");
////        int indexAddress = smsInboxCursor.getColumnIndex("address");
////        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
////        //arrayAdapter.clear();
////        do {
////            String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
////                    "\n" + smsInboxCursor.getString(indexBody) + "\n";
////            //arrayAdapter.add(str);
////        } while (smsInboxCursor.moveToNext());
////        smsInboxCursor.close();
//    }

    /**
     * Display popup for multi Permission
     */
    public void askPermissionMultiple() {
        try {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_EXTERNAL_STORAGE});
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Display popup for Reading SMS
     */
    public void getPermissionToReadSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_SMS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_SMS},
                    READ_SMS_PERMISSIONS_REQUEST);
        }
    }

    /**
     * Display popup for Sending SMS
     */
    public void getPermissionToSendSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.SEND_SMS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                    SEND_SMS_PERMISSIONS_REQUEST);
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
            }

        }
        else if(requestCode == MY_EXTERNAL_STORAGE_PERMISSION){
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                textStorageStat.setText("Storage permission granted");
            }else{
                textStorageStat.setText("Storage permission denied");
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Get Permission status
     */
    @SuppressLint("SetTextI18n")
    public void getPermissionStat(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            textStorageStat.setText("Storage permission denied");
        }else{
            textStorageStat.setText("Storage permission granted");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            textReadSMSStat.setText("Read SMS permission denied");
        }else{
            textReadSMSStat.setText("Read SMS permission granted");
        }
        countSMS();
    }

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    public void countSMS(){
        int countSMS;
        try {
            Cursor cursor = getContentResolver().query(smsUri, null, null, null, null);
            countSMS = cursor.getCount();
            cursor.close();
        }
        catch (Exception e){
            countSMS = 0;
        }
        textSMSCount.setText(String.valueOf(countSMS));
        pgExportSMS.setMax(countSMS);
    }

    /**
     * Export SMS button. Load all SMS from Device and save with JSon format in Download folder
     * @param view View
     */
    public void Read_SMS(View view){
//        int hasReadSmsPermission = checkSelfPermission(Manifest.permission.READ_SMS);
//        if (hasReadSmsPermission != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_ASK_PERMISSIONS);
//            return;
//        }
        getPermissionToReadSMS();
        File dir;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && false) {
            dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            final String relativePath = downloadFolder; // save directory
//                String fileName = "Your_File_Name"; // file name to save file with
//                String mimeType = "image/*"; // Mime Types define here
//                Bitmap bitmap = null; // your bitmap file to save

            final ContentValues contentValues = new ContentValues();
//                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
//                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
        }
        else{
            dir = Environment.getExternalStoragePublicDirectory(downloadFolder); // save directory
        }
        if(!dir.exists()){
            dir.mkdir();
        }

        Cursor cursor;
        //cursor = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
        cursor = getContentResolver().query(smsUri, null, null, null, null);
        int max = cursor.getCount();
        JSONObject[] company = new JSONObject[max];
        cursor.moveToFirst();
        try {
            for(int i = 0 ; i< max;i++){
                company[i] = new JSONObject();
//                company[i].put("Id", cursor.getString(0));
//                company[i].put("thread_id", cursor.getString(1));
//                company[i].put("address", cursor.getString(2));
//                company[i].put("person", cursor.getString(3));
//                company[i].put("date", cursor.getString(4));
//                company[i].put("date_sent", cursor.getString(5));
//                //company[i].put("sc_timestamp", cursor.getString(6));
//                company[i].put("protocol", cursor.getString(6));
//                company[i].put("read", cursor.getString(7));
//                company[i].put("status", cursor.getString(8));
//                company[i].put("type", cursor.getString(9));
//                company[i].put("reply_path_present", cursor.getString(10));
//                company[i].put("subject", cursor.getString(11));
//                company[i].put("body", cursor.getString(12));
                for(int j=0;j<cursor.getColumnCount();j++){
                    company[i].put(cursor.getColumnName(j), cursor.getString(j));
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        catch (JSONException ignored){}

        //myTextView.setText(cursor.getString(12));
        // JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        //JSONObject obj = new JSONObject();
        companyList = new JSONArray();

        try {
            for (int j = 0; j < company.length; j++) {
                companyList.put(company[j]);
                smsExported = j + 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            new Thread(() -> {
                for (int j = 0; j < company.length; j++) {
                    smsExported = j+1;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        textSMSExport.setText(String.valueOf(smsExported));
                        pgExportSMS.setProgress(smsExported);
                    });

                    try {
                        // Sleep for 100 milliseconds.
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();}

        try {
            outputText.setText(companyList.toString());
            Date date = new Date();
            file = new FileWriter(dir.getPath()+ File.separator+ app_name+sdf.format(date.getTime())+".json");
            file.write(companyList.toString());
            Toast.makeText(getBaseContext(), "File saved successfully in Download folder!",
                    Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();

        }
        finally {

            try {
                file.flush();
                file.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void updateInbox(final String smsMessage) {
        //arrayAdapter.insert(smsMessage, 0);
        //arrayAdapter.notifyDataSetChanged();
    }

    /**
     * Display popup for Browse files Permission
     */
    private void askPermissionAndBrowseFile()  {
        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23

            // Check if we have Call permission
            int permisson = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permisson != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_EXTERNAL_STORAGE_PERMISSION
                );
                //return;
            }
        //}
        //this.doBrowseFile(new View());
    }

    /**
     * Choose JSon file button
     * @param view View
     */
    public void doBrowseFile(View view)  {
        askPermissionAndBrowseFile();
        Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        chooseFileIntent.setType("application/json");
        chooseFileIntent.setDataAndType(downloadUri,"application/json");
        // Only return URIs that can be opened with ContentResolver
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);

//        chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file");
        startActivityForResult(chooseFileIntent, MY_RESULT_CODE_FILECHOOSER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Kiểm tra requestCode có trùng với REQUEST_CODE vừa dùng
        if(requestCode == MY_RESULT_CODE_FILECHOOSER) {

            // resultCode được set bởi DetailActivity
            // RESULT_OK chỉ ra rằng kết quả này đã thành công
            if(resultCode == Activity.RESULT_OK) {
                try {
                    smsList = loadJsonFromFile(data.getData());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                outputText.setText(smsList.toString());
                // Nhận dữ liệu từ Intent trả về
                final String result = "ok";//data.getStringExtra(DetailActivity.EXTRA_DATA);

                // Sử dụng kết quả result bằng cách hiện Toast
                Toast.makeText(this, "Result: " + result, Toast.LENGTH_LONG).show();
            }
        }
        else if(requestCode==REQUEST_CODE_APP_CHOOSER){
            if(resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "REQUEST_CODE_APP_CHOOSER", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Please set default SMS app!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Load all SMS from JSon file
     * @param filepath Uri filepath
     * @return JSONArray
     * @throws JSONException ex
     */
    public JSONArray loadJsonFromFile(Uri filepath) throws JSONException {
        BufferedReader reader;
        StringBuilder builder = new StringBuilder();
        try
        {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(filepath)));

            String line;
            while ((line = reader.readLine()) != null)
            {
                builder.append(line);
            }
            reader.close();
        }
        catch (IOException e) {e.printStackTrace();}
        // This responce will have Json Format String
        String responce = builder.toString();
        //JSONObject jsonObject  = new JSONObject(responce);
        return new JSONArray(responce);
    }
}