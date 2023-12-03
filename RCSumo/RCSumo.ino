#include <Arduino.h>
#include <WiFi.h>
#include <WiFiMulti.h>
#include <WiFiClientSecure.h>
#include <WebSocketsServer.h>

WiFiMulti WiFiMulti;
WebSocketsServer webSocket = WebSocketsServer(81);

// Configuración de IP estática
IPAddress local_IP(192, 168, 208, 137);  // Cambia esto a la IP deseada
IPAddress gateway(192, 168, 1, 1);       // Normalmente la IP de tu router
IPAddress subnet(255, 255, 255, 0);      // Máscara de subred estándar


void handleForward() {
  goForward();
  Serial.println("Moving forward");
}

void handleBackward() {
  goBackward();
  Serial.println("Moving backward");
}

void handleRight() {
  goRight();
  Serial.println("Turning right");
}

void handleLeft() {
  goLeft();
  Serial.println("Turning left");
}

void handleStop() {
  stopMovement();
  Serial.println("Stopping");
}

String handlePhoto() {
  Serial.println("Taking photo");
  return takePhoto();
}

void webSocketEvent(uint8_t num, WStype_t type, uint8_t *payload, size_t length) {
  switch (type) {
    case WStype_DISCONNECTED:
      Serial.printf("[%u] Disconnected!\n", num);
      break;
    case WStype_CONNECTED:
      {
        IPAddress ip = webSocket.remoteIP(num);
        Serial.printf("[%u] Connected from %d.%d.%d.%d url: %s\n", num, ip[0], ip[1], ip[2], ip[3], payload);
        webSocket.sendTXT(num, "Connected");
      }
      break;
    case WStype_TEXT:
      Serial.printf("[%u] get Text: %s\n", num, payload);
      String command = String((char *)payload);

      if (command == "forward") {
        handleForward();
      } else if (command == "backward") {
        handleBackward();
      } else if (command == "right") {
        handleRight();
      } else if (command == "left") {
        handleLeft();
      } else if (command == "stop") {
        handleStop();
      } else if (command == "photo") {
        String img = handlePhoto();
        if (img.length() > 0) {
          webSocket.sendTXT(num, img);
        }
      }

      break;
  }
}

void setup() {
  Serial.begin(115200);
  Serial.setDebugOutput(true);
  SetupMotorDriver();
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, HIGH);
  Serial.println();
  Serial.println();
  Serial.println();
  SetupCamera();
  WiFiMulti.addAP("Carmelo", "Prueba01!");

  // Intenta conectarte a las redes WiFi
  while (WiFiMulti.run() != WL_CONNECTED) {
    Serial.println("Conectando a WiFi...");
    delay(100);
  }

  // Una vez conectado, configura la IP estática
  if (!WiFi.config(local_IP, gateway, subnet)) {
    Serial.println("Fallo al configurar la IP estática");
  }

  Serial.println("Conectado a WiFi");
  Serial.print("Dirección IP: ");
  Serial.println(WiFi.localIP());
  digitalWrite(LED_BUILTIN, LOW);
  webSocket.begin();
  webSocket.onEvent(webSocketEvent);
}

void loop() {
  webSocket.loop();
}
