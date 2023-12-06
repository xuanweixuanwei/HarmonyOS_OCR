package com.example.lab2.slice;

import com.example.lab2.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.Text;
import ohos.agp.components.TextField;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.utils.Color;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.app.dispatcher.task.Revocable;

import java.util.Timer;
import java.util.TimerTask;

public class MainAbilitySlice extends AbilitySlice {

    private ShapeElement errorElement;
    private ShapeElement successElement;
    private TextField tf_username, tf_password;
    private Button bt_submit;
    private Text text_result;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        errorElement = new ShapeElement(getContext(), ResourceTable.Graphic_background_text_field_error);
        successElement = new ShapeElement(getContext(), ResourceTable.Graphic_rounded_rectangle);
        initComponents();
    }


    private void initComponents() {

        Component usernameTFCom = findComponentById(ResourceTable.Id_tf_username);
        if (usernameTFCom instanceof TextField) {
            tf_username = (TextField) usernameTFCom;
        }

        Component passwordTFCom = findComponentById(ResourceTable.Id_tf_password);
        if (passwordTFCom instanceof TextField) {
            tf_password = (TextField) passwordTFCom;
        }

        Component textCom = findComponentById(ResourceTable.Id_text_result);
        if (textCom instanceof Text) {
            text_result = (Text) textCom;
        }

        Component submitBTCom = findComponentById(ResourceTable.Id_bt_submit);
        if (submitBTCom instanceof Button) {
            bt_submit = (Button) submitBTCom;
            bt_submit.setClickedListener(new Component.ClickedListener() {
                @Override
                public void onClick(Component component) {
                    if ("13rg".equals(tf_username.getText().trim()) && "1234".equals(tf_password.getText().trim())) {
                        showSuccess();
                    } else {
                        showError();
                    }
                }
            });
        }

    }

    private void showSuccess() {
        TaskDispatcher dispatcher = getUITaskDispatcher();
        Revocable revocable = dispatcher.delayDispatch(new Runnable() {
            @Override
            public void run() {
                text_result.setText(ResourceTable.String_text_right_result);
                text_result.setTextColor(Color.GREEN);
                text_result.setVisibility(Component.VISIBLE);
            }
        }, 10);
    }


    private void showError() {
        TaskDispatcher dispatcher = getUITaskDispatcher();
        Revocable revocable = dispatcher.delayDispatch(new Runnable() {
            @Override
            public void run() {
                // 显示错误提示的Text
                text_result.setText(ResourceTable.String_text_wrong_result);
                text_result.setTextColor(Color.RED);
                text_result.setVisibility(Component.VISIBLE);

                // 显示TextField错误状态下的样式

                tf_password.setBackground(errorElement);
                tf_username.setBackground(errorElement);
                // TextField失去焦点
                tf_username.clearFocus();
                tf_password.clearFocus();

                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        clearErrorMessage();
                    }
                };
                timer.schedule(timerTask, 6000);
            }
        }, 10);

    }

    private void clearErrorMessage() {
        TaskDispatcher dispatcher = getUITaskDispatcher();
        Revocable revocable = dispatcher.delayDispatch(new Runnable() {
            @Override
            public void run() {
                text_result.setVisibility(Component.HIDE);

                // 显示TextField正常等待输入状态下的样式
                tf_password.setBackground(successElement);
                tf_username.setBackground(successElement);

            }
        }, 10);


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
