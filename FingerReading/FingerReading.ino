// Constant que guardarem
#define MAXFINGERS 1
#define DELAYS 14

// Mes endavant es podran canviar els pins dels dits a voler.
int fingers[4] = {5, 3, 4, 5};
int readings[4];

boolean touched[MAXFINGERS];
int touchedTimes[MAXFINGERS];
boolean released[MAXFINGERS];
int cyclesForTouched = 3;
int codeBefore = 0;
int buttonsTouched = 0;
int buttonsReleased = 0;
int buttonsReleasedBefore = 0;

// Configuracio del programa:
// Mode entrada a tots els dits
// Totes les entrades resetejades
// Sortida per el Serial per debuguejar

void setup() {
  int i = 0;
  for (i = 0; i < MAXFINGERS; i++) {
    pinMode(fingers[i], INPUT);
    readings[i] = 0;
  }
  Serial.begin(115200);
}

// Fins aqui hem plantejat l"estructura del programa

void loop() {

  // Mostrejam els dits

  int i = 0;
  for (i = 0; i < MAXFINGERS; i++) {
    readings[i] = digitalRead(fingers[i]);
  }
  delay(DELAYS);

  // Seguent pas: Esclarir que passa
  /*   Un cicle ha de permetre un mostreig d"un gest.
        Per tant ha de ser mes rapid que tocar-se els dits dues vegades almenys.
  */
  int code = 0;
  for (i = 0; i < MAXFINGERS; i++) {
    code += readings[i] * pow(2, i);
  }

  // Serial.print(touchedOnce); Serial.print(released); Serial.print(code, DEC);
  // Serial.println(" ");
  if (code != 0 && codeBefore != code) {
    for(i = 0; i < MAXFINGERS; i++){
      touched[i] = readings[i] == 1;
      released[i] = readings[i] != 1;
      buttonsReleased = readings[i] * pow(2, i);
    }
    buttonsTouched = code;
  }
  
  if (code == 0) {
    for(i = 0; i < MAXFINGERS; i++){
      released[i] = true;
      touched[i] = false;
    }
    buttonsReleased = 0;
  }
  
  if (buttonsTouched != 0 && buttonsReleased != buttonsReleasedBefore) {
    Serial.print(buttonsTouched, DEC);
    switch (buttonsTouched) {
      case 0: Serial.println("Not touched"); break;
      case 1: Serial.println("Touched One"); break;
      case 2: Serial.println("Touched Two"); break;
      case 3: Serial.println("Both touched"); break;
    }
    buttonsTouched = 0;
  }
  
  codeBefore = code; 
  buttonsReleasedBefore = buttonsReleased;
  
  delay(DELAYS);
}
