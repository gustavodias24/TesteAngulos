package benicio.soluces.clinometroclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/* loaded from: classes.dex */
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int LIMITE_LEITURA = 6;
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private static final String SHARED_PREFERENCES = "settings";
    private Sensor accelerometer;
    private Animation animation;
    private ImageButton btnInsertHorizontalDistance;
    private ImageButton btnMeasureAngleSensor;
    private ImageButton btnRestartMeasure;
    private ImageButton btnSettingsPreferences;
    private float calculateHeight;
    private float currentAngleSensor;
    private float lastAccelX;
    private float lastAccelY;
    private float lastAccelZ;
    private LinearLayout layoutMeasureAngle;
    private LinearLayout layoutMessageEmail;
    private LinearLayout layoutMessagePhone;
    private LinearLayout layoutMessageWhatsApp;
    private LinearLayout layoutRestartMeasure;
    private LinearLayout layoutSettingsPreferences;
//    private AdView mAdView;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundHandlerThread;
    private CameraDevice mCameraDevice;
    private String mCameraId;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private Size mPreviewSize;
    private TextureView mTextureView;
    private int messageStepInfoUser;
    private SensorManager sensorManager;
    private int statusMeasureStep;
    private TextView textViewBaseAngle;
    private TextView textViewHorizontalDistance;
    private TextView textViewInformationStep;
    private TextView textViewMeasureHeight;
    private TextView textViewTopAngle;
    private float valueAB;
    private float valueAT;
    private float valueHD;
    private float valueHT;
    private float valueTA;
    private float valueTB;
    private long lastUpdate = 0;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() { // from class: br.eng.itech.clinometro.MainActivity.1
        @Override // android.view.TextureView.SurfaceTextureListener
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override // android.view.TextureView.SurfaceTextureListener
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
        }

        @Override // android.view.TextureView.SurfaceTextureListener
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

        @Override // android.view.TextureView.SurfaceTextureListener
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
            MainActivity.this.setupCamera(i, i2);
            MainActivity.this.transformImage(i, i2);
            MainActivity.this.connectCamera();
        }
    };
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() { // from class: br.eng.itech.clinometro.MainActivity.2
        @Override // android.hardware.camera2.CameraDevice.StateCallback
        public void onOpened(CameraDevice cameraDevice) {
            MainActivity.this.mCameraDevice = cameraDevice;
            MainActivity.this.startPreview();
        }

        @Override // android.hardware.camera2.CameraDevice.StateCallback
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            MainActivity.this.mCameraDevice = null;
        }

        @Override // android.hardware.camera2.CameraDevice.StateCallback
        public void onError(CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            MainActivity.this.mCameraDevice = null;
        }
    };

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    static {
        ORIENTATIONS.append(0, 0);
        ORIENTATIONS.append(1, 90);
        ORIENTATIONS.append(2, 180);
        ORIENTATIONS.append(3, 270);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class CompareSizeByArea implements Comparator<Size> {
        private CompareSizeByArea() {
        }

        @Override // java.util.Comparator
        public int compare(Size size, Size size2) {
            return Long.signum(((size.getWidth() * size.getHeight()) / size2.getWidth()) * size2.getHeight());
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        getSupportActionBar().hide();
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_main);
//        MobileAds.initialize(this, "ca-app-pub-5459341231308421~3755171741");
//        this.mAdView = (AdView) findViewById(R.id.adView);
//        this.mAdView.loadAd(new AdRequest.Builder().build());
        this.mTextureView = (TextureView) findViewById(R.id.textureView);
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.textViewHorizontalDistance = (TextView) findViewById(R.id.textViewHorizontalDistance);
        this.textViewBaseAngle = (TextView) findViewById(R.id.textViewBaseAngle);
        this.textViewTopAngle = (TextView) findViewById(R.id.textViewTopAngle);
        this.textViewMeasureHeight = (TextView) findViewById(R.id.textViewMeasureHeight);
        this.textViewInformationStep = (TextView) findViewById(R.id.textViewInformationStep);
        this.layoutMeasureAngle = (LinearLayout) findViewById(R.id.layoutMeasureAngle);
        this.layoutRestartMeasure = (LinearLayout) findViewById(R.id.layoutRestartMeasure);
        this.layoutSettingsPreferences = (LinearLayout) findViewById(R.id.layoutSettingsPreferences);
        this.btnInsertHorizontalDistance = (ImageButton) findViewById(R.id.btnInsertHorizontalDistance);
        this.btnMeasureAngleSensor = (ImageButton) findViewById(R.id.btnMeasureAngleSensor);
        this.btnRestartMeasure = (ImageButton) findViewById(R.id.btnRestartMeasure);
        this.btnSettingsPreferences = (ImageButton) findViewById(R.id.btnSettingsPreferences);
        loadSharedPreferencesSettings();
        managerInitialize(1);
        this.btnInsertHorizontalDistance.setOnClickListener(new View.OnClickListener() { // from class: br.eng.itech.clinometro.MainActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.showDialogDistanceHorizontal();
            }
        });
        this.btnMeasureAngleSensor.setOnClickListener(new View.OnClickListener() { // from class: br.eng.itech.clinometro.MainActivity.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (MainActivity.this.getStatusMeasureStep() == 1) {
                    MainActivity mainActivity = MainActivity.this;
                    mainActivity.setValueAB(mainActivity.getCurrentAngleSensor());
                    MainActivity.this.textViewBaseAngle.setText(String.format("%.2f°", Float.valueOf(MainActivity.this.getValueAB())));
                    MainActivity.this.textViewInformationStep.setText("Posicione a mira no TOPO DA ÁRVORE. Em seguida clique no botão \"DISPARAR\" para coletar o segundo ângulo.");
                    MainActivity.this.blinkTextViewInformationStep();
                    MainActivity.this.setStatusMeasureStep(2);
                } else if (MainActivity.this.getStatusMeasureStep() == 2) {
                    MainActivity mainActivity2 = MainActivity.this;
                    mainActivity2.setValueAT(mainActivity2.getCurrentAngleSensor());
                    MainActivity.this.textViewTopAngle.setText(String.format("%.2f°", Float.valueOf(MainActivity.this.getValueAT())));
                    MainActivity.this.calculateMeasureHeight();
                    MainActivity.this.managerInitialize(2);
                }
            }
        });
        this.btnRestartMeasure.setOnClickListener(new View.OnClickListener() { // from class: br.eng.itech.clinometro.MainActivity.5
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.managerInitialize(3);
            }
        });
        this.btnSettingsPreferences.setOnClickListener(new View.OnClickListener() { // from class: br.eng.itech.clinometro.MainActivity.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.showDialogSettingsPreferences();
            }
        });
