// Copyright 2014 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.wsapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.neostra.electronic.Electronic;
import com.neostra.electronic.ElectronicCallback;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

//


public class ElectronicHandler implements ElectronicCallback {
    private Electronic mElectronic;

    public ElectronicHandler(){

        try {
            //  Block of code to try
            mElectronic = new Electronic.Builder().setDevicePath("/dev/ttyS4").builder();
        }
        catch(Exception e) {
            //  Block of code to handle errors
            Log.e("Init Electronic ",e.getMessage(),e);
        }
    }






    @Override
    public void electronicStatus(String s, String s1) {

    }

//    static {
//        System.loadLibrary("serialPort");
//    }
}

