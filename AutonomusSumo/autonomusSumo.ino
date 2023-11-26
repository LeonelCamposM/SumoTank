void setup() {
  Serial.begin(115200);
  SetupStrategy();
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, HIGH);
}

void loop() {
  execute();
  delay(1);
}
