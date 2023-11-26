// Define the motor control pins as constants or macros
#define MOTOR_A_INPUT1 D10
#define MOTOR_A_INPUT2 D9
#define MOTOR_A_PWM D8
#define MOTOR_B_INPUT1 D5
#define MOTOR_B_INPUT2 D6
#define MOTOR_B_PWM D7
#define STBY_PIN D4


// Constructor
void SetupMotorControl() {
  // Initialize the motor control pins as outputs
  pinMode(MOTOR_A_INPUT1, OUTPUT);
  pinMode(MOTOR_A_INPUT2, OUTPUT);
  pinMode(MOTOR_A_PWM, OUTPUT);
  pinMode(MOTOR_B_INPUT1, OUTPUT);
  pinMode(MOTOR_B_INPUT2, OUTPUT);
  pinMode(MOTOR_B_PWM, OUTPUT);
  pinMode(STBY_PIN, OUTPUT);

  // Enable the motor driver outputs
  digitalWrite(STBY_PIN, HIGH);
}

// Method to control motor A
void controlMotorA(bool direction, int speed) {
  digitalWrite(MOTOR_A_INPUT1, direction);
  digitalWrite(MOTOR_A_INPUT2, !direction);
  analogWrite(MOTOR_A_PWM, speed);
}

// Method to control motor B
void controlMotorB(bool direction, int speed) {
  digitalWrite(MOTOR_B_INPUT1, direction);
  digitalWrite(MOTOR_B_INPUT2, !direction);
  analogWrite(MOTOR_B_PWM, speed);
}

// Method to drive both motors in forward direction
void moveForward(int velocity) {
  controlMotorA(false, velocity);
  controlMotorB(false, velocity);
}

// Method to drive both motors in backward direction
void moveBackward(int velocity) {
  controlMotorA(true, velocity);
  controlMotorB(true, velocity);
}

// Method to rotate the robot right
void turnRight(int velocity) {
  controlMotorA(false, velocity);
  controlMotorB(true, velocity);
}

// Method to rotate the robot left
void turnLeft(int velocity) {
  controlMotorA(true, velocity);
  controlMotorB(false, velocity);
}

// Method to stop both motors
void stop() {
  controlMotorA(false, 0);
  controlMotorB(false, 0);
}
