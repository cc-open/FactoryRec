package com.example.factoryrec.app;

import android.app.Activity;
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
import android.widget.EditText;

import com.example.factoryrec.R;
import com.example.factoryrec.selector.MultiImageSelectorActivity;
import com.example.factoryrec.util.FileUtil;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class Fragment_OM extends MainFragment {

    private EditText mOMEditText = null;
    private EditText mOMConfirmEditText = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_om, container, false);
        mOMConfirmEditText = view.findViewById(R.id.om_confirm_et);
        mOMConfirmEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mItem.setConfirm_2(s + "");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mOMEditText = view.findViewById(R.id.om_et);
        mRecyclerView = view.findViewById(R.id.recycler_om);

        mOMEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mItem.setOMText(s + "");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {// 图片选择结果回调

                List<String> newImageUris = new ArrayList<String>();
                if (selectList != null) {
                    for (int i = 0; i < selectList.size(); i++) {
                        newImageUris.add(selectList.get(i).getCompressPath());
                    }
                    mItem.setOM_BadPic(newImageUris);
                }
            }
        }
    }

    public void updateDeleteImages(List<LocalMedia> list) {

        List<String> newImageUris = new ArrayList<String>();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                newImageUris.add(list.get(i).getCompressPath());
            }
            mItem.setOM_BadPic(newImageUris);
        }
    }

}

