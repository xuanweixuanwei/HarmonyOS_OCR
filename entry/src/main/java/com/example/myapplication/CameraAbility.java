package com.example.myapplication;

import com.example.myapplication.slice.CameraAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.bundle.IBundleManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.camera.CameraKit;
import ohos.media.camera.device.CameraInfo;
import ohos.media.camera.params.ResultKey;

import java.util.List;

public class CameraAbility extends Ability {


    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(CameraAbilitySlice.class.getName());

//        reqPermissionsToTakePhoto();
    }


//    @Override
//    public void onRequestPermissionsFromUserResult (int requestCode, String[] permissions, int[] grantResults) {
//
//        switch (requestCode) {
//            case 4: {
//                // 匹配requestPermissions的requestCode
//                if (grantResults.length > 0
//                        && grantResults[0] == IBundleManager.PERMISSION_GRANTED) {
////                        openCamera();
//                    // 权限被授予
//                    // 注意：因时间差导致接口权限检查时有无权限，所以对那些因无权限而抛异常的接口进行异常捕获处理
//                } else {
////                        terminateAbility();
//                    // 权限被拒绝
//                }
//                return;
//            }
//        }
//    }

//    private void openCamera(){
//        // 获取CameraKit对象
//        CameraKit cameraKit = CameraKit.getInstance(getApplicationContext());
//        if (cameraKit == null) {
//            // 处理cameraKit获取失败的情况
//        }else {
//            try {
//                // 获取当前设备的逻辑相机列表
//                String[] cameraIds = cameraKit.getCameraIds();
//                if (cameraIds.length <= 0) {
//                    HiLog.error(LABEL, "cameraIds size is 0");
//                }
//                for (int i = 0; i < cameraIds.length; i++) {
//                    CameraInfo cameraInfo = cameraKit.getCameraInfo(cameraIds[i]);
//                    ohos.media.camera.device.CameraAbility cameraAbility = cameraKit.getCameraAbility(cameraIds[i]);
//                    int facingType = cameraInfo.getFacingType();
//                    String logicalId = cameraInfo.getLogicalId();
//                    List<String> physicalIdList = cameraInfo.getPhysicalIdList();
//                    List<ResultKey.Key<?>> supportedResults = cameraAbility.getSupportedResults();
//
//                }
//            } catch (IllegalStateException e) {
//                // 处理异常
//
//            }
//        }
//    }

}
