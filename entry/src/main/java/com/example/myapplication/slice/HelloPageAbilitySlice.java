package com.example.myapplication.slice;

import com.example.myapplication.CameraAbility;
import com.example.myapplication.OCRAbility;
import com.example.myapplication.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;

import java.util.Timer;
import java.util.TimerTask;

public class HelloPageAbilitySlice extends AbilitySlice {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_hello);
        toOcrAbility();
    }

    private void toOcrAbility() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Intent secondIntent = new Intent();
                // 指定待启动FA的bundleName和abilityName
                Operation operation = new Intent.OperationBuilder()
                        .withDeviceId("")
                        .withBundleName(getBundleName())
                        .withAbilityName(OCRAbility.class.getName())
                        .build();
                secondIntent.setOperation(operation);
                // startAbility接口实现启动另一个页面
                startAbility(secondIntent);
            }
        };
        timer.schedule(timerTask,3000);
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
