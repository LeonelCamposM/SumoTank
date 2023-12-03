#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/queue.h"

QueueHandle_t commandQueue;

struct Command {
  char action;  // 'S' para stop, 'B' para backward, 'F' para forward, 'L' para left, 'R' para right
  int speed;

  bool operator==(const Command& other) const {
    return action == other.action && speed == other.speed;
  }

  bool operator!=(const Command& other) const {
    return !(*this == other);
  }
};

struct SensorState {
  int distance;
  bool frontLineDetected;
  bool backLineDetected;
};

void setup() {
  Serial.begin(9600);
  SetupMotorControl();
  SetupSensors();

  commandQueue = xQueueCreate(10, sizeof(Command));
  xTaskCreate(sensorTask, "SensorTask", 1000, NULL, 1, NULL);
  xTaskCreate(motorTask, "MotorTask", 1000, NULL, 1, NULL);
}

Command determineCommand(const SensorState& state) {
  Command cmd = { 'S', 0 };  // 'N' puede representar 'Ninguna acción' o estado neutro

  if (state.frontLineDetected || state.backLineDetected) {
    cmd.action = 'S';  // 'S' para Stop
    cmd.speed = 255;
  } else if (state.distance <= 40) {
    cmd.action = 'F';  // 'F' para Forward (hacia adelante)
    cmd.speed = 255;
  } else {
    cmd.action = 'F';  // 'F' para Forward a menor velocidad
    cmd.speed = 120;
  }

  return cmd;
}

void sensorTask(void* parameter) {
  SensorState currentState;
  Command lastCommandSent = {};
  Command currentCommand;

  while (true) {
    currentState.distance = getDistance();
    currentState.frontLineDetected = isFrontLineDetected();
    currentState.backLineDetected = isBackLineDetected();

    currentCommand = determineCommand(currentState);

    if (currentCommand != lastCommandSent) {
      xQueueSend(commandQueue, &currentCommand, portMAX_DELAY);
      lastCommandSent = currentCommand;
    }

    vTaskDelay(pdMS_TO_TICKS(100));
  }
}

void motorTask(void* parameter) {
  Command cmd;
  while (true) {
    if (xQueueReceive(commandQueue, &cmd, portMAX_DELAY)) {
      switch (cmd.action) {
        case 'S':
          stop();
          moveBackward(cmd.speed);
          delay(500);       
          turnRight(cmd.speed); 
          delay(500);       
          break;
        case 'B':
          moveBackward(cmd.speed);
          break;
        case 'F':
          moveForward(cmd.speed);
          break;
        case 'L':
          turnLeft(cmd.speed);
          break;
        case 'R':
          turnRight(cmd.speed);
          break;
      }
    }
  }
}


void loop() {
  // El loop principal puede quedar vacío en un entorno FreeRTOS
}
