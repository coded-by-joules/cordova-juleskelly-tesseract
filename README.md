# cordova-juleskelly-tesseract
Tesseract OCR Plugin for Cordova

## How to install
Before installing the plugin to your project, you need to do a few things:

1. Copy the `contents of the libs` folder to `platforms/android/libs` directory of your Cordova project
2. Copy the `tessdata` folder to `platforms/android/assets` directory of your Cordova project
3. Instal the plugin using `cordova plugin add http://github.com/phoenixtwister/cordova-juleskelly-tesseract.git`

## How to use
Add this code on the `deviceready` event of your project. This takes time on your first run of your application.

    document.addEventListener("deviceready", onDeviceReady, false);
    function onDeviceReady() {
        tesseractOCR.load(function (callback) {
          // your code here
        }
    }
    
Whenever you want to scan the text on your image, you have to pass the image path to the `recognizeImage` method.

    tesseractOCR.recognizeImage(imageURL, function (result) {
      // the result argument stores the recognized text
      
    });
    
## Notes
This plugin uses Tess-Two Tesseract Tools for integrating it for Android
[https://github.com/rmtheis/tess-two/tree/master/tess-two]

This plugin is only for Android. More platforms will be supported soon.
