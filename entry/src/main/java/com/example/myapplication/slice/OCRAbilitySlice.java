package com.example.myapplication.slice;


import com.example.myapplication.CameraAbility;
import com.example.myapplication.HelloPageAbility;
import com.example.myapplication.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.aafwk.content.Operation;
import ohos.agp.components.*;
import ohos.agp.components.surfaceprovider.SurfaceProvider;
import ohos.agp.graphics.Surface;
import ohos.agp.window.dialog.ToastDialog;
import ohos.ai.cv.common.*;
import ohos.ai.cv.text.ITextDetector;
import ohos.ai.cv.text.Text;
import ohos.ai.cv.text.TextConfiguration;
import ohos.ai.cv.text.TextDetectType;
import ohos.app.Context;

import ohos.bundle.IBundleManager;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.camera.CameraKit;
import ohos.media.camera.device.*;

import ohos.media.image.ImageReceiver;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.miscservices.pasteboard.PasteData;
import ohos.miscservices.pasteboard.SystemPasteboard;
import ohos.security.SystemPermission;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class OCRAbilitySlice extends AbilitySlice {
    static final HiLogLabel LABEL = new HiLogLabel(HiLog.LOG_APP, 0x5555, "HHHHHHHHHHHHHHHHHHHH");
    TextField tf_recognize_result;
    Button bt_take_photo ;
    Button bt_copy_result2 ;
    Button bt_clear_input ;
    Image image_to_recognize;
    ITextDetector textDetector;
    VisionCallback<Text> visionCallback;

    PixelMap pixelMap;
    Text text ;
//
//    Byte[] pictureByteArray;
//
//    VisionImage visionImage ;
//    ConnectionCallback connectionCallback ;
    Context context;
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        context = this;
        initData();
        addListener();
        if(intent.getStringParam("photo")!=null){
            String photos = intent.getStringParam("photo");
            tf_recognize_result.setText(photos);

            ImageSource.SourceOptions sourceOptions = new ImageSource.SourceOptions();
            sourceOptions.formatHint= "image/jpg";
            ImageSource imageSource = ImageSource.create(photos,sourceOptions);
            PixelMap pixelmap = imageSource.createPixelmap(null);
            image_to_recognize.setPixelMap(pixelmap);
        }
        wordRecognition();
    }

    private void initData() {
        tf_recognize_result = (TextField) findComponentById(ResourceTable.Id_tf_recognize_result);
        bt_take_photo = (Button) findComponentById(ResourceTable.Id_bt_take_photo);
        bt_copy_result2 = (Button) findComponentById(ResourceTable.Id_bt_copy_result);
        bt_clear_input = (Button) findComponentById(ResourceTable.Id_clear_input);
        image_to_recognize = (Image) findComponentById(ResourceTable.Id_image_to_recognize);


//         visionImage = VisionImage.fromPixelMap(image_to_recognize.getPixelMap());
    }
    private void addListener() {
        bt_clear_input.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                new ToastDialog(context).setText("清除文本框内容").show();
                tf_recognize_result.setText("");
            }
        });

        bt_take_photo.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {

//                wordRecognition();
//            reqPermissionsToTakePhoto();
                requestPermission();
//            TODO 网络请求

            }
        });

        bt_copy_result2.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                SystemPasteboard pasteboard = SystemPasteboard.getSystemPasteboard(getContext());
                if (pasteboard != null) {
                    pasteboard.setPasteData(PasteData.creatPlainTextData(tf_recognize_result.getText().trim()));
                }
            }
        });
    }
