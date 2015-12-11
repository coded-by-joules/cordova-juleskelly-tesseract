var tesseractOCR = {
  // loads the train data
  load: function (callback) {
    cordova.exec(
      callback,             // success callback
      function (err) {      // error callback
        callback(err);
      },
      "TesseractOCR",       // class name
      "loadEngine",         // method name
      []
    );
  },

  // recognizes the image
  recognizeImage: function (imageURL, callback) {
    cordova.exec(
      callback,
      function (err) {
        callback(err);
      },
      "TesseractOCR",
      "recognizeImage",
      [{
        "imageURL": imageURL
      }]
    );
  }
};

module.exports = tesseractOCR;
