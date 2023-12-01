#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

#define SERVICE_UUID "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define CHARACTERISTIC_UUID_SENSOR "12345678-1234-1234-1234-123456789abc"

void goForward();
void goBackward();
void goRight();
void goLeft();
void stopMovement();
BLECharacteristic *pSensorCharacteristic;

class MyCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    std::string value = pCharacteristic->getValue();

    if (value.length() > 0) {
      char command = value[0];
      Serial.print("Command Received: ");
      Serial.println(command);

      switch (command) {
        case 'f':  // go forward
          goForward();
          break;
        case 'b':  // go backward
          goBackward();
          break;
        case 'r':  // turn right
          goRight();
          break;
        case 'l':  // turn left
          goLeft();
          break;
        case 's':  // stop
          stopMovement();
          break;
        default:
          Serial.println(command);
          break;
      }
    }
  }
};

void setup() {
  Serial.begin(115200);
  SetupMotorDriver();
  SetupSensors();
  SetupCamera();
  Serial.println("Motor driver started");
  BLEDevice::init("BLE TANK");
  BLEServer *pServer = BLEDevice::createServer();
  BLEService *pService = pServer->createService(SERVICE_UUID);
  BLECharacteristic *pCharacteristic = pService->createCharacteristic(
    CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristic->setValue("Hello World says BLE TANK");

  pSensorCharacteristic = pService->createCharacteristic(
    CHARACTERISTIC_UUID_SENSOR,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY);
  pSensorCharacteristic->setValue("Inicial");
  pCharacteristic->setCallbacks(new MyCallbacks());
  pService->start();
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
  Serial.println("Starting BLE work!");
}

void loop() {
  // Get distance from the ultrasonic sensor
  int distance = getDistance();

  // Check if the line is detected by front and back sensors
  bool frontLineDetected = isFrontLineDetected();
  bool backLineDetected = isBackLineDetected();

  // Prepare a string to send via BLE
  // Format: "Distance: <distance>, Front: <frontLineDetected>, Back: <backLineDetected>"
  char sensorData[50];
  sprintf(sensorData, "Distance: %d, Front: %d, Back: %d",
          distance, frontLineDetected, backLineDetected);

  // Update the BLE characteristic value and notify
  pSensorCharacteristic->setValue(sensorData);
  pSensorCharacteristic->notify();

  delay(1000);  // Adjust the delay as needed
}