//        showDialogReleaseApplication();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void loadSharedPreferencesSettings() {
        setMessageStepInfoUser(getSharedPreferences(SHARED_PREFERENCES, 0).getInt("messageStepInfoUser", 1));
        if (getMessageStepInfoUser() == 1) {
//            showHideView(false, this.textViewInformationStep);
        }
        if (getMessageStepInfoUser() == 0) {
//            showHideView(true, this.textViewInformationStep);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void calculateMeasureHeight() {
        setValueTA(0.0f);
        setValueTB(0.0f);
        setValueTA((float) Math.tan(Math.toRadians(getValueAB())));
        setValueTB((float) Math.tan(Math.toRadians(getValueAT())));
        if ((getValueTA() > 0.0f && getValueTB() > 0.0f) || (getValueTA() < 0.0f && getValueTB() < 0.0f)) {
            if (getValueTA() < 0.0f) {
                setValueTA(getValueTA() * (-1.0f));
            }
            if (getValueTB() < 0.0f) {
                setValueTB(getValueTB() * (-1.0f));
            }
            setCalculateHeight(getValueHD() * (getValueTA() - getValueTB()));
            if (getCalculateHeight() < 0.0f) {
                setCalculateHeight(getCalculateHeight() * (-1.0f));
            }
        } else {
            if (getValueTA() < 0.0f) {
                setValueTA(getValueTA() * (-1.0f));
            }
            if (getValueTB() < 0.0f) {
                setValueTB(getValueTB() * (-1.0f));
            }
            setCalculateHeight(getValueHD() * (getValueTA() + getValueTB()));
            if (getCalculateHeight() < 0.0f) {
                setCalculateHeight(getCalculateHeight() * (-1.0f));
            }
        }
        setValueHT(getCalculateHeight());
        this.textViewMeasureHeight.setText(String.format("%.2f m", Float.valueOf(getValueHT())));
    }

