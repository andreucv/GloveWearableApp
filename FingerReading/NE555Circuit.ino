// Constant que guardarem
#define MAXFINGERS          2
#define MAXGESTURES         4
#define DELAYS              14
#define CYCLESFORLONGCLICK  10
#define CYCLESFORCLICK      5
#define CYCLESFORGESTURE    10

// The integers in this array corresponds to the number pins of the shield is
// used for the project.
int fingers[4] = {2, 3, 4, 5};

int readings[MAXFINGERS];
boolean touched[MAXFINGERS];
boolean released[MAXFINGERS];
int touchedTimes[MAXFINGERS];

int cyclesForTouched = 3;

int buttonsTouched = 0;
int buttonsReleased = 0;
int buttonsReleasedBefore = 0;
int buttonsTouchedBefore = 0;

int cycleRecognizing = 0;
boolean isRecognizing = false;

int gestureArray[MAXGESTURES];
int indexArray = 0;

int clickCycles = 0;

boolean singleClick = false;
boolean longClick = false;

// Programm Config:
// All pin Inputs for detecting digitalChanges.
// All values of readings reset.
// Serial init for debugging.
void setup() {
    int i = 0;
    for (i = 0; i < MAXFINGERS; i++) {
        pinMode(fingers[i], INPUT);
        readings[i] = 0;
    }
    for(i = 0; i < MAXGESTURES; i++){
        gestureArray[i] = 0;
    }
    Serial.begin(115200);
    cycleRecognizing = 0;
    isRecognizing = false;
}

void loop() {
    // Sampling the fingers touching now in this method
    sampleFingers();

    // Seguent pas: Esclarir que passa
    /*   Un cicle ha de permetre un mostreig d"un gest.
    Per tant ha de ser mes rapid que tocar-se els dits dues vegades almenys.
    */
    // Calculatig the touch code in this cycleRecognizing.
    int code = 0;
    for (i = 0; i < MAXFINGERS; i++) {
        code += readings[i] * pow(2, i);
    }
    Serial.println("Code = " + code);

    // signaling if it is recognizing a gesture or not.
    startRecognizing(code, cycleRecognizing);

    // Beggining the Gesture Recognition
    if(isRecognizing()){
        // RECOGNIZING AREA

        // StartRecognizing a Gesture
        gestureRecognition(code);


        // CONTROL RECOGNIZE AREA
        if(isTimeToRecognizePast()){
            sendCommand();
            resetRecognition();

            // The command to send must be:
            // _|---|_|---|_ coded in a number or similar (an array)...
        }
        else countRecognition();

    }

    buttonsTouchedBefore = code;

    // FIN ---------------------------------------------------------------------
    /*
    // Serial.print(touchedOnce); Serial.print(released); Serial.print(code, DEC);
    // Serial.println(" ");
    if (code != 0 && buttonsTouchedBefore != code) {
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

    buttonsTouchedBefore = code;
    buttonsReleasedBefore = buttonsReleased;

    delay(DELAYS);
    cicle++;

    if()*/
}

/**
*   Method that is sampling the fingers using the digitalRead() method in
*   Arduino code.
*   Later is delaying DELAYS cycles to stabilize the values in the array.
*/
void sampleFingers(){
    int i = 0;
    for (i = 0; i < MAXFINGERS; i++) {
        readings[i] = digitalRead(fingers[i]);
    }
    delay(DELAYS);
}

int gestureRecognition(int code){
    boolean saveGesture = false;

    // Gesture Recognition per cycle
    switch(code){
        case codeBefore: //LONG CLICK CALLBACK
            clickCycles++;
        break;
        case default: //SAVE LAST GESTURE AND BEGIN NEW GESTURE break;
        case 0: saveGesture = true; break;

    }
    if(clickCycles == CYCLESFORCLICK){
        // WE HAVE A CLICK
        longClick = false;
        singleClick = true;
        Serial.println("We have a click on a finger");
    }
    if(clickCycles > CYCLESFORCLICK){
        // WE HAVE A LONG CLICK
        singleClick = false;
        longClick = true;
        Serial.println("Long click on a finger");
    }

    if(saveGesture){
        if(indexArray < MAXGESTURES){
            gestureArray[indexArray] = code;
            indexArray++;
        }
        Serial.println("Saved Gesture");
    }
    else{
        //RESET array
        indexArray = 0;
    }
}

void startRecognizing(int code, int cycles){
    if(code != 0 || cycles != 0) isRecognizing = true;
    Serial.println("Start Recognizing " + isRecognizing);
}

boolean isRecognizing(){
    return isRecognizing;
}

boolean isTimeToRecognizePast(){
    return cycleRecognizing == CYCLESFORGESTURE;
}

void countRecognition(){
    cycleRecognizing++;
}

void resetRecognition(){
    isRecognizing = false;
    cycleRecognizing = 0;
}

void arrayRecognitionAdd(){

}
