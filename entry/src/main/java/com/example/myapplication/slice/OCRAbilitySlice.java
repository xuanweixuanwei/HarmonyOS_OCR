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
import ohos.media.photokit.metadata.AVStorage;
import ohos.miscservices.pasteboard.PasteData;
import ohos.miscservices.pasteboard.SystemPasteboard;
import ohos.security.SystemPermission;

import java.io.FileDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.utils.net.Uri;


public class OCRAbilitySlice extends AbilitySlice {
    static final HiLogLabel LABEL = new HiLogLabel(HiLog.LOG_APP, 0x5555, "HHHHHHHHHHHHHHHHHHHH");
//    识别结果的文本框
    TextField tf_recognize_result;
//  通过拍照获取图片进行识别
    Button bt_take_photo ;
//  复制识别结果按钮
    Button bt_copy_result;
//   清空识别结果按钮
    Button bt_clear_input ;
//    Image,OCR识别目标图片
    Image image_to_recognize;
//   通过从相册选择图片的按钮
    Button bt_pick_picture;
    ITextDetector textDetector;
    PixelMap pixelMap;
    Context context;
    private int imgRequestCode=111;

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

            dataPathRecognize(photos);

        }
    }


    private void initData() {
        tf_recognize_result = (TextField) findComponentById(ResourceTable.Id_tf_recognize_result);
        bt_take_photo = (Button) findComponentById(ResourceTable.Id_bt_take_photo);
        bt_copy_result = (Button) findComponentById(ResourceTable.Id_bt_copy_result);
        bt_clear_input = (Button) findComponentById(ResourceTable.Id_clear_input);
        image_to_recognize = (Image) findComponentById(ResourceTable.Id_image_to_recognize);
        bt_pick_picture = (Button) findComponentById(ResourceTable.Id_bt_pick_picture);
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
                requestPermissionForTF();
            }
        });

        bt_copy_result.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                SystemPasteboard pasteboard = SystemPasteboard.getSystemPasteboard(getContext());
                if (pasteboard != null) {
                    pasteboard.setPasteData(PasteData.creatPlainTextData(tf_recognize_result.getText().trim()));
                    new ToastDialog(getContext()).setText("已成功复制到剪切板").show();
                }
            }
        });

        bt_pick_picture.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                toPickPictureAbility();
            }
        });
    }

    int MY_PERMISSIONS_REQUEST_CAMERA = 4;
//  获取拍照识别的权限，若已经获取直接跳转CameraAbility
    private void requestPermissionForTF() {
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
    //  获取选择系统相册中图片的权限，若已经获取直接跳转CameraAbility
    private void requestPermissionForPP() {
        String[] permissions = {
                // 存储权限
                SystemPermission.READ_USER_STORAGE,
        };

        List<String> permissionFiltereds = Arrays.stream(permissions)
                .filter(permission -> verifySelfPermission(permission) != IBundleManager.PERMISSION_GRANTED)
                .collect(Collectors.toList());

        if (permissionFiltereds.isEmpty()) {
            toPickPictureAbility();

            return;
        }

        //获取存储权限
        requestPermissionsFromUser(permissions,imgRequestCode);
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

    private void toPickPictureAbility(){
        Intent intent = new Intent();
        Operation opt=new Intent.OperationBuilder().withAction("android.intent.action.GET_CONTENT").build();
        intent.setOperation(opt);
        intent.addFlags(Intent.FLAG_NOT_OHOS_COMPONENT);
        intent.setType("image/*");
        startAbilityForResult(intent, imgRequestCode);
    }

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



    private void dataPathRecognize(String path){
            ImageSource.SourceOptions sourceOptions = new ImageSource.SourceOptions();
            sourceOptions.formatHint= "image/jpg";
            ImageSource imageSource = ImageSource.create(path,sourceOptions);
            PixelMap pixelmap = imageSource.createPixelmap(null);
            image_to_recognize.setPixelMap(pixelmap);
            wordRecognize();
    }

    @Override
    protected void onAbilityResult(int requestCode, int resultCode, Intent resultData) {
        if(requestCode==imgRequestCode)
        {  if (resultData==null) {
            return;
        }
            HiLog.info(LABEL,"选择图片getUriString:"+resultData.getUriString());
            //选择的Img对应的Uri
            String chooseImgUri=resultData.getUriString();
//            new ToastDialog(getContext()).setText(chooseImgUri.substring(chooseImgUri.lastIndexOf('/'))).show();
//            tf_recognize_result.setText(chooseImgUri);
            HiLog.info(LABEL,"选择图片getScheme:"+chooseImgUri.substring(chooseImgUri.lastIndexOf('/')));

            //定义数据能力帮助对象
            DataAbilityHelper helper=DataAbilityHelper.creator(getContext());
            //定义图片来源对象
            ImageSource imageSource = null;
            //获取选择的Img对应的Id
            String chooseImgId=null;
            //如果是选择文件则getUriString结果为content://com.android.providers.media.documents/document/image%3A30，其中%3A是":"的URL编码结果，后面的数字就是image对应的Id
            //如果选择的是图库则getUriString结果为content://media/external/images/media/30，最后就是image对应的Id
            //这里需要判断是选择了文件还是图库
            if(chooseImgUri.lastIndexOf("%3A")!=-1){
                chooseImgId = chooseImgUri.substring(chooseImgUri.lastIndexOf("%3A")+3);
            }
            else if(chooseImgUri.contains("//media/external/images/media/")){
                chooseImgId = chooseImgUri.substring(chooseImgUri.lastIndexOf('/')+1);
            }else if(chooseImgUri.contains("emulated")){
                chooseImgId = chooseImgUri.substring(chooseImgUri.indexOf("/root/")+5);
//                tf_recognize_result.setText(chooseImgId);
                dataPathRecognize(chooseImgId);
                return;
            }
            //获取图片对应的uri，由于获取到的前缀是content，我们替换成对应的dataability前缀
            Uri uri=Uri.appendEncodedPathToUri(AVStorage.Images.Media.EXTERNAL_DATA_ABILITY_URI,chooseImgId);
//            tf_recognize_result.setText(tf_recognize_result.getText()+"      999      "+uri);
            HiLog.info(LABEL,"选择图片dataability路径:"+uri.toString());
            try {
                //读取图片
                FileDescriptor fd = helper.openFile(uri, "r");
                imageSource = ImageSource.create(fd, null);
                //创建位图
                PixelMap pixelMap = imageSource.createPixelmap(null);
                //设置图片控件对应的位图
                image_to_recognize.setPixelMap(pixelMap);

                wordRecognize();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (imageSource != null) {
                    imageSource.release();
                }
            }
        }
    }

    private void wordRecognize(){
        int init = VisionManager.init(context, connectionCallback);
//        // 实例化VisionImage对象image，并传入待检测图片pixelMap
//        VisionImage image = VisionImage.fromPixelMap(pixelMap);
//        Text text = new Text();
//        int prepare = textDetector.prepare();
//
//        int result2 = textDetector.detect(image, text, null); // 同步
//
//        sendResult(text.getValue()+"\n"+"END"+"    "+prepare);
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
