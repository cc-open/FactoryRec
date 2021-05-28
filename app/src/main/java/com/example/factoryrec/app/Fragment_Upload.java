package com.example.factoryrec.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.factoryrec.R;
import com.example.factoryrec.ftp.FTPClientFunctions;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class Fragment_Upload extends MainFragment implements View.OnClickListener{

    private String TAGUPLOAD = "stormxzupload";
    private EditText mHostName, mUserName, mPassword, mUploadFilePath;
    private Button mUploadBut;
    private List<String> newImageVideoUris = new ArrayList<String>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_upload, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_om);

        mHostName = view.findViewById(R.id.hostname_et);
        mUserName = view.findViewById(R.id.username_et);
        mPassword = view.findViewById(R.id.password_et);
        mUploadFilePath = view.findViewById(R.id.upload_filepath_et);

        mUploadBut = view.findViewById(R.id.upload_but);

        mUploadBut.setOnClickListener(this);

        super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {// 图片选择结果回调

                if (selectList != null) {
                    for (int i = 0; i < selectList.size(); i++) {
                        Log.e(TAGUPLOAD, " upload uri getCompressPath = " + selectList.get(i).getCompressPath() + "  cut path = " + selectList.get(i).getCutPath() + "  getPath = " + selectList.get(i).getPath());
                        newImageVideoUris.add(selectList.get(i).getPath());
                    }
                    Log.e(TAGUPLOAD, " upload uri");
                }
            }
        }
    }

    public void updateDeleteImages(List<LocalMedia> list) {

        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                newImageVideoUris.add(list.get(i).getPath());
            }
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.upload_but:
                // upload files
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 网络必现在子线程中执行
                        uploadFiles(mHostName.getText().toString(), mUserName.getText().toString(), mPassword.getText().toString());
                    }
                }).start();

                Log.e(TAGUPLOAD, " upload_but click");
                break;
        }
    }

    private void uploadFiles(String hostname, String username, String password) {

        Log.e(TAGUPLOAD, " hostname = " + hostname + "  username = " + username + "  password = " + password + "  name = ");
        // TODO 可以首先去判断一下网络
        FTPClientFunctions ftpClient = new FTPClientFunctions();

//        boolean connectResult = ftpClient.ftpConnect("ftp.arcsoft.com.cn", "CoolPad_DLNA", "weC5uq9G", 21);
        // 连接FTP
        boolean connectResult = ftpClient.ftpConnect(hostname, username, password, 21);

        if (connectResult) {
            Log.i(TAGUPLOAD, "connect ftp success");

            // 根据日期创建目录
            String time = getTime();
            String filePathDir = mUploadFilePath.getText().toString() + "/" + time;
            boolean createDir = ftpClient.ftpMkdirDir(filePathDir);

            if (createDir) {
                // 查看目录是否存在，并进入此目录
                boolean changeDirResult = ftpClient.ftpChangeDir(filePathDir);

                Log.i(TAGUPLOAD, "changeDirResult = " + changeDirResult);
                if (changeDirResult) {
                    // 查找文件，并上传
                    String submitTimer = getTime();
                    File fileNameDir = mActivity.getApplicationContext().getExternalFilesDir(null);

                    File mNewFile = new File(fileNameDir.getAbsolutePath() + File.separator + "/" + submitTimer +"/");
                    File[] tempList = mNewFile.listFiles();
                    boolean uploadResult = false;
                    setUploadButEnable(false);
                    showShowToast("正在进行文件上传");
                    if (tempList != null) {
                        for (int i = 0; i < tempList.length; i++) {
                            if (tempList[i].isFile()) {
                                // 上传文件
                                uploadResult = ftpClient.ftpUpload(tempList[i].getAbsolutePath(), tempList[i].getName(), "");
                            }
                        }
                    }

                    if (newImageVideoUris != null) {
                        for (int i = 0; i < newImageVideoUris.size(); i++) {
                            File file = new File(newImageVideoUris.get(i));
                            uploadResult = ftpClient.ftpUpload(file.getAbsolutePath(), file.getName(), "");
                        }
                    }

                    if (uploadResult) {
                        Log.i(TAGUPLOAD, "uploadResult success");
                        // 断开连接
                        boolean disConnectResult = ftpClient.ftpDisconnect();
                        if (disConnectResult) {
                            setUploadButEnable(true);
                            showShowToast("文件上传成功");
                            Log.e(TAGUPLOAD, "disConnectResult sucess");
                        } else {
                            Log.e(TAGUPLOAD, "disConnectResult fail");
                            showShowToast("断开FTP 连接失败");
                        }
                    } else {
                        Log.i(TAGUPLOAD, "uploadResult fail");
                        showShowToast("上传失败，请查看网络是否稳定");
                    }
                } else {
                    Log.i(TAGUPLOAD, "ftpChangeDir fail");
                    showShowToast("检查目录失败，请查看网络是否稳定");
                }
            } else {
                Log.i(TAGUPLOAD, "connect ftp fail");
                showShowToast("创建存储目录失败");
            }




        } else {
            Log.i(TAGUPLOAD, "connect ftp fail");
            showShowToast("登录失败，请检查FTP 输入内容是否正确");
        }
    }

    private void showShowToast(final String message) {

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = getContext();
                if (context == null) return;
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUploadButEnable(final boolean isEnable) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mUploadBut != null) {
                    if (!isEnable) {
                        mUploadBut.setText("上传中...");
                    } else {
                        mUploadBut.setText("上传");
                    }
                    mUploadBut.setEnabled(isEnable);
                }
            }
        });

    }

    private String getTime() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        String monS = "" + month;
        if (month < 10) {
            monS = "0" + monS;
        }
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dayS = "" + day;
        if (day < 10) {
            dayS = "0" + dayS;
        }
        return "" + year + monS + dayS;
    }
}