//    private void showDialogReleaseApplication() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View inflate = getLayoutInflater().inflate(R.layout.dialog_release, (ViewGroup) null);
//        Button button = (Button) inflate.findViewById(R.id.btn_yes);
//        Button button2 = (Button) inflate.findViewById(R.id.btn_not);
//        builder.setView(inflate);
//        final AlertDialog create = builder.create();
//        create.setCanceledOnTouchOutside(false);
//        button.setOnClickListener(new View.OnClickListener() { // from class: br.eng.itech.clinometro.MainActivity.7
//            @Override // android.view.View.OnClickListener
//            public void onClick(View view) {
//                MainActivity.this.openLinkAppPro("play.google.com/store/apps/details?id=br.eng.itech.clinometro_pro");
//                create.dismiss();
//            }
//        });
//        button2.setOnClickListener(new View.OnClickListener() { // from class: br.eng.itech.clinometro.MainActivity.8
//            @Override // android.view.View.OnClickListener
//            public void onClick(View view) {
//                create.dismiss();
//            }
//        });
//        create.show();
//    }

    public void openLinkAppPro(String str) {
        if (!str.startsWith("http://") && !str.startsWith("https://")) {
            str = "http://" + str;
        }
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(Uri.parse(str));
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showDialogDistanceHorizontal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View inflate = getLayoutInflater().inflate(R.layout.dialog_distance, (ViewGroup) null);
        final EditText editText = (EditText) inflate.findViewById(R.id.editTextHorizontalDistance);
        Button button = (Button) inflate.findViewById(R.id.btnConfirmHorizontalDistance);
        builder.setView(inflate);
        final AlertDialog create = builder.create();
        create.setCanceledOnTouchOutside(true);
        button.setOnClickListener(new View.OnClickListener() { // from class: br.eng.itech.clinometro.MainActivity.9
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (editText.getText().length() == 0) {
                    editText.setError("Valor Obrigatório!");
                    return;
                }
                MainActivity.this.setValueHD(Float.parseFloat(editText.getText().toString()));
                MainActivity.this.textViewHorizontalDistance.setText(String.valueOf(MainActivity.this.getValueHD()).replace(".", ",") + " m");
                MainActivity mainActivity = MainActivity.this;
//                mainActivity.showHideView(false, mainActivity.layoutMeasureAngle);
                MainActivity mainActivity2 = MainActivity.this;
//                mainActivity2.showHideView(true, mainActivity2.layoutSettingsPreferences);
                MainActivity.this.managerInitialize(3);
                create.dismiss();
            }
        });
        create.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showDialogSettingsPreferences() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View inflate = getLayoutInflater().inflate(R.layout.dialog_settings, (ViewGroup) null);
        Switch r2 = (Switch) inflate.findViewById(R.id.switchMessageInfo);
        builder.setView(inflate);
        AlertDialog create = builder.create();
        create.setCanceledOnTouchOutside(true);
        if (getMessageStepInfoUser() == 1) {
            r2.setChecked(true);
        } else {
            r2.setChecked(false);
        }
        r2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: br.eng.itech.clinometro.MainActivity.10
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                SharedPreferences.Editor edit = MainActivity.this.getSharedPreferences(MainActivity.SHARED_PREFERENCES, 0).edit();
                if (z) {
                    edit.putInt("messageStepInfoUser", 1);
                } else {
                    edit.putInt("messageStepInfoUser", 0);
                }
                edit.commit();
                MainActivity.this.loadSharedPreferencesSettings();
            }
        });
        this.layoutMessageEmail = (LinearLayout) inflate.findViewById(R.id.layoutMessageEmail);
        this.layoutMessageEmail.setOnClickListener(new View.OnClickListener() { // from class: br.eng.itech.clinometro.MainActivity.11
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.sendMail();
            }
        });
        this.layoutMessageWhatsApp = (LinearLayout) inflate.findViewById(R.id.layoutMessageWhatsApp);
        this.layoutMessageWhatsApp.setOnClickListener(new View.OnClickListener() { // from class: br.eng.itech.clinometro.MainActivity.12
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.sendWhatsApp();
            }
        });
        this.layoutMessagePhone = (LinearLayout) inflate.findViewById(R.id.layoutMessagePhone);
        this.layoutMessagePhone.setOnClickListener(new View.OnClickListener() { // from class: br.eng.itech.clinometro.MainActivity.13
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.sendPhone();
            }
        });
        create.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendPhone() {
        Intent intent = new Intent("android.intent.action.DIAL");
        intent.setData(Uri.parse("tel:+5511953860365"));
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendWhatsApp() {
        Intent intent = new Intent("android.intent.action.SENDTO", Uri.parse("smsto:+5511953860365"));
        intent.setPackage("com.whatsapp");
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendMail() {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.putExtra("android.intent.extra.EMAIL", new String[]{"itech@itech.eng.br"});
        intent.putExtra("android.intent.extra.SUBJECT", "E-mail enviado através do aplicativo clinômetro");
        intent.putExtra("android.intent.extra.TEXT", "Olá, fiquei interessado em saber mais sobre as possíveis funcionalidades deste aplicativo!");
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, "Enviar e-mail para o desenvolvedor"));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void managerInitialize(int i) {
        if (i == 1) {
            clearContentTextView(1);
//            showHideView(true, this.layoutMeasureAngle);
//            showHideView(true, this.layoutRestartMeasure);
//            showHideView(false, this.layoutSettingsPreferences);
            this.textViewInformationStep.setText("Para iniciar clique no botão \"DISTÂNCIA\". Em seguida informe a distância horizontal entre o dispositivo e a árvore.");
            blinkTextViewInformationStep();
        } else if (i == 2) {
//            showHideView(true, this.layoutSettingsPreferences);
//            showHideView(true, this.layoutMeasureAngle);
//            showHideView(false, this.layoutRestartMeasure);
            this.textViewInformationStep.setText("");
            setStatusMeasureStep(3);
        } else if (i != 3) {
        } else {
            clearContentTextView(2);
//            showHideView(false, this.layoutMeasureAngle);
//            showHideView(true, this.layoutRestartMeasure);
            this.textViewInformationStep.setText("Posicione a mira na BASE DA ÁRVORE. Em seguida clique no botão \"DISPARAR\" para coletar o primeiro ângulo.");
            blinkTextViewInformationStep();
            setStatusMeasureStep(1);
        }
    }

    private void clearContentTextView(int i) {
        if (i == 1) {
            this.textViewHorizontalDistance.setText("");
            this.textViewBaseAngle.setText("");
            this.textViewTopAngle.setText("");
            this.textViewMeasureHeight.setText("");
        } else if (i != 2) {
        } else {
            this.textViewBaseAngle.setText("");
            this.textViewTopAngle.setText("");
            this.textViewMeasureHeight.setText("");
        }
    }

//    public void showHideView(boolean z, View view) {
//        if (z) {
//            view.setVisibility(View.INVISIBLE);
//        } else {
//            view.setVisibility(View.INVISIBLE);
//        }
//    }

    /* JADX INFO: Access modifiers changed from: private */
    public void blinkTextViewInformationStep() {
        if (getMessageStepInfoUser() == 1) {
            this.animation = new AlphaAnimation(1.0f, 0.0f);
            this.animation.setDuration(1000L);
            this.animation.setInterpolator(new LinearInterpolator());
            this.animation.setRepeatCount(2);
            this.textViewInformationStep.startAnimation(this.animation);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (this.mTextureView.isAvailable()) {
            setupCamera(this.mTextureView.getWidth(), this.mTextureView.getHeight());
            connectCamera();
        } else {
            this.mTextureView.setSurfaceTextureListener(this.mSurfaceTextureListener);
        }
        this.accelerometer = this.sensorManager.getDefaultSensor(1);
        Sensor sensor = this.accelerometer;
        if (sensor == null) {
            Toast.makeText(this, "Não foi possível acessar o sensor de inclinação do dispositivo!", Toast.LENGTH_LONG).show();
            finish();
        } else if (sensor != null) {
            this.sensorManager.registerListener(this, sensor, 3);
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity, androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i != 0 || iArr[0] == 0) {
            return;
        }
        Toast.makeText(this, "Não é possível executar o aplicativo sem a permissão da camera!", Toast.LENGTH_LONG).show();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        this.sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        View decorView = getWindow().getDecorView();
        if (z) {
            decorView.setSystemUiVisibility(5894);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setupCamera(int i, int i2) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String str : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(str);
                if (((Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)).intValue() != 0) {
                    StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    int sensorToDeviceRotation = sensorToDeviceRotation(cameraCharacteristics, getWindowManager().getDefaultDisplay().getRotation());
                    if (sensorToDeviceRotation == 90 || sensorToDeviceRotation == 270) {
                        i2 = i;
                        i = i2;
                    }
                    this.mPreviewSize = chooseOptimalSize(streamConfigurationMap.getOutputSizes(SurfaceTexture.class), i, i2);
                    this.mCameraId = str;
                    return;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == 0) {
                    cameraManager.openCamera(this.mCameraId, this.mCameraDeviceStateCallback, this.mBackgroundHandler);
                    return;
                }
                if (shouldShowRequestPermissionRationale("android.permission.CAMERA")) {
                    Toast.makeText(this, "A execução do aplicativo requer uso da camera!", Toast.LENGTH_LONG).show();
                }
                requestPermissions(new String[]{"android.permission.CAMERA"}, 0);
                return;
            }
            cameraManager.openCamera(this.mCameraId, this.mCameraDeviceStateCallback, this.mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startPreview() {
        SurfaceTexture surfaceTexture = this.mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(this.mPreviewSize.getWidth(), this.mPreviewSize.getHeight());
        Surface surface = new Surface(surfaceTexture);
        try {
            this.mCaptureRequestBuilder = this.mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            this.mCaptureRequestBuilder.addTarget(surface);
            this.mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() { // from class: br.eng.itech.clinometro.MainActivity.14
                @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(MainActivity.this.mCaptureRequestBuilder.build(), null, MainActivity.this.mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this.getApplicationContext(), "Não foi possível configurar a visualização da camera!", Toast.LENGTH_LONG).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void transformImage(int i, int i2) {
        if (this.mPreviewSize == null || this.mTextureView == null) {
            return;
        }
        Matrix matrix = new Matrix();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        float f = i;
        float f2 = i2;
        RectF rectF = new RectF(0.0f, 0.0f, f, f2);
        RectF rectF2 = new RectF(0.0f, 0.0f, this.mPreviewSize.getHeight(), this.mPreviewSize.getWidth());
        float centerX = rectF.centerX();
        float centerY = rectF.centerY();
        if (rotation == 1 || rotation == 3) {
            rectF2.offset(centerX - rectF2.centerX(), centerY - rectF2.centerY());
            matrix.setRectToRect(rectF, rectF2, Matrix.ScaleToFit.FILL);
            float max = Math.max(f / this.mPreviewSize.getWidth(), f2 / this.mPreviewSize.getHeight());
            matrix.postScale(max, max, centerX, centerY);
            matrix.postRotate((rotation - 2) * 90, centerX, centerY);
        }
        this.mTextureView.setTransform(matrix);
    }

    private void closeCamera() {
        CameraDevice cameraDevice = this.mCameraDevice;
        if (cameraDevice != null) {
            cameraDevice.close();
            this.mCameraDevice = null;
        }
    }

    private void startBackgroundThread() {
        this.mBackgroundHandlerThread = new HandlerThread("Camera2VideoImage");
        this.mBackgroundHandlerThread.start();
        this.mBackgroundHandler = new Handler(this.mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        this.mBackgroundHandlerThread.quitSafely();
        try {
            this.mBackgroundHandlerThread.join();
            this.mBackgroundHandlerThread = null;
            this.mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int i) {
        return ((((Integer) cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue() + ORIENTATIONS.get(i)) + 360) % 360;
    }

    private static Size chooseOptimalSize(Size[] sizeArr, int i, int i2) {
        ArrayList arrayList = new ArrayList();
        for (Size size : sizeArr) {
            if (size.getHeight() == (size.getWidth() * i2) / i && size.getWidth() >= i && size.getHeight() >= i2) {
                arrayList.add(size);
            }
        }
        if (arrayList.size() > 0) {
            return (Size) Collections.min(arrayList, new CompareSizeByArea());
        }
        return sizeArr[0];
    }

    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() != 1) {
            return;
        }
        float f = sensorEvent.values[0];
        float f2 = sensorEvent.values[1];
        float f3 = sensorEvent.values[2];
        float degrees = (float) Math.toDegrees(Math.acos(f / ((float) Math.sqrt(((f * f) + (f2 * f2)) + (f3 * f3)))));
        if (f3 > 0.0f) {
            degrees *= -1.0f;
        }
        setCurrentAngleSensor(degrees);
        long currentTimeMillis = System.currentTimeMillis();
        long j = this.lastUpdate;
        if (currentTimeMillis - j > 100) {
            long j2 = currentTimeMillis - j;
            this.lastUpdate = currentTimeMillis;
            if ((Math.abs(((((f2 + f) + f3) - this.lastAccelX) - this.lastAccelY) - this.lastAccelZ) / ((float) j2)) * 10000.0f > 6.0f) {
                if (getStatusMeasureStep() == 1) {
                    setValueAB(getCurrentAngleSensor());
                    this.textViewBaseAngle.setText(String.format("%.2f°", Float.valueOf(getValueAB())));
                } else if (getStatusMeasureStep() == 2) {
                    setValueAT(getCurrentAngleSensor());
                    this.textViewTopAngle.setText(String.format("%.2f°", Float.valueOf(getValueAT())));
                }
            }
            this.lastAccelX = f2;
            this.lastAccelY = f;
            this.lastAccelZ = f3;
        }
    }

    public float getValueHD() {
        return this.valueHD;
    }

    public void setValueHD(float f) {
        this.valueHD = f;
    }

    public float getValueAB() {
        return this.valueAB;
    }

    public void setValueAB(float f) {
        this.valueAB = f;
    }

    public float getValueAT() {
        return this.valueAT;
    }

    public void setValueAT(float f) {
        this.valueAT = f;
    }

    public float getValueHT() {
        return this.valueHT;
    }

    public void setValueHT(float f) {
        this.valueHT = f;
    }

    public float getValueTA() {
        return this.valueTA;
    }

    public void setValueTA(float f) {
        this.valueTA = f;
    }

    public float getValueTB() {
        return this.valueTB;
    }

    public void setValueTB(float f) {
        this.valueTB = f;
    }

    public float getCalculateHeight() {
        return this.calculateHeight;
    }

    public void setCalculateHeight(float f) {
        this.calculateHeight = f;
    }

    public float getCurrentAngleSensor() {
        return this.currentAngleSensor;
    }

    public void setCurrentAngleSensor(float f) {
        this.currentAngleSensor = f;
    }

    public int getStatusMeasureStep() {
        return this.statusMeasureStep;
    }

    public void setStatusMeasureStep(int i) {
        this.statusMeasureStep = i;
    }

    public int getMessageStepInfoUser() {
        return this.messageStepInfoUser;
    }

    public void setMessageStepInfoUser(int i) {
        this.messageStepInfoUser = i;
    }
}