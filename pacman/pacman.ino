// LLINDAR, VARIA AMB EL MATERIAL 
int llindar = 500;

// CODIS DE TECLAs

//int UP_CODE = 0x41;
int UP_CODE = 0xDA;
int RIGHT_CODE = 0xD7;
int LEFT_CODE = 0xD8;
int DOWN_CODE = 0xD9;

// PORTS ANALOGICS
int UP_PORT = 0;
int DOWN_PORT = 1;
int LEFT_PORT = 2;
int RIGHT_PORT = 3;

// INICI DEL PROGRAMA
void setup() {
  delay(3000);  
}

// BUCLE INFINIT
void loop () {
  
   // LECTURA DELS VALOR ACTUALS DELS PORTS ANALOGICS
   int UP = analogRead(UP_PORT);
   int DOWN = analogRead(DOWN_PORT);
   int LEFT = analogRead(LEFT_PORT);
   int RIGHT = analogRead(RIGHT_PORT);
   
   // BOTO FLETXA AMUNT
   if (UP < llindar) Keyboard.write(UP_CODE);
   
   // BOTO FLETXA ABAIX
   if (DOWN < llindar) Keyboard.write(DOWN_CODE);
   
   // BOTO FLETXA ESQUERRA
   if (LEFT < llindar) Keyboard.write(LEFT_CODE);

   // BOTO FLETXA DRETA
   if (RIGHT < llindar) Keyboard.write(RIGHT_CODE);
     
   // ESPERA
   delay(100);
 
   // Comprovació Resistències
//   Serial.println(UP);
//   Serial.println(DOWN);
//   Serial.println(LEFT);
//   Serial.println(RIGHT);
}
