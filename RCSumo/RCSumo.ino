#include <WiFiClient.h>
#include <ArduinoJson.h>
#include <WebServer.h>

const char *ssid = "xiaocar";
const char *password = "xiaocar";
WebServer server(80);

void setupWifi() {
  if (!WiFi.softAP(ssid)) {
    log_e("Soft AP creation failed.");
    while (1)
      ;
  }

  IPAddress myIP = WiFi.softAPIP();
  Serial.print("AP IP address: ");
  Serial.println(myIP);
  server.begin();
}

void handleRoot() {
  String htmlCode = R"html(
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <style>
      body {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        min-height: 100vh;
        margin: 0;
      }
      #image-container {
        width: 80%;
        max-width: 400px;
        margin-bottom: 20px;
      }
      #image {
        width: 300px;
        height: 300px;
        display: block;
        margin: 0 auto;
      }
      .btn-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        width: 100%;
        max-width: 300px;
        margin-top: 20px;
      }
      .row {
        display: flex;
        justify-content: center;
        margin-bottom: 10px;
      }
      .row + .row {
        margin-top: 10px;
      }
      .btn {
        width: 80px;
        height: 80px;
        font-size: 16px;
        background-color: #3498db;
        color: #ffffff;
        border: none;
        cursor: pointer;
        border-radius: 50%;
        transition: background-color 0.3s ease;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 10px;
      }
      .btn:hover {
        background-color: #2980b9;
      }
      .btn-big {
        width: 100px;
        height: 100px;
        font-size: 18px;
      }
      @media (max-width: 600px) {
        .btn-container {
          flex-direction: row;
          align-items: center;
        }
        .row {
          margin-bottom: 0;
        }
      }
    </style>
    <style>
      body {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        min-height: 100vh;
        margin: 0;
        padding: 10px; /* Add padding to ensure content doesn't touch the edges */
      }
      #image-container {
        width: 80%;
        max-width: 400px;
        margin: 20px auto; /* Center the image container and add margin to the top */
      }
      #image {
        max-width: 100%; /* Make the image responsive */
        height: auto; /* Maintain the aspect ratio */
        display: block;
        margin: 0 auto; /* Center the image */
      }
      .btn-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        width: 100%;
        max-width: 300px;
        margin: auto; /* Auto margin for horizontal centering */
      }
      .row {
        display: flex;
        justify-content: center;
        margin: 10px 0; /* Uniform margin for top and bottom */
      }
      .btn {
        width: 80px;
        height: 80px;
        font-size: 16px;
        background-color: #3498db;
        color: #ffffff;
        border: none;
        cursor: pointer;
        border-radius: 50%;
        transition: background-color 0.3s ease;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 10px;
      }
      .btn:hover {
        background-color: #2980b9;
      }
      .btn-big {
        width: 100px;
        height: 100px;
        font-size: 18px;
      }
      @media (max-width: 600px) {
        .btn-container {
          flex-direction: row;
          flex-wrap: wrap; /* Allow buttons to wrap on smaller screens */
          justify-content: space-around; /* Evenly distribute space around items */
        }
        .row {
          flex-basis: 100%; /* Allow rows to take full width on wrap */
          justify-content: space-around; /* Evenly distribute space around items */
          margin: 10px 0; /* Add margin to top and bottom for spacing */
        }
        .btn,
        .btn-big {
          width: 70px; /* Smaller width for better fit */
          height: 70px; /* Smaller height for better fit */
          font-size: 14px; /* Smaller font size for better fit */
          margin: 5px; /* Reduced margin for tighter fit */
        }
        .btn-big {
          width: 90px; /* Smaller width for better fit */
          height: 90px; /* Smaller height for better fit */
        }
      }
    </style>
  </head>
  <body>
    <div id="image-container">
      <img id="photoImage" src="" alt="Displayed Image" />
    </div>
    <div class="btn-container">
      <div class="row">
        <button
          class="btn"
          id="forward"
          ontouchstart="sendRequest('/forward', event)"
          ontouchend="sendRequest('/stop', event)"
        >
          ▲
        </button>
      </div>
      <div class="row">
        <button
          class="btn btn-big"
          id="left"
          ontouchstart="sendRequest('/left', event)"
          ontouchend="sendRequest('/stop', event)"
        >
          ◄
        </button>
        <button class="btn" id="photo" onclick="sendRequest('/photo', event)">
          📷
        </button>
        <button
          class="btn btn-big"
          id="right"
          ontouchstart="sendRequest('/right', event)"
          ontouchend="sendRequest('/stop', event)"
        >
          ►
        </button>
      </div>
      <div class="row">
        <button
          class="btn"
          id="backward"
          ontouchstart="sendRequest('/backward', event)"
          ontouchend="sendRequest('/stop', event)"
        >
          ▼
        </button>
      </div>
    </div>

    <script>
      function sendRequest(url, event) {
        if (event) {
          event.preventDefault();
        }
        if ("vibrate" in navigator) {
          navigator.vibrate(200);
        }
        var xhr = new XMLHttpRequest();
        xhr.open("GET", url, true);
        xhr.onreadystatechange = function () {
          if (xhr.readyState === 4 && xhr.status === 200) {
            if (url === "/photo") {
              var imgElement = document.getElementById("photoImage");
              imgElement.src = "data:image/jpeg;base64," + xhr.responseText;
            }
          }
        };
        xhr.send();
      }
    </script>
  </body>
</html>

)html";

  server.send(200, "text/html", htmlCode);
}

void setupServer() {
  setupWifi();
  server.on("/", handleRoot);
  server.on("/forward", handleForward);
  server.on("/backward", handleBackward);
  server.on("/right", handleRight);
  server.on("/left", handleLeft);
  server.on("/photo", handlePhoto);
  server.on("/stop", stopMovement);

  server.onNotFound([]() {
    server.send(400, "text/plain", "Not found");
  });

  server.begin();
  Serial.println("HTTP server started");
}

void handleForward() {
  goForward();
}

void handleBackward() {
  goBackward();
}

void handleRight() {
  goRight();
}

void handleLeft() {
  goLeft();
}


void handlestopMovement() {
  stopMovement();
}

void handlePhoto() {
  String imageBase64 = takePhoto();
  server.send(200, "text/plain", imageBase64);
}

void setup() {
  Serial.begin(115200);
  SetupMotorDriver();
  SetupCamera();
  SetupSensorManager();
  setupServer();
}

void loop() {
  server.handleClient();
  delay(1);
}
