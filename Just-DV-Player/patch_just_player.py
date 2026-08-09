from pathlib import Path

ROOT = Path(__file__).resolve().parent
UPSTREAM = Path.cwd()
PLAYER = UPSTREAM / "app/src/main/java/com/brouken/player/PlayerActivity.java"
STRINGS = UPSTREAM / "app/src/main/res/values/strings.xml"
GRADLE = UPSTREAM / "app/build.gradle"


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"Patch anchor {label!r} expected once, found {count}")
    return text.replace(old, new, 1)


text = PLAYER.read_text(encoding="utf-8")

text = replace_once(
    text,
    "import android.text.TextUtils;\n",
    "import android.text.InputType;\nimport android.text.TextUtils;\n",
    "InputType import",
)
text = replace_once(
    text,
    "import android.widget.FrameLayout;\n",
    "import android.widget.EditText;\nimport android.widget.FrameLayout;\n",
    "EditText import",
)
text = replace_once(
    text,
    "    private ImageButton buttonOpen;\n",
    "    private ImageButton buttonOpen;\n    private ImageButton buttonUrl;\n    private ImageButton buttonDv;\n",
    "DV control fields",
)

button_anchor = """        buttonOpen.setOnLongClickListener(view -> {\n            if (!isTvBox && mPrefs.askScope) {\n                askForScope(true, false);\n            } else {\n                loadSubtitleFile(mPrefs.mediaUri);\n            }\n            return true;\n        });\n\n        if (Utils.isPiPSupported(this)) {\n"""
button_replacement = """        buttonOpen.setOnLongClickListener(view -> {\n            if (!isTvBox && mPrefs.askScope) {\n                askForScope(true, false);\n            } else {\n                loadSubtitleFile(mPrefs.mediaUri);\n            }\n            return true;\n        });\n\n        buttonUrl = new ImageButton(this, null, 0, R.style.ExoStyledControls_Button_Bottom);\n        buttonUrl.setImageResource(android.R.drawable.ic_menu_send);\n        buttonUrl.setContentDescription(\"Open URL\");\n        buttonUrl.setOnClickListener(view -> showUrlDialog());\n\n        buttonDv = new ImageButton(this, null, 0, R.style.ExoStyledControls_Button_Bottom);\n        buttonDv.setImageResource(android.R.drawable.ic_menu_manage);\n        buttonDv.setContentDescription(\"Dolby Vision profile\");\n        buttonDv.setOnClickListener(view -> showDvProfileDialog());\n        buttonDv.setOnLongClickListener(view -> {\n            showDvInfoDialog();\n            return true;\n        });\n\n        if (Utils.isPiPSupported(this)) {\n"""
text = replace_once(text, button_anchor, button_replacement, "URL and DV buttons")

text = replace_once(
    text,
    "        controls.addView(buttonOpen);\n        controls.addView(exoSubtitle);\n",
    "        controls.addView(buttonOpen);\n        controls.addView(buttonUrl);\n        controls.addView(buttonDv);\n        controls.addView(exoSubtitle);\n",
    "bottom controls",
)

text = replace_once(
    text,
    ".setMapDV7ToHevc(mPrefs.mapDV7ToHevc);",
    ".setMapDV7ToHevc(DvSettings.mapDv7ToHevc(this, mPrefs.mapDV7ToHevc));",
    "DV7 renderer mapping",
)

methods_anchor = """    private void handleSubtitles(Uri uri) {\n"""
methods = """    private void showUrlDialog() {\n        final EditText input = new EditText(this);\n        input.setSingleLine(true);\n        input.setHint(\"https://…/video.mkv, .mp4, HLS or DASH\");\n        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);\n\n        new AlertDialog.Builder(this)\n                .setTitle(\"Open direct video URL\")\n                .setView(input)\n                .setPositiveButton(\"Play\", (dialog, which) -> {\n                    final String value = input.getText().toString().trim();\n                    if (value.isEmpty()) return;\n                    final Uri uri = Uri.parse(value);\n                    if (!uri.isAbsolute()) {\n                        Toast.makeText(this, \"Enter a complete URL\", Toast.LENGTH_SHORT).show();\n                        return;\n                    }\n                    if (player != null) {\n                        releasePlayer();\n                    }\n                    resetApiAccess();\n                    mPrefs.updateMedia(this, uri, null);\n                    searchSubtitles();\n                    focusPlay = true;\n                    initializePlayer();\n                })\n                .setNegativeButton(\"Cancel\", null)\n                .show();\n    }\n\n    private void showDvProfileDialog() {\n        final DvTargetProfile[] profiles = DvTargetProfile.values();\n        final String[] labels = new String[profiles.length];\n        for (int i = 0; i < profiles.length; i++) {\n            labels[i] = profiles[i].label;\n        }\n\n        final DvTargetProfile current = DvSettings.getTarget(this);\n        new AlertDialog.Builder(this)\n                .setTitle(\"Dolby Vision output target\")\n                .setSingleChoiceItems(labels, current.ordinal(), (dialog, which) -> {\n                    final DvTargetProfile selected = profiles[which];\n                    DvSettings.setTarget(this, selected);\n                    buttonDv.setContentDescription(\"Dolby Vision: \" + selected.label);\n                    dialog.dismiss();\n                    Toast.makeText(this, selected.label, Toast.LENGTH_SHORT).show();\n\n                    if (player != null && haveMedia) {\n                        final long position = player.getCurrentPosition();\n                        final boolean resume = player.getPlayWhenReady();\n                        mPrefs.updatePosition(position);\n                        releasePlayer(false);\n                        restorePlayState = resume;\n                        initializePlayer();\n                    }\n                })\n                .setNegativeButton(\"Close\", null)\n                .show();\n    }\n\n    private void showDvInfoDialog() {\n        final Format format = player == null ? null : player.getVideoFormat();\n        final StringBuilder info = new StringBuilder();\n        info.append(\"Input: \" ).append(DvProfileDetector.describe(format));\n        info.append(\"\\nTarget: \" ).append(DvSettings.getTarget(this).label);\n        info.append(\"\\nNative RPU engine: \" ).append(DoviNative.isAvailable() ? \"READY\" : \"Unavailable\");\n        info.append(\"\\n\\nDevice Dolby Vision decoder:\\n\" ).append(DvCapabilityScanner.scan());\n\n        new AlertDialog.Builder(this)\n                .setTitle(\"Dolby Vision info\")\n                .setMessage(info.toString())\n                .setPositiveButton(\"OK\", null)\n                .show();\n    }\n\n    private void handleSubtitles(Uri uri) {\n"""
text = replace_once(text, methods_anchor, methods, "clean dialogs")

PLAYER.write_text(text, encoding="utf-8")

strings = STRINGS.read_text(encoding="utf-8")
strings = replace_once(
    strings,
    '<string name="app_name" translatable="false">Just Player</string>',
    '<string name="app_name" translatable="false">Extreme DV Player</string>',
    "app name",
)
STRINGS.write_text(strings, encoding="utf-8")

gradle = GRADLE.read_text(encoding="utf-8")
gradle = replace_once(
    gradle,
    'applicationId "com.brouken.player"',
    'applicationId "com.extreme.dvplayer"',
    "application id",
)
gradle = replace_once(
    gradle,
    'archivesName = "Just.Player.v${defaultConfig.versionName}"',
    'archivesName = "Extreme.DV.Player.v${defaultConfig.versionName}"',
    "archive name",
)
GRADLE.write_text(gradle, encoding="utf-8")

print("Just Player patched: clean fullscreen UI + URL + Dolby Vision dialogs")
