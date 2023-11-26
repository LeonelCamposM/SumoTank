const int trig = D2;
const int eco = D3;
const int front = D0;
const int back = D1;

void SetupSensorManager() {
  pinMode(trig, OUTPUT);
  pinMode(eco, INPUT);
  pinMode(front, INPUT);
  pinMode(back, INPUT);
}

int getDistance() {
  digitalWrite(trig, HIGH);
  delay(1);
  digitalWrite(trig, LOW);
  int duration = pulseIn(eco, HIGH);
  int distance = duration / 58.2;
  return distance;
}

bool getFrontTrack() {
  bool frontDetected = digitalRead(front) == HIGH;
  return frontDetected;
}

bool getBackTrack() {
  bool backDetected = digitalRead(back) == HIGH;
  return backDetected;
}
