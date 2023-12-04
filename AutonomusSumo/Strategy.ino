void SetupStrategy() {
  SetupMotorControl();
  SetupSensors();
}

void scanForEnemies() {
  unsigned long start = millis();
  Serial.println("Starting enemy scan");
  while (true) {
    // Verifica si han pasado 6.5 segundos
    if (millis() - start >= 6500) {
      stop();
      break;
    }

    int distance = getDistance();
    Serial.println(distance);
    if (distance <= 80) {
      digitalWrite(LED_BUILTIN, LOW);
      moveForward(255);
      break;
    } else {
      digitalWrite(LED_BUILTIN, HIGH);
      turnRight(120);
    }
  }
}

void execute() {
  int distance = getDistance();
  Serial.print("Front distance: ");
  Serial.println(distance);

  if ((isFrontLineDetected() || isBackLineDetected()) && distance > 80) {
    stop();
    Serial.println("Line detected, stopping and moving backward");
    moveBackward(255);
    delay(800);
    scanForEnemies();
  } else {

    if (distance <= 80) {
      digitalWrite(LED_BUILTIN, LOW);
      moveForward(255);
      Serial.println("Distance <= 80, moving forward fast");
    } else {
      digitalWrite(LED_BUILTIN, HIGH);
      moveForward(180);
      Serial.println("Distance > 80, moving forward slow");
    }
  }
}
