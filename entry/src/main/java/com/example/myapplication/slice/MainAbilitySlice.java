package com.example.myapplication.slice;

import com.example.myapplication.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.Image;
import ohos.agp.components.TextField;
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
import ohos.media.image.PixelMap;

public class MainAbilitySlice extends AbilitySlice {
    TextField tf_recognize_result;
    Button bt_take_photo ;
    Button bt_pick_picture;
    Button bt_copy_result2 ;
    Button bt_clear_input ;
    Image image_to_recognize;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        initComponent();
        addListener();
    }



    private void initComponent() {
         tf_recognize_result = (TextField) findComponentById(ResourceTable.Id_tf_recognize_result);
         bt_take_photo = (Button) findComponentById(ResourceTable.Id_bt_take_photo);
         bt_pick_picture = (Button) findComponentById(ResourceTable.Id_bt_pick_picture);
         bt_copy_result2 = (Button) findComponentById(ResourceTable.Id_bt_copy_result);
         bt_clear_input = (Button) findComponentById(ResourceTable.Id_clear_input);
         image_to_recognize = (Image) findComponentById(ResourceTable.Id_image_to_recognize);
    }
    private void addListener() {
        bt_clear_input.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                new ToastDialog(getContext()).setText("清除文本框内容").show();
                tf_recognize_result.setText("");
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
