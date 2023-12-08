package com.example.myapplication;

import com.example.myapplication.slice.OCRAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.bundle.IBundleManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.camera.CameraKit;
import ohos.media.camera.device.CameraAbility;
import ohos.media.camera.device.CameraInfo;
import ohos.media.camera.params.ResultKey;

import java.util.List;

public class OCRAbility extends Ability {

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(OCRAbilitySlice.class.getName());
//        reqPermissionsToTakePhoto();
    }

}
