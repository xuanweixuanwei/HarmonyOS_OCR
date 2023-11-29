package com.example.myapplication.slice;

import com.example.myapplication.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.Image;
import ohos.agp.components.TextField;
import ohos.agp.utils.TextTool;
import ohos.agp.window.dialog.ToastDialog;

import ohos.ai.cv.common.ConnectionCallback;
import ohos.ai.cv.common.VisionCallback;
import ohos.ai.cv.common.VisionConfiguration;
import ohos.ai.cv.common.VisionImage;
import ohos.ai.cv.common.VisionManager;
import ohos.ai.cv.text.ITextDetector;
import ohos.ai.cv.text.Text;
import ohos.ai.cv.text.TextConfiguration;
import ohos.ai.cv.text.TextDetectType;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;

import java.util.logging.Logger;

public class MainAbilitySlice extends AbilitySlice {
    static final HiLogLabel LABEL = new HiLogLabel(HiLog.LOG_APP, 0x5555, "HHHHHHHHHHHHHHHHHHHH");
    TextField tf_recognize_result;
    Button bt_take_photo ;
    Button bt_pick_picture;
    Button bt_copy_result2 ;
    Button bt_clear_input ;
    Image image_to_recognize;
    ITextDetector textDetector;
    VisionCallback<Text> visionCallback;

    Text text ;

    Byte[] pictureByteArray;

    VisionImage visionImage ;
    ConnectionCallback connectionCallback ;
    Context context;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        context = this;
        initData();
        addListener();

//        for (int i = 0; i < 10000; i++) {
//            HiLog.info(LABEL, "Hey! You have successfully printed a log.");
//        }
//        ocrServiceConnect();
    }

    private void ocrServiceConnect() {
//   定义ConnectionCallback回调，实现连接能力引擎成功与否后的操作
       connectionCallback = new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
//                 定义连接能力引擎成功后的操作。
//                在收到onServiceConnect回调连接服务成功后，实例化ITextDetector接口，将此工程的context作为入参
                 textDetector = VisionManager.getTextDetector(context);
                 visionImage = VisionImage.fromPixelMap(image_to_recognize.getPixelMap());
                text = new Text();
                visionCallback= new VisionCallback<Text>() {
                    @Override
                    public void onResult(Text text) {
                        tf_recognize_result.setText("已获得识别结果");
                        // 对正确获得文字识别结果进行处理。
                    }

                    @Override
                    public void onError(int i) {
                        // 处理错误返回码。
                        tf_recognize_result.setText("识别失败，错误码"+i);

                    }

                    @Override
                    public void onProcessing(float v) {
                        // 返回处理进度。
//                        onProcessing()方法用于返回处理进度，目前没有实现此接口的功能。
                        tf_recognize_result.setText("识别中");
                    }
                };

                TextConfiguration.Builder builder = new TextConfiguration.Builder();
                builder.setProcessMode(VisionConfiguration.MODE_IN);
                builder.setDetectType(TextDetectType.TYPE_TEXT_DETECT_FOCUS_SHOOT);
                builder.setLanguage(TextConfiguration.ENGLISH);
                TextConfiguration config = builder.build();
                textDetector.setVisionConfiguration(config);
//                在detect()方法中会首先调用prepare()启动引擎，如果引擎已经启动则不会再次启动
                if(textDetector.prepare()!=0){
                    new ToastDialog(context).setText("OCR能力准备失败，需要处理错误");
                }
                visionCallback= new VisionCallback<Text>() {
                    @Override
                    public void onResult(Text text) {
                        tf_recognize_result.setText("已获得识别结果");
                        // 对正确获得文字识别结果进行处理。
                    }

                    @Override
                    public void onError(int i) {
                        // 处理错误返回码。
                        tf_recognize_result.setText("识别失败，错误码"+i);

                    }

                    @Override
                    public void onProcessing(float v) {
                        // 返回处理进度。
//                        onProcessing()方法用于返回处理进度，目前没有实现此接口的功能。
                        tf_recognize_result.setText("识别中");
                    }
                };

               int result = textDetector.detect(visionImage, text, visionCallback); // 异步
            }

            @Override
            public void onServiceDisconnect() {
                // 定义连接能力引擎失败后的操作。
                new ToastDialog(context).setText("链接OCR能力引擎失败");
            }
        };
       HiLog.warn(LABEL,"定义ConnectionCallback回调");
    /*
    调用VisionManager.init()方法，
    将此工程的context和已经定义的connectionCallback回调作为入参，
    建立与能力引擎的连接。
    context应为ohos.aafwk.ability.Ability或ohos.aafwk.ability.AbilitySlice的实例或子类实例
    */
        int result = VisionManager.init(context, connectionCallback);
         textDetector = VisionManager.getTextDetector(context);
        visionImage = VisionImage.fromPixelMap(image_to_recognize.getPixelMap());
        text = new Text();
        TextConfiguration.Builder builder = new TextConfiguration.Builder();
        builder.setProcessMode(VisionConfiguration.MODE_IN);
        builder.setDetectType(TextDetectType.TYPE_TEXT_DETECT_FOCUS_SHOOT);
        builder.setLanguage(TextConfiguration.ENGLISH);
        TextConfiguration config = builder.build();
        textDetector.setVisionConfiguration(config);
        int number=textDetector.prepare();
        if(number!=0){
            new ToastDialog(context).setText("OCR能力准备失败，需要处理错误");
        }

        result = textDetector.detect(visionImage, null, visionCallback); // 异步
        HiLog.warn(LABEL,"调用VisionManager.init()方法,result = %{result}d",result);


    }


    private void initData() {
         tf_recognize_result = (TextField) findComponentById(ResourceTable.Id_tf_recognize_result);
         bt_take_photo = (Button) findComponentById(ResourceTable.Id_bt_take_photo);
         bt_pick_picture = (Button) findComponentById(ResourceTable.Id_bt_pick_picture);
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
            ocrServiceConnect();
            }
        });

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