//    static final HiLogLabel LABEL = new HiLogLabel(HiLog.LOG_APP, 0x5555, "HHHHHHHHHHHHHHHHHHHH");
    int MY_PERMISSIONS_REQUEST_CAMERA = 4;
    String READ_USER_STORAGE = "ohos.permission.READ_USER_STORAGE";
    String WRITE_USER_STORAGE ="ohos.permission.WRITE_USER_STORAGE";
    String CAMERA = "ohos.permission.CAMERA";
    private void reqPermissionsToTakePhoto() {

        String[] permissions = { SystemPermission.WRITE_USER_STORAGE, SystemPermission.READ_USER_STORAGE, SystemPermission.CAMERA};

        requestPermissionsFromUser(permissions, 4);

        if (verifySelfPermission(CAMERA) != IBundleManager.PERMISSION_GRANTED) {
            // 应用未被授予权限
            if (canRequestPermission(CAMERA)) {
                // 是否可以申请弹框授权(首次申请或者用户未选择禁止且不再提示)
                requestPermissionsFromUser(
                        new String[] {  SystemPermission.WRITE_USER_STORAGE, SystemPermission.READ_USER_STORAGE, SystemPermission.CAMERA},4);
            } else {
                // 显示应用需要权限的理由，提示用户进入设置授权
            }
        } else {
            toAuthAfterPage();
//            openCamera();
            // 权限已被授予
        }
    }

    private void requestPermission() {
        String[] permissions = {
                // 存储权限
                SystemPermission.WRITE_MEDIA,
                // 相机权限
                SystemPermission.CAMERA,
                SystemPermission.READ_MEDIA
        };
        List<String> permissionFiltereds = Arrays.stream(permissions)
                .filter(permission -> verifySelfPermission(permission) != IBundleManager.PERMISSION_GRANTED)
                .collect(Collectors.toList());
        if (permissionFiltereds.isEmpty()) {
            toAuthAfterPage();
//            PermissionBridge.getHandler().sendEvent(EVENT_PERMISSION_GRANTED);
            return;
        }
        requestPermissionsFromUser(permissionFiltereds.toArray(new String[permissionFiltereds.size()]),
                MY_PERMISSIONS_REQUEST_CAMERA);
    }


    private void toAuthAfterPage() {
        Intent secondIntent = new Intent();
        // 指定待启动FA的bundleName和abilityName
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName(getBundleName())
                .withAbilityName(CameraAbility.class.getName())
                .build();
        secondIntent.setOperation(operation);
        // startAbility接口实现启动另一个页面
        startAbility(secondIntent);
    }

    private void toHelloPage() {
        Intent secondIntent = new Intent();
        // 指定待启动FA的bundleName和abilityName
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName(getBundleName())
                .withAbilityName(HelloPageAbility.class.getName())
                .build();
        secondIntent.setOperation(operation);
        // startAbility接口实现启动另一个页面
        startAbility(secondIntent);
    }


    CameraKit cameraKit;
    private void openCamera(){
        int targetCameraID=999;
        // 前置相机类型
        int frontCamera = CameraInfo.FacingType.CAMERA_FACING_FRONT;
// 后置相机类型
        int backCamera = CameraInfo.FacingType.CAMERA_FACING_BACK;
// 其他相机类型
        int otherCamera = CameraInfo.FacingType.CAMERA_FACING_OTHERS;


        // 获取CameraKit对象
         cameraKit = CameraKit.getInstance(getApplicationContext());
        if (cameraKit == null) {
            new ToastDialog(getContext()).setText("获取相机失败，功能无法使用").show();
            return;
            // 处理cameraKit获取失败的情况
        }else {
            try {// 选择想要创建的相机类型，如果不存在该类型相机，则返回false
                boolean isCameraCreated = openCameraByFacingType(backCamera);
                // 获取当前设备的逻辑相机列表
                String[] cameraIds = cameraKit.getCameraIds();
                if (cameraIds.length <= 0) {
                    HiLog.error(LABEL, "cameraIds size is 0");
                }
/*                for (int i = 0; i < cameraIds.length; i++) {
                    CameraInfo cameraInfo = cameraKit.getCameraInfo(cameraIds[i]);
                    CameraAbility cameraAbility = cameraKit.getCameraAbility(cameraIds[i]);
                    int facingType = cameraInfo.getFacingType();
                    String logicalId = cameraInfo.getLogicalId();
                    List<String> physicalIdList = cameraInfo.getPhysicalIdList();
                    List<ResultKey.Key<?>> supportedResults = cameraAbility.getSupportedResults();
                    HiLog.info(LABEL,supportedResults.toString());
                    CameraInfo.FacingType.CAMERA_FACING_FRONT
                }*/
            } catch (IllegalStateException e) {
                // 处理异常

            }
        }
    }
    // 相机创建和相机运行时的回调
    CameraStateCallbackImpl cameraStateCallback = new CameraStateCallbackImpl();
    // 执行回调的EventHandler
    EventHandler eventHandler = new EventHandler(EventRunner.create("CameraCb"));
    // 根据类型创建相机的方法
    private boolean openCameraByFacingType(int facingType) {
//        CameraKit cameraKit = CameraKit.getInstance(getApplicationContext());
        for(String cameraId : cameraKit.getCameraIds()) {
            CameraInfo cameraInfo = cameraKit.getCameraInfo(cameraId);
            if(facingType == cameraInfo.getFacingType()) {

                cameraKit.createCamera(cameraId, cameraStateCallback, eventHandler);
//                TODO
                return true;
            }
        }
        return false;
    }



    // Surface提供对象
    private SurfaceProvider surfaceProvider;

    private void initSurface() {
        surfaceProvider = new SurfaceProvider(this);
        DirectionalLayout.LayoutConfig params = new DirectionalLayout.LayoutConfig(
                ComponentContainer.LayoutConfig.MATCH_PARENT, ComponentContainer.LayoutConfig.MATCH_PARENT);
        surfaceProvider.setLayoutConfig(params);
        surfaceProvider.pinToZTop(false);
//      TODO  surfaceProvider.getSurfaceOps().get().addCallback(new SurfaceCallBack());
        ((ComponentContainer)
                findComponentById(ResourceTable.Id_surface_container)).addComponent(surfaceProvider);
    }

    private FrameStateCallback frameStateCallbackImpl = new FrameStateCallback(){
        @Override
        public void onFrameStarted(Camera camera, FrameConfig frameConfig, long frameNumber, long timestamp) {
//        ...
        }
        @Override
        public void onFrameProgressed(Camera camera, FrameConfig frameConfig, FrameResult frameResult) {
//        ...
        }
        @Override
        public void onFrameFinished(Camera camera, FrameConfig frameConfig, FrameResult frameResult) {
//        ...
        }
        @Override
        public void onFrameError(Camera camera, FrameConfig frameConfig, int errorCode, FrameResult frameResult) {
//        ...
        }
        @Override
        public void onCaptureTriggerStarted(Camera camera, int captureTriggerId, long firstFrameNumber) {
//        ...
        }
        @Override
        public void onCaptureTriggerFinished(Camera camera, int captureTriggerId, long lastFrameNumber) {
//        ...
        }
        @Override
        public void onCaptureTriggerInterrupted(Camera camera, int captureTriggerId) {
//        ...
        }
    };

    // 相机设备
    private Camera cameraDevice;
    // 相机预览模板
    private Surface previewSurface;
    // 相机配置模板
    private CameraConfig.Builder cameraConfigBuilder;
    // 图像帧数据接收处理对象
    private ImageReceiver imageReceiver;

    private final class CameraStateCallbackImpl extends CameraStateCallback {
        @Override
        public void onCreated(Camera camera) {
            cameraDevice = camera;
            previewSurface = surfaceProvider.getSurfaceOps().get().getSurface();
            cameraConfigBuilder = camera.getCameraConfigBuilder();
            if (cameraConfigBuilder == null) {
                HiLog.error(LABEL, "onCreated cameraConfigBuilder is null");
                return;
            }
            // 配置预览的Surface
            cameraConfigBuilder.addSurface(previewSurface);
            // 配置拍照的Surface
            cameraConfigBuilder.addSurface(imageReceiver.getRecevingSurface());
            // 配置帧结果的回调
            cameraConfigBuilder.setFrameStateCallback(frameStateCallbackImpl, eventHandler);
            try {
                // 相机设备配置
                camera.configure(cameraConfigBuilder.build());
            } catch (IllegalArgumentException e) {
                HiLog.error(LABEL, "Argument Exception");
            } catch (IllegalStateException e) {
                HiLog.error(LABEL, "State Exception");
            }
        }
    }

    public void wordRecognition() {

        // 实例化ITextDetector接口
        textDetector = VisionManager.getTextDetector(context);

        text = new Text();
        // 实例化VisionImage对象image，并传入待检测图片pixelMap

        VisionImage image = VisionImage.fromPixelMap(image_to_recognize.getPixelMap());

        // 定义VisionCallback<Text>回调，异步模式下用到
//        VisionCallback<Text> visionCallback = getVisionCallback();

        // 定义ConnectionCallback回调，实现连接能力引擎成功与否后的操作
        ConnectionCallback connectionCallback = getConnectionCallback(image, visionCallback);

        // 建立与能力引擎的连接
        int init = VisionManager.init(context, connectionCallback);
//        int result = textDetector.detect(image, text, visionCallback);

        // 实例化Text对象text
        Text text = new Text();

        // 通过TextConfiguration配置textDetector()方法的运行参数
        TextConfiguration.Builder builder = new TextConfiguration.Builder();
        builder.setProcessMode(VisionConfiguration.MODE_IN);
        builder.setDetectType(TextDetectType.TYPE_TEXT_DETECT_FOCUS_SHOOT); // 此处变量名将会被调整
        builder.setLanguage(TextConfiguration.AUTO);
        TextConfiguration config = builder.build();
        textDetector.setVisionConfiguration(config);
        int prepare = textDetector.prepare();
        int result2 = textDetector.detect(image, text, null); // 同步
        sendResult(text.getValue());
    }

    private ConnectionCallback getConnectionCallback(VisionImage image, VisionCallback<Text> visionCallback) {
        return new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                // 实例化Text对象text
                Text text = new Text();
                // 通过TextConfiguration配置textDetector()方法的运行参数
                TextConfiguration.Builder builder = new TextConfiguration.Builder();
                builder.setProcessMode(VisionConfiguration.MODE_IN);
                builder.setDetectType(TextDetectType.TYPE_TEXT_DETECT_FOCUS_SHOOT); // 此处变量名将会被调整
                builder.setLanguage(TextConfiguration.AUTO);
                TextConfiguration config = builder.build();
                textDetector.setVisionConfiguration(config);
                int prepare = textDetector.prepare();
                int result2 = textDetector.detect(image, text, null); // 同步
                sendResult(text.getValue());

            }

            @Override
            public void onServiceDisconnect() {
                // 释放 成功：同步结果码为0，异步结果码为700
                textDetector.release();
                if (pixelMap != null) {
                    pixelMap.release();
                    pixelMap = null;
                }
                VisionManager.destroy();
            }
        };
    }




    public void sendResult(String value) {
        if (textDetector != null) {
            textDetector.release();
        }
        if (pixelMap != null) {
            pixelMap.release();
            pixelMap = null;
            VisionManager.destroy();
        }
        if (value != null) {
            tf_recognize_result.setText(value);
        }

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
