# audio_manager
[![pub package](https://img.shields.io/pub/v/audio_manager.svg)](https://pub.dartlang.org/packages/audio_manager)

A Flutter plugin for music playback, including notification handling.
> This plugin is developed for iOS based on AVPlayer, while Android is based on MediaPlayer.

<img src="https://raw.githubusercontent.com/jeromexiong/audio_manager/master/screenshots/android.png" height="300" alt="The example app running in Android"><img src="https://raw.githubusercontent.com/jeromexiong/audio_manager/master/screenshots/android2.png" height="300" alt="The example app running in Android"><img src="https://raw.githubusercontent.com/jeromexiong/audio_manager/master/screenshots/iOS.png" height="300" alt="The example app running in iOS"><img src="https://raw.githubusercontent.com/jeromexiong/audio_manager/master/screenshots/iOS2.jpeg" height="300" alt="The example app running in iOS">

## iOS
Add the following permissions in the `info.plist` file:
```xml
<key>UIBackgroundModes</key>
<array>
    <string>audio</string>
</array>
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```
- ⚠️ Some methods are invalid in the simulator, please use a real device.

## Android
Since `Android 9.0 (API 28)`, the application disables HTTP plaintext requests by default. To allow requests, add the following in `AndroidManifest.xml` inside the `<application>` tag:
```xml
<application
    ...
    android:enableOnBackInvokedCallback="true"
    android:usesCleartextTraffic="true"
    android:foregroundServiceType="mediaPlayback"
    ...
>
```
- ⚠️ Android minimum supported version: `23` `(app/build.gradle -> minSdkVersion: 23)`
- ⚠️ Android minimum supported Gradle version: `5.4.1` `(gradle-wrapper.properties -> gradle-5.4.1-all.zip)`

Additionally, add the following service declaration inside `AndroidManifest.xml`:
```xml
<service
    android:name="cc.dync.audio_manager.MediaPlayerService"
    android:foregroundServiceType="mediaPlayback"
    android:exported="false"/>
```

## How to use?
The `audio_manager` plugin is developed in singleton mode. You only need to get `AudioManager.instance` in the method to quickly start using it.

## Quick start
You can use local `assets`, `directory file`, or `network` resources:
```dart
// Initial playback. Preloaded playback information
AudioManager.instance
    .start(
        "assets/audio.mp3",
        // "network format resource"
        // "local resource (file://${file.path})"
        "title",
        desc: "desc",
        // cover: "network cover image resource"
        cover: "assets/ic_launcher.png")
    .then((err) {
    print(err);
});

// Play or pause; that is, pause if currently playing, otherwise play
AudioManager.instance.playOrPause()

// Events callback
AudioManager.instance.onEvents((events, args) {
    print("$events, $args");
});
```

---

# فارسی

## معرفی
`audio_manager` یک پلاگین برای پخش موسیقی در Flutter است که مدیریت نوتیفیکیشن را نیز شامل می‌شود.
> این پلاگین در iOS بر پایه `AVPlayer` و در اندروید بر پایه `MediaPlayer` توسعه داده شده است.

## iOS
مجوزهای زیر را به فایل `info.plist` اضافه کنید:
```xml
<key>UIBackgroundModes</key>
<array>
    <string>audio</string>
</array>
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```
- ⚠️ برخی از متدها در شبیه‌ساز کار نمی‌کنند، لطفاً از یک دستگاه واقعی استفاده کنید.

## اندروید
از `Android 9.0 (API 28)` به بعد، درخواست‌های HTTP بدون رمزگذاری به‌طور پیش‌فرض غیرفعال هستند. برای فعال کردن آن‌ها، این مقادیر را به `<application>` در `AndroidManifest.xml` اضافه کنید:
```xml
<application
    ...
    android:enableOnBackInvokedCallback="true"
    android:usesCleartextTraffic="true"
    android:foregroundServiceType="mediaPlayback"
    ...
>
```
- ⚠️ حداقل نسخه پشتیبانی شده اندروید: `23` `(app/build.gradle -> minSdkVersion: 23)`
- ⚠️ حداقل نسخه پشتیبانی شده Gradle: `5.4.1` `(gradle-wrapper.properties -> gradle-5.4.1-all.zip)`

همچنین، سرویس زیر را به `AndroidManifest.xml` اضافه کنید:
```xml
<service
    android:name="cc.dync.audio_manager.MediaPlayerService"
    android:foregroundServiceType="mediaPlayback"
    android:exported="false"/>
```

## نحوه استفاده؟
`audio_manager` به صورت `singleton` توسعه داده شده است. برای شروع استفاده، فقط کافی است از `AudioManager.instance` استفاده کنید.

## شروع سریع
شما می‌توانید از `assets`، فایل‌های محلی، یا منابع `شبکه‌ای` استفاده کنید:
```dart
// مقداردهی اولیه و تنظیم اطلاعات پخش
AudioManager.instance
    .start(
        "assets/audio.mp3",
        // "منابع شبکه‌ای"
        // "منبع محلی (file://${file.path})"
        "title",
        desc: "desc",
        // cover: "منبع تصویر کاور از شبکه"
        cover: "assets/ic_launcher.png")
    .then((err) {
    print(err);
});

// پخش یا توقف: اگر در حال پخش باشد متوقف می‌شود و در غیر این صورت شروع به پخش می‌کند
AudioManager.instance.playOrPause()

// فراخوانی رویدادها
AudioManager.instance.onEvents((events, args) {
    print("$events, $args");
});
```

با این تنظیمات، `audio_manager` بدون مشکل در سیستم‌های اندروید و iOS اجرا خواهد شد. 🚀

