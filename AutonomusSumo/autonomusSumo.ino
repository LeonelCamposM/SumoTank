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
  } else if (state.distance < 80) {
    cmd.action = 'F';  // 'F' para Forward (hacia adelante)
    cmd.speed = 255;
  } else {
    cmd.action = 'F';  // 'F' para Forward a menor velocidad
    cmd.speed = 120;
  }

  return cmd;
}

Command evaluateSensorState() {
  SensorState currentState;
  currentState.distance = getDistance();
  //Serial.println("Distance:");
  // Serial.println(currentState.distance);
  currentState.frontLineDetected = isFrontLineDetected();
  currentState.backLineDetected = isBackLineDetected();

  Command currentCommand = determineCommand(currentState);
  return currentCommand;
}


void sensorTask(void* parameter) {
  Command lastCommandSent = {};
  Command currentCommand;

  while (true) {
    currentCommand = evaluateSensorState();  // Obtener el estado actual de los sensores

    if (currentCommand != lastCommandSent) {
      if (lastCommandSent.action == 'S') {
        // Escaneo de enemigos, se asume que el motor se mantiene moviendose a la derecha
        int counter = 0;
        Command newCommand;
        while (counter < 40) {
          newCommand = evaluateSensorState();  // Re-evaluar el estado de los sensores
          if (newCommand.action == 'F' && newCommand.speed == 255) {
            // Serial.println("SCANNING: encontrado enemigo");
            break;
          } else {
            //Serial.println("SCANNING: No encontrado enemigo");
          }
          counter++;
          // Serial.print("COUNTER: ");
          // Serial.println(counter);
          vTaskDelay(pdMS_TO_TICKS(100));
        }
        // Asegurarse de enviar el último comando evaluado después de salir del bucle
        currentCommand = newCommand;
        lastCommandSent = newCommand;
        //Serial.print("COMMAND: enviando ");
        Serial.println(newCommand.action);
        xQueueSend(commandQueue, &newCommand, portMAX_DELAY);
      } else {
        // Serial.print("COMMAND: enviando ");
        // Serial.println(currentCommand.action);
        xQueueSend(commandQueue, &currentCommand, portMAX_DELAY);
        lastCommandSent = currentCommand;
      }
    } else {
      vTaskDelay(pdMS_TO_TICKS(100));
    }
  }
}

void motorTask(void* parameter) {
  Command cmd;
  bool isMoving = false;

  while (true) {
    if (xQueueReceive(commandQueue, &cmd, portMAX_DELAY)) {
      isMoving = true;
      switch (cmd.action) {
        case 'S':
          stop();
          moveBackward(cmd.speed);
          delay(500);
          break;
        case 'F':
          moveForward(cmd.speed);
          isMoving = false;  // Restablece el estado de movimiento
          break;
      }
    }

    // Mantener el movimiento actual si no se ha recibido una nueva instrucción
    // después de recibir un S
    if (isMoving) {
      turnRight(120);
    }
  }
}




void loop() {
  // El loop principal puede quedar vacío en un entorno FreeRTOS
}
