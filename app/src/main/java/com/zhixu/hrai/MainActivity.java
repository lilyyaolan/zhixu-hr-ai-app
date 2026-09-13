package com.zhixu.hrai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    private final ActivityResultLauncher<String> audioPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) launchSpeechRecognizer();
            else sendSpeechError("未获得麦克风权限");
        });

    private final ActivityResultLauncher<Intent> speechLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                ArrayList<String> results =
                    result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null && !results.isEmpty()) {
                    sendSpeechResult(results.get(0));
                    return;
                }
            }
            sendSpeechError("没有识别到有效语音");
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zhixu.hrai.R.layout.activity_main);

        webView = findViewById(com.zhixu.hrai.R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
        });

        webView.addJavascriptInterface(new SpeechBridge(), "AndroidSpeech");
        loadBundledApp();
    }

    private void loadBundledApp() {
        try (InputStream input = getAssets().open("www/index.html");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            String html = output.toString(StandardCharsets.UTF_8.name());
            html = html.replace("}return \"其他\"}", "}");
            webView.loadDataWithBaseURL(
                    "file:///android_asset/www/",
                    html,
                    "text/html",
                    "UTF-8",
                    null
            );
        } catch (Exception e) {
            webView.loadUrl("file:///android_asset/www/index.html");
        }
    }

    public class SpeechBridge {
        @JavascriptInterface
        public void startListening() {
            runOnUiThread(() -> {
                if (ContextCompat.checkSelfPermission(
                        MainActivity.this, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED) {
                    launchSpeechRecognizer();
                } else {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                }
            });
        }
    }

    private void launchSpeechRecognizer() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN");
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出你的HR工作事项");
            speechLauncher.launch(intent);
        } catch (Exception e) {
            sendSpeechError("当前手机未提供可用的语音识别服务");
        }
    }

    private void sendSpeechResult(String text) {
        String safe = text.replace("\\", "\\\\")
                          .replace("'", "\\'")
                          .replace("\n", " ");
        webView.evaluateJavascript(
                "window.onNativeSpeechResult('" + safe + "')", null);
    }

    private void sendSpeechError(String message) {
        String safe = message.replace("\\", "\\\\").replace("'", "\\'");
        webView.evaluateJavascript(
                "window.onNativeSpeechError('" + safe + "')", null);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
