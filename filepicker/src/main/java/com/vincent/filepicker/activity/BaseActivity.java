package com.vincent.filepicker.activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.vincent.filepicker.FolderListHelper;
import com.vincent.filepicker.R;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Vincent Woo
 * Date: 2016/10/12
 * Time: 16:21
 */

public abstract class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int RC_READ_EXTERNAL_STORAGE = 123;
    private static final String TAG = BaseActivity.class.getName();

    protected FolderListHelper mFolderHelper;
    protected boolean isNeedFolderList;
    public static final String IS_NEED_FOLDER_LIST = "isNeedFolderList";

    abstract void permissionGranted();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isNeedFolderList = getIntent().getBooleanExtra(IS_NEED_FOLDER_LIST, false);
        if (isNeedFolderList) {
            mFolderHelper = new FolderListHelper();
            mFolderHelper.initFolderListView(this);
        }
    }

    @Override
    protected void onPostCreate( Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        readExternalStorage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Read external storage file
     */
    @AfterPermissionGranted(RC_READ_EXTERNAL_STORAGE)
    private void readExternalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            boolean isGranted = EasyPermissions.hasPermissions(this, getPermissionId());
            if (isGranted) {
                permissionGranted();
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.vw_rationale_storage),
                        RC_READ_EXTERNAL_STORAGE, getPermissionId());
            }
        }
        else {
            boolean isGranted = EasyPermissions.hasPermissions(this, READ_EXTERNAL_STORAGE);
            if (isGranted) {
                permissionGranted();
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.vw_rationale_storage),
                        RC_READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
        permissionGranted();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        // If Permission permanently denied, ask user again
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (EasyPermissions.hasPermissions(this, getPermissionId())) {
                    permissionGranted();
                } else {
                    finish();
                }
            }
            else {
                if (EasyPermissions.hasPermissions(this, READ_EXTERNAL_STORAGE)) {
                    permissionGranted();
                } else {
                    finish();
                }
            }
        }
    }

    public void onBackClick(View view) {
        finish();
    }
    public String getPermissionId(){
        if (this.getLocalClassName().toString().equals("com.vincent.filepicker.activity.VideoPickActivity")){
            return READ_MEDIA_VIDEO;
        }
        else if (this.getLocalClassName().toString().equals("com.vincent.filepicker.activity.ImagePickActivity")){
            return READ_MEDIA_IMAGES;
        }
        else return READ_MEDIA_AUDIO;
    }
}