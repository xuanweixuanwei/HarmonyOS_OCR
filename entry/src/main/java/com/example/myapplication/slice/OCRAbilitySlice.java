package com.example.myapplication.slice;


import com.example.myapplication.CameraAbility;
import com.example.myapplication.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.*;
import ohos.agp.window.dialog.ToastDialog;
import ohos.ai.cv.common.*;
import ohos.ai.cv.text.ITextDetector;
import ohos.ai.cv.text.Text;
import ohos.ai.cv.text.TextConfiguration;
import ohos.ai.cv.text.TextDetectType;
import ohos.app.Context;
import ohos.bundle.IBundleManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
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
    PixelMap pixelMap;
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
    }


    private void initData() {
        tf_recognize_result = (TextField) findComponentById(ResourceTable.Id_tf_recognize_result);
        bt_take_photo = (Button) findComponentById(ResourceTable.Id_bt_take_photo);
        bt_copy_result2 = (Button) findComponentById(ResourceTable.Id_bt_copy_result);
        bt_clear_input = (Button) findComponentById(ResourceTable.Id_clear_input);
        image_to_recognize = (Image) findComponentById(ResourceTable.Id_image_to_recognize);
        // 建立与能力引擎的连接
        int init = VisionManager.init(context, connectionCallback);
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
                requestPermission();
            }
        });

        bt_copy_result2.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                SystemPasteboard pasteboard = SystemPasteboard.getSystemPasteboard(getContext());
                if (pasteboard != null) {
                    pasteboard.setPasteData(PasteData.creatPlainTextData(tf_recognize_result.getText().trim()));
                    new ToastDialog(getContext()).setText("已成功复制到剪切板").show();
                }
            }
        });
    }

    int MY_PERMISSIONS_REQUEST_CAMERA = 4;

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
            toCameraAbility();
//            PermissionBridge.getHandler().sendEvent(EVENT_PERMISSION_GRANTED);
            return;
        }
        requestPermissionsFromUser(permissionFiltereds.toArray(new String[permissionFiltereds.size()]),
                MY_PERMISSIONS_REQUEST_CAMERA);
    }


    private void toCameraAbility() {
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

/*
    private void toPickPictureAbility(){

    }
*/

    ConnectionCallback connectionCallback = getConnectionCallback();

    private ConnectionCallback getConnectionCallback( ) {
        return new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                // 实例化ITextDetector接口
                textDetector = VisionManager.getTextDetector(context);
                // 实例化Text对象text
                Text text = new Text();
                // 实例化VisionImage对象image，并传入待检测图片pixelMap
                VisionImage image = VisionImage.fromPixelMap(image_to_recognize.getPixelMap());

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
