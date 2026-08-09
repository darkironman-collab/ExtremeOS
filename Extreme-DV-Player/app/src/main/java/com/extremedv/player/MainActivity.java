package com.extremedv.player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class MainActivity extends Activity {
    private static final int REQUEST_OPEN_FILE = 1001;
    private ExoPlayer player;
    private PlayerView playerView;
    private TextView detectedText;
    private TextView outputText;
    private TextView engineText;
    private TextView capabilityText;
    private Spinner profileSpinner;
    private Format currentFormat;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        buildUi();
        initPlayer();
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) play(intent.getData());
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(dp(12), dp(8), dp(12), dp(8));

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER_VERTICAL);

        Button file = button("Open File");
        file.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("video/*");
            startActivityForResult(i, REQUEST_OPEN_FILE);
        });
        controls.addView(file, new LinearLayout.LayoutParams(0, dp(48), 1f));

        Button url = button("Open URL");
        url.setOnClickListener(v -> showUrlDialog());
        controls.addView(url, new LinearLayout.LayoutParams(0, dp(48), 1f));

        Button caps = button("DV Decoder");
        caps.setOnClickListener(v -> showCapabilities());
        controls.addView(caps, new LinearLayout.LayoutParams(0, dp(48), 1f));
        root.addView(controls);

        profileSpinner = new Spinner(this);
        profileSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, DvTargetProfile.values()));
        profileSpinner.setSelection(DvTargetProfile.AUTO.ordinal());
        profileSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { refreshOutput(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        root.addView(profileSpinner, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        detectedText = label("Input: waiting for video…");
        outputText = label("Output: Auto → P8.4 compatibility");
        engineText = label("Native RPU engine: checking…");
        capabilityText = label("");
        root.addView(detectedText);
        root.addView(outputText);
        root.addView(engineText);
        root.addView(capabilityText);

        playerView = new PlayerView(this);
        playerView.setUseController(true);
        playerView.setKeepScreenOn(true);
        root.addView(playerView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
        engineText.setText("Native RPU engine: " + (DoviNative.isAvailable() ? "READY (libdovi JNI)" : "not bundled in this APK"));
    }

    private void initPlayer() {
        DefaultRenderersFactory renderers = new DefaultRenderersFactory(this).setEnableDecoderFallback(true);
        player = new ExoPlayer.Builder(this, renderers).build();
        playerView.setPlayer(player);
        player.addListener(new Player.Listener() {
            @Override public void onVideoSizeChanged(androidx.media3.common.VideoSize videoSize) { refreshDetected(); }
            @Override public void onTracksChanged(androidx.media3.common.Tracks tracks) { refreshDetected(); }
            @Override public void onPlaybackStateChanged(int playbackState) { if (playbackState == Player.STATE_READY) refreshDetected(); }
            @Override public void onPlayerError(PlaybackException error) {
                detectedText.setText("Playback error: " + error.getErrorCodeName() + "\n" + error.getMessage());
            }
        });
    }

    private void play(Uri uri) {
        currentFormat = null;
        detectedText.setText("Input: probing " + uri);
        player.setMediaItem(MediaItem.fromUri(uri));
        player.prepare();
        player.play();
    }

    private void refreshDetected() {
        currentFormat = player.getVideoFormat();
        detectedText.setText("Input: " + DvProfileDetector.describe(currentFormat));
        refreshOutput();
    }

    private void refreshOutput() {
        DvTargetProfile target = (DvTargetProfile) profileSpinner.getSelectedItem();
        if (target == null) return;
        int source = DvProfileDetector.profileNumber(currentFormat);
        outputText.setText("Output target: " + target.label + "\n" + DvConversionPlanner.plan(source, target));
    }

    private void showUrlDialog() {
        EditText input = new EditText(this);
        input.setHint("https://…/video.mkv or .mp4 or HLS/DASH URL");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        new AlertDialog.Builder(this)
            .setTitle("Open direct video URL")
            .setView(input)
            .setPositiveButton("Play", (d, w) -> {
                String s = input.getText().toString().trim();
                if (!s.isEmpty()) play(Uri.parse(s));
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showCapabilities() {
        String result = DvCapabilityScanner.scan();
        capabilityText.setText("Device DV decoder:\n" + result);
        new AlertDialog.Builder(this).setTitle("Dolby Vision MediaCodec capabilities").setMessage(result).setPositiveButton("OK", null).show();
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        return b;
    }

    private TextView label(String s) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextColor(Color.WHITE);
        v.setTextSize(14);
        v.setPadding(dp(6), dp(3), dp(6), dp(3));
        return v;
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try { getContentResolver().takePersistableUriPermission(uri, data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Throwable ignored) {}
            play(uri);
        }
    }

    @Override protected void onStop() { super.onStop(); if (!isChangingConfigurations()) player.pause(); }
    @Override protected void onDestroy() { if (player != null) player.release(); super.onDestroy(); }
}
