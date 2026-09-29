package com.example.yunzhou;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(YzExportPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
