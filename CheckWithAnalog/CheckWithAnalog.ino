int threshold = 500;

int key1, key2, key3;

void setup() {
  // put your setup code here, to run once:
  pinMode(A5, INPUT);
  pinMode(A2, INPUT);
  pinMode(A3, INPUT);
  pinMode(13, OUTPUT);
  Serial.begin(115200);
  
}

void loop() {
  // put your main code here, to run repeatedly:

  key1 = analogRead(5);
  //key2 = analogRead(A2);
  //key3 = analogRead(A3);
  
  Serial.print(key1); Serial.print(" ");
//  Serial.print(key2); Serial.print(" ");
//  Serial.print(key3); Serial.print(" ");
  Serial.println();
  if(key1 < threshold){
    digitalWrite(13, HIGH);
  }
  else digitalWrite(13, LOW);
  delay(100);
}

void blink(int pin, int number){
  for(int i = number; i > 0; i--){
    digitalWrite(pin, HIGH);
    delay(100);
    digitalWrite(pin, LOW);
    delay(100);
  }
}

