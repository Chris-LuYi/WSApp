// Copyright 2014 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.wsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.content.ContextWrapper;
import android.os.Bundle;
import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugins.GeneratedPluginRegistrant;
//
import com.neostra.electronic.Electronic;
import com.neostra.electronic.ElectronicCallback;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends FlutterActivity implements ElectronicCallback {
  private static final String BATTERY_CHANNEL = "samples.flutter.io/battery";
  private static final String CHARGING_CHANNEL = "samples.flutter.io/charging";
  private static final String WS_CHANNEL = "samples.flutter.io/ws";
  private Electronic mElectronic;


  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {

//      BroadcastReceiver  localBroadcastManager;
//      WSBroadcastReceiver myLocalBroadcastReceiver;
//     new ElectronicCallback {
//       @Override
//       public void electronicStatus(String weight, String weightStatus) {
//         var a = weight
//         var b = weightStatus
//       //Callback in child thread
//         ...
//       }}
//     mElectronic = new Electronic("/dev/ttyS4",9600,cb);
      try {
          //  Block of code to try
          mElectronic = new Electronic.Builder().setDevicePath("/dev/ttyS4").setReceiveCallback(this).builder();
      }
      catch(Exception e) {
          //  Block of code to handle errors
          Log.e("Init Electronic ",e.getMessage(),e);
      }

      // 获取LocalBroadcastManager实例
//      localBroadcastManager = LocalBroadcastManager.getInstance(this);
//
//      // 设置IntentFilter的action
//      IntentFilter intentFilter = new IntentFilter();
//      intentFilter.addAction("ACTION_MY_LOCAL_BROADCAST");
//
//      // 动态注册广播
//      myLocalBroadcastReceiver = new WSBroadcastReceiver();
//      localBroadcastManager.registerReceiver(myLocalBroadcastReceiver,intentFilter);

      // ElectronicHandler eh = new ElectronicHandler();
    new EventChannel(flutterEngine.getDartExecutor(), WS_CHANNEL).setStreamHandler(
      new StreamHandler() {


        private BroadcastReceiver weightingStateChangeReceiver;
        @Override
        public void onListen(Object arguments, EventSink events) {
          weightingStateChangeReceiver = createWeightingStateChangeReceiver(events);
          registerReceiver(
            weightingStateChangeReceiver, new IntentFilter("ACTION_MY_LOCAL_BROADCAST"));
        }

        @Override
        public void onCancel(Object arguments) {
          unregisterReceiver(weightingStateChangeReceiver);
          weightingStateChangeReceiver = null;
        }
      }
    );

//      new MethodChannel(flutterEngine.getDartExecutor(), WS_CHANNEL).setMethodCallHandler(
//              new MethodCallHandler() {
//                  @Override
//                  public void onMethodCall(MethodCall call, Result result) {
//                      if (call.method.equals("getBatteryLevel")) {
//                          int batteryLevel = getBatteryLevel();
//
//                          if (batteryLevel != -1) {
//                              result.success(batteryLevel);
//                          } else {
//                              result.error("UNAVAILABLE", "Battery level not available.", null);
//                          }
//                      } else {
//                          result.notImplemented();
//                      }
//                  }
//              }
//      );

    new EventChannel(flutterEngine.getDartExecutor(), CHARGING_CHANNEL).setStreamHandler(
      new StreamHandler() {
        private BroadcastReceiver chargingStateChangeReceiver;
        @Override
        public void onListen(Object arguments, EventSink events) {
          chargingStateChangeReceiver = createChargingStateChangeReceiver(events);
          registerReceiver(
              chargingStateChangeReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }

        @Override
        public void onCancel(Object arguments) {
          unregisterReceiver(chargingStateChangeReceiver);
          chargingStateChangeReceiver = null;
        }
      }
    );

    new MethodChannel(flutterEngine.getDartExecutor(), BATTERY_CHANNEL).setMethodCallHandler(
      new MethodCallHandler() {
        @Override
        public void onMethodCall(MethodCall call, Result result) {
          if (call.method.equals("getBatteryLevel")) {
            int batteryLevel = getBatteryLevel();

            if (batteryLevel != -1) {
              result.success(batteryLevel);
            } else {
              result.error("UNAVAILABLE", "Battery level not available.", null);
            }
          } else {
            result.notImplemented();
          }
        }
      }
    );



      setTimeout(() -> {
          Log.i("Send Intent test","sdfsdfssssssssssssssssssssssss");

          Intent intent = new Intent("ACTION_MY_LOCAL_BROADCAST");
          intent.putExtra("weight","sss");
          intent.putExtra("weightStatus","s11111");
          sendBroadcast(intent);
      }, 5000);
  }

    public static void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception e){
                System.err.println(e);
            }
        }).start();
    }

  private BroadcastReceiver createWeightingStateChangeReceiver(final EventSink events) {
    return new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
         Log.i("Testfffffffffffff","sdfsdfssssssssssssssssssssssss");
          JSONObject result = new JSONObject();
          try {
              result.put("weight",intent.getStringExtra("weight"));
              result.put("weightStatus",intent.getStringExtra("weightStatus"));
              events.success(result.toString());
          } catch (JSONException e) {
              e.printStackTrace();
              events.error("UNAVAILABLE", "Charging status unavailable", null);
          }

      }
    };
  }

  private BroadcastReceiver createChargingStateChangeReceiver(final EventSink events) {
    return new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        if (status == BatteryManager.BATTERY_STATUS_UNKNOWN) {
          events.error("UNAVAILABLE", "Charging status unavailable", null);
        } else {
          boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                               status == BatteryManager.BATTERY_STATUS_FULL;
          events.success(isCharging ? "charging" : "discharging");
        }
      }
    };
  }

  private int getBatteryLevel() {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
      return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    } else {
      Intent intent = new ContextWrapper(getApplicationContext()).
          registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
      return (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
          intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    }
  }

  @Override
  public void electronicStatus(String s, String s1) {


      Log.i("Send electronicStatus",s + '-'+s1);

      Intent intent = new Intent("ACTION_MY_LOCAL_BROADCAST");
      intent.putExtra("weight",s);
      intent.putExtra("weightStatus",s1);

      sendBroadcast(intent);
  }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//    }
}
