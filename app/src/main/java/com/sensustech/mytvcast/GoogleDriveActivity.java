package com.sensustech.mytvcast;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.ironsource.mediationsdk.IronSource;
import com.sensustech.mytvcast.Adapters.GDAdapter;
import com.sensustech.mytvcast.Utils.AdsManager;
import com.sensustech.mytvcast.utils.GoogleDrive.DriveServiceHelper;
import com.sensustech.mytvcast.utils.ItemClickSupport;
import com.sensustech.mytvcast.utils.StreamingManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleDriveActivity extends AppCompatActivity {

    private static final String TAG = "GoogleDriveActivity";
    private RecyclerView recyclerView;
    private GDAdapter adapter;
    private LinearLayoutManager manager;
    private Toolbar toolbar;
    private List<File> files = new ArrayList<>();
    private DriveServiceHelper mDriveServiceHelper;
    private String gdPath = "";
    private ProgressDialog progressDialog;
    private ArrayList<File> paths = new ArrayList<>();
    private boolean needShowAds = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler);
        setSupportActionBar(toolbar);
        setTitle("Google Drive");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                File f = files.get(position);
                if (!f.getMimeType().contains("folder")) {
                    if (!StreamingManager.getInstance(GoogleDriveActivity.this).isDeviceConnected()) {
                        startActivity(new Intent(GoogleDriveActivity.this, ConnectActivity.class));
                        return;
                    }
                    shareFile(f.getName(), f.getId(), f.getMimeType());
                } else {
                    setTitle(f.getName());
                    gdPath = f.getId();
                    paths.add(f);
                    loadData();
                }
            }
        });
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(GoogleDriveActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));
        SharedPreferences prefs = getSharedPreferences("googledrive-mytvcast", MODE_PRIVATE);
        if (prefs.getString("selectedAccount", null) != null) {
            boolean accFound = false;
            for (Account acc : credential.getAllAccounts()) {
                if (acc.name.equals(prefs.getString("selectedAccount", null))) {
                    credential.setSelectedAccount(acc);
                    Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).setApplicationName("My TV Cast").build();
                    mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                    loadData();
                    accFound = true;
                    break;
                }
            }
            if (!accFound)
                requestSignIn();
        } else {
            requestSignIn();
        }
    }


    public void showAds() {
        if(!AdsManager.getInstance().isPremium(this)) {
            AdsManager.getInstance().showAds();
        }
    }

    private static final int REQUEST_CODE_SIGN_IN = 1;

    private void requestSignIn() {
        needShowAds = true;
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestScopes(new Scope(DriveScopes.DRIVE)).build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    public void requestSignOut() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestScopes(new Scope(DriveScopes.DRIVE)).build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
        client.signOut();
    }

    private void shareFile(String fileName, String fileId, String mime) {
        String fileUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
        if (fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg") || fileName.toLowerCase().endsWith(".gif")) {
            StreamingManager.getInstance(GoogleDriveActivity.this).showContent(fileName, mime, fileUrl);
        } else if (fileName.toLowerCase().endsWith(".mov") || fileName.toLowerCase().endsWith(".mp4")) {
            StreamingManager.getInstance(GoogleDriveActivity.this).playMedia(fileName, mime, fileUrl);
        } else if (fileName.toLowerCase().endsWith(".mp3") || fileName.toLowerCase().endsWith(".wav")) {
            StreamingManager.getInstance(GoogleDriveActivity.this).playAudio(fileName, mime, fileUrl);
        }
        // showAds();
    }

    public void loadData() {
        if (mDriveServiceHelper != null) {
            Log.d("Test","loadData");
            showProgress("Loading files...");
            mDriveServiceHelper.queryFiles(gdPath)
                    .addOnSuccessListener(new OnSuccessListener<FileList>() {
                        @Override
                        public void onSuccess(FileList fileList) {
                            hideProgress();
                            List<File> sortedFiles = new ArrayList<>();
                            for (File f : fileList.getFiles()) {
                                if(f.getOwnedByMe()) {
                                    if (!f.getMimeType().contains("folder")) {
                                        String fileName = f.getName();
                                        if (fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg") || fileName.toLowerCase().endsWith(".gif")) {
                                            sortedFiles.add(f);
                                        } else if (fileName.toLowerCase().endsWith(".mov") || fileName.endsWith(".mp4")) {
                                            sortedFiles.add(f);
                                        } else if (fileName.toLowerCase().endsWith(".mp3") || fileName.toLowerCase().endsWith(".wav")) {
                                            sortedFiles.add(f);
                                        }
                                    } else {
                                        sortedFiles.add(f);
                                    }
                                }
                            }
                            files = sortedFiles;
                            adapter = new GDAdapter(files, GoogleDriveActivity.this);
                            recyclerView.setAdapter(adapter);
                            if(needShowAds) {
                                // showAds();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            exception.printStackTrace();
                            hideProgress();
                            if(exception instanceof UserRecoverableAuthIOException) {
                                UserRecoverableAuthIOException e = (UserRecoverableAuthIOException) exception;
                                startActivityForResult(e.getIntent(), REQUEST_CODE_SIGN_IN);
                            }
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    handleSignInResult(resultData);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleAccount) {
                        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(GoogleDriveActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));
                        credential.setSelectedAccount(googleAccount.getAccount());
                        SharedPreferences prefs = getSharedPreferences("googledrive-mytvcast", MODE_PRIVATE);
                        prefs.edit().putString("selectedAccount", googleAccount.getAccount().name).commit();
                        Drive googleDriveService =
                                new Drive.Builder(
                                        AndroidHttp.newCompatibleTransport(),
                                        new GsonFactory(),
                                        credential)
                                        .setApplicationName("My TV Cast")
                                        .build();
                        mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                        loadData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "Unable to sign in.", exception);
                    }
                });
    }

    private void showProgress(String text) {
        progressDialog = new ProgressDialog(GoogleDriveActivity.this);
        progressDialog.setMessage(text);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (paths.size() == 0) {
            finish();
        } else {
            paths.remove(paths.size() - 1);
            if (paths.size() == 0) {
                gdPath = "";
                setTitle("Google Drive");
            } else {
                gdPath = paths.get(paths.size() - 1).getId();
                setTitle(paths.get(paths.size() - 1).getName());
            }
            loadData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.exit_menu, menu);
        MenuItem exitItem = menu.findItem(R.id.action_exit);
        exitItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences prefs = getSharedPreferences("googledrive-mytvcast", MODE_PRIVATE);
                prefs.edit().putString("selectedAccount", null).commit();
                requestSignOut();
                finish();
                return true;
            }
        });
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IronSource.onResume(this);
    }

}