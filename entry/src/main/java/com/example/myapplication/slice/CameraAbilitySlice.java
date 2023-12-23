package com.example.myapplication.slice;

import com.example.myapplication.OCRAbility;
import com.example.myapplication.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.surfaceprovider.SurfaceProvider;
import ohos.agp.graphics.Surface;
import ohos.agp.graphics.SurfaceOps;
import ohos.agp.window.dialog.ToastDialog;
import ohos.app.Context;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.camera.CameraKit;
import ohos.media.camera.device.*;
import ohos.media.image.Image;
import ohos.media.image.ImageReceiver;
import ohos.media.image.common.ImageFormat;
import ohos.media.image.common.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static ohos.media.camera.device.Camera.FrameConfigType.FRAME_CONFIG_PICTURE;

public class CameraAbilitySlice extends AbilitySlice {
    static final HiLogLabel TAG = new HiLogLabel(HiLog.LOG_APP, 0x5555, CameraAbilitySlice.class.getName());
    private static final int SCREEN_WIDTH = 1080;
    private static final int SCREEN_HEIGHT = 1920;
    private static final int SLEEP_TIME = 200;
    private EventHandler creamEventHandler;
    private Button exitImage;
    private SurfaceProvider surfaceProvider;
    private Button switchCameraImage;
    private boolean isCameraRear = false;
    private Camera cameraDevice;
    private Surface previewSurface;
    private Button takePhotoImage;
    private CameraKit cameraKit;
    private String cameraId;
    // 图像帧数据接收处理对象
    private ImageReceiver imageReceiver;
    // 执行回调的EventHandler
    private EventHandler eventHandler = new EventHandler(EventRunner.create("CameraCb"));
    // 拍照支持分辨率
    private Size pictureSize;
    private ImageSaver imageSaver;
    private File dir = getExternalFilesDir(null);
    private boolean couldCreateFile = true;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_open_camera);
        getWindow().setTransparent(true);
        cameraKit = CameraKit.getInstance(getApplicationContext());
        initSurface();
        initComponent();
    }

    private void initSurface() {
        surfaceProvider = new SurfaceProvider(this);
        DirectionalLayout.LayoutConfig params = new DirectionalLayout.LayoutConfig(
                ComponentContainer.LayoutConfig.MATCH_PARENT, ComponentContainer.LayoutConfig.MATCH_PARENT);
        surfaceProvider.setLayoutConfig(params);
        surfaceProvider.pinToZTop(false);
        // 添加SurfaceCallBack回调
        surfaceProvider.getSurfaceOps().get().addCallback(new SurfaceCallBack());

        // 将SurfaceProvider加入到布局中
        Component component = findComponentById(ResourceTable.Id_surface_container);
        if (component instanceof ComponentContainer) {
            ((ComponentContainer) component).addComponent(surfaceProvider);
        }
    }

    //    初始化底部按钮以及绑定点击事件监听器
    private void initComponent() {
//         退出拍照页面图标
        Component exitImageCom = findComponentById(ResourceTable.Id_exit_image);
        if (exitImageCom instanceof Button) {
            exitImage = (Button) exitImageCom;
            exitImage.setClickedListener(component -> getAbility().terminateAbility());
        }
        // 切换前后置摄像头图标
        Component switchCameraImageCom = findComponentById(ResourceTable.Id_switch_camera_image);
        if (switchCameraImageCom instanceof Button) {
            switchCameraImage = (Button) switchCameraImageCom;
            switchCameraImage.setClickedListener(component -> switchClicked());
        }

        Component takePhotoCom = findComponentById(ResourceTable.Id_take_photo_image);
        if (takePhotoCom instanceof Button) {
            takePhotoImage = (Button) takePhotoCom;
            takePhotoImage.setClickedListener(new Component.ClickedListener() {
                @Override
                public void onClick(Component component) {
                    takeSingleCapture();
                }
            });
        }

    }

    private void switchClicked() {
        isCameraRear = !isCameraRear;
        openCamera();
    }

    private void takeSingleCapture() {

        if (cameraDevice == null || imageReceiver == null) {
            return;
        }
        // 获取拍照配置模板
        FrameConfig.Builder framePictureConfigBuilder = cameraDevice.getFrameConfigBuilder(FRAME_CONFIG_PICTURE);
        // 配置拍照Surface
        framePictureConfigBuilder.addSurface(imageReceiver.getRecevingSurface());
        // 配置拍照其他参数
        framePictureConfigBuilder.setImageRotation(90);
        try {
            // 启动单帧捕获(拍照)
            cameraDevice.triggerSingleCapture(framePictureConfigBuilder.build());
            new ToastDialog(getContext()).setText("启动单帧捕获").show();

        } catch (IllegalArgumentException e) {
            new ToastDialog(getContext()).setText("Argument Exception").show();
            HiLog.error(TAG, "Argument Exception");
        } catch (IllegalStateException e) {
            new ToastDialog(getContext()).setText("State Exception").show();
            HiLog.error(TAG, "State Exception");
        }

    }

    // 单帧捕获生成图像回调Listener
    private final ImageReceiver.IImageArrivalListener imageArrivalListener = new ImageReceiver.IImageArrivalListener() {
        @Override
        public void onImageArrival(ImageReceiver imageReceiver) {
            StringBuffer fileName = new StringBuffer("picture_");
            fileName.append(UUID.randomUUID()).append(".jpg"); // 定义生成图片文件名
            File myFile = new File(getExternalFilesDir(null), fileName.toString()); // 创建图片文件
            Image nextImage = imageReceiver.readNextImage();

            imageSaver = new ImageSaver(nextImage, myFile); // 创建一个读写线程任务用于保存图片
            eventHandler.postTask(imageSaver); // 执行读写线程任务生成图片
        }
    };

    // 保存图片, 图片数据读写，及图像生成见run方法
    class ImageSaver implements Runnable {
        private final ohos.media.image.Image myImage;
        private final File myFile;

        ImageSaver(Image image, File file) {
            myImage = image;
            myFile = file;
        }

        @Override
        public void run() {
            ohos.media.image.Image.Component component = myImage.getComponent(ImageFormat.ComponentType.JPEG);
            byte[] bytes = new byte[component.remaining()];
            component.read(bytes);
            FileOutputStream output = null;
            Intent intent = new Intent();
            String fileName = myFile.getPath();
            intent.setParam("photo", fileName);

            try {
                output = new FileOutputStream(myFile);
                output.write(bytes); // 写图像数据
                output.flush();

            } catch (IOException e) {
                HiLog.error(TAG, "save picture occur exception!");
            } finally {
                if (output != null) {
                    try {
                        output.close(); // 关闭流
                    } catch (IOException e) {
                        HiLog.error(TAG, "image release occur exception!");
                    }
                }
                myImage.release();
            }
            String msg = "Take photo succeed, path=" + myFile.getPath();
            showTips(getContext(), msg);
            Operation operation_ocr = new Intent.OperationBuilder()
                    .withDeviceId("")
                    .withBundleName(getBundleName())
                    .withAbilityName(OCRAbility.class.getName())
                    .build();

            intent.setOperation(operation_ocr);
            startAbility(intent);
        }
    }

    private void showTips(Context context, String msg) {
        getUITaskDispatcher().asyncDispatch(() -> new ToastDialog(context).setText(msg).show());
    }

    private void takePictureInit() {
        List<Size> pictureSizes = cameraKit.getCameraAbility(cameraId).getSupportedSizes(ImageFormat.JPEG); // 获取拍照支持分辨率列表
        pictureSize = getPictureSize(pictureSizes);// 根据拍照要求选择合适的分辨率
        imageReceiver = ImageReceiver.create(Math.max(pictureSize.width, pictureSize.height),
                Math.min(pictureSize.width, pictureSize.height), ImageFormat.JPEG, 5); // 创建ImageReceiver对象，注意create函数中宽度要大于高度；5为最大支持的图像数，请根据实际设置。
        imageReceiver.setImageArrivalListener(imageArrivalListener);
    }

    private Size getPictureSize(List<Size> pictureSizes) {
        for (Size size : pictureSizes) {
            HiLog.info(TAG, size.toString());
        }
        return pictureSizes.get(0);
    }

    private void openCamera() {

        String[] cameraLists = cameraKit.getCameraIds();
        if (cameraLists.length > 1 && isCameraRear) {
            cameraId = cameraLists[0];
        } else {
            cameraId = cameraLists[1];
        }
        CameraStateCallback cameraStateCallback = new CameraStateCallbackImpl();
        creamEventHandler = new EventHandler(EventRunner.create("======CameraBackground"));
        cameraKit.createCamera(cameraId, cameraStateCallback, creamEventHandler);
        takePictureInit();
    }

    private FrameStateCallback frameStateCallbackImpl = new FrameStateCallback() {
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

    /**
     * CameraStateCallbackImpl
     */
    class CameraStateCallbackImpl extends CameraStateCallback {
        CameraStateCallbackImpl() {
        }

        @Override
        public void onCreated(Camera camera) {
            // 获取预览
            previewSurface = surfaceProvider.getSurfaceOps().get().getSurface();
            if (previewSurface == null) {
                HiLog.error(TAG, "create camera filed, preview surface is null");
                return;
            }
            // Wait until the preview surface is created.
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException exception) {
                HiLog.warn(TAG, "Waiting to be interrupted");
            }
            CameraConfig.Builder cameraConfigBuilder = camera.getCameraConfigBuilder();
            // 配置预览
            cameraConfigBuilder.addSurface(previewSurface);
            // 配置拍照的Surface
            cameraConfigBuilder.addSurface(imageReceiver.getRecevingSurface());
            // 配置帧结果的回调
            cameraConfigBuilder.setFrameStateCallback(frameStateCallbackImpl, eventHandler);


            try {
                // 相机设备配置
                camera.configure(cameraConfigBuilder.build());
            } catch (IllegalArgumentException e) {
                HiLog.error(TAG, "Argument Exception");
            } catch (IllegalStateException e) {
                HiLog.error(TAG, "State Exception");
            }

            cameraDevice = camera;
            enableImageGroup();
        }

        @Override
        public void onConfigured(Camera camera) {
            FrameConfig.Builder framePreviewConfigBuilder
                    = camera.getFrameConfigBuilder(Camera.FrameConfigType.FRAME_CONFIG_PREVIEW);
            framePreviewConfigBuilder.addSurface(previewSurface);
            // 开启循环捕捉
            camera.triggerLoopingCapture(framePreviewConfigBuilder.build());

        }

        private void enableImageGroup() {
            if (!exitImage.isEnabled()) {
                exitImage.setEnabled(true);
                switchCameraImage.setEnabled(true);
            }
        }
    }

    /**
     * SurfaceCallBack
     */
    class SurfaceCallBack implements SurfaceOps.Callback {
        @Override
        public void surfaceCreated(SurfaceOps callbackSurfaceOps) {
            if (callbackSurfaceOps != null) {
                callbackSurfaceOps.setFixedSize(SCREEN_HEIGHT, SCREEN_WIDTH);
            }
            openCamera();
        }

        @Override
        public void surfaceChanged(SurfaceOps callbackSurfaceOps, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceOps callbackSurfaceOps) {
        }
    }

    @Override
    public void onStop() {
        cameraDevice.release();
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



