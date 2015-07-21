// Constant que guardarem
#define MAXFINGERS          4
#define MAXGESTURES         6
#define THRESHOLD           575
#define DELAYS              15
#define DEBUGDELAYS         3
#define CYCLESCLICKLOW      2
#define CYCLESCLICKHIGH     40
#define CYCLESZEROLOW       0
#define CYCLESZEROHIGH      30
#define CYCLESRECOGNIZING   60
#define CODECLICK           100
#define CODELONGCLICK       200

// The integers in this array corresponds to the number pins of the shield is
// used for the project.
static const uint8_t analogPins[] = {A2, A3, A4, A5};

int readings[MAXFINGERS];
int releases[MAXFINGERS];
int zerosCounter = 0;

int code = 0;
int codeBefore = 0;

int cycleRecognizing = 0;
boolean recognizing = false;
boolean firstTime = true;

int gestureArray[MAXGESTURES];
int indexArray = 0;

int clickCycles = 0;

boolean singleClick = false;
boolean longClick = false;

boolean connected = false;

boolean debug = true;

// Program Config:
// All pin Inputs for detecting digitalChanges.
// All values of readings reset.
// Serial init for debugging.
void setup() {
    int i = 0;
    for (i = 0; i < MAXFINGERS; i++) {
        pinMode(analogPins[i], INPUT);
        readings[i] = 0;
    }
    for(i = 0; i < MAXGESTURES; i++){
        gestureArray[i] = 0;
    }
    Serial.begin(115200);
    cycleRecognizing = 0;
    recognizing = false;
    digitalWrite(13, HIGH);
    delay(1000);
    digitalWrite(13, LOW);
    waitToConnect();
}

void loop() {
    // Sampling the fingers touching now in this method.
    // And calculatig the touch code in this cycle.
    code = sampleFingers();

    // Seguent pas: Esclarir que passa
    /*   Un cicle ha de permetre un mostreig d"un gest.
    Per tant ha de ser mes rapid que tocar-se els dits dues vegades almenys.
    */
    //calculateCode();
    //Serial.print(code); Serial.print("  ->  "); Serial.println(codeBefore);

    // signaling if it is recognizing a gesture or not.
    startRecognizing();

    // Beggining the Gesture Recognition
    // isRecognizing only checks the flag recognizing.
    if(isRecognizing()){
        // RECOGNIZING AREA

        // Start Recognizing a Gesture
        gestureRecognition();


        // CONTROL RECOGNIZE AREA
        if(isTimeToRecognizePast()){
            sendCommand();
            resetRecognition();
            // The command to send must be:
            // _|---|_|---|_ coded in an array like
            //  2XX 1XX 2XX
        }
        else countRecognition();
    }

    codeBefore = code;
    if(debug) delay(DEBUGDELAYS);
    // FIN ---------------------------------------------------------------------

}

/**
*   Method that is sampling the fingers using the digitalRead() method in
*   Arduino code.
*   Later is delaying DELAYS cycles to stabilize the values in the array.
*/
int sampleFingers(){
    int i = 0, code = 0;
    for (i = 0; i < MAXFINGERS; i++) {
        releases[i] = readings[i];
        bool lectureAnalog = analogRead(analogPins[i]) < THRESHOLD;
        readings[i] = lectureAnalog * (readings[i]+1);
        if(readings[i] > 0) code += round(pow(2, i));

        //if(code > 0) {
        //  Serial.print(pow(2, i)); Serial.print(" "); Serial.print(code); Serial.println();
        //}
    }
    delay(DELAYS);
    if(code == 0) zerosCounter++;
    return code;
}

int calculateCode(){
    int i = 0, code = 0;
    for (i = 0; i < MAXFINGERS; i++) {
        if(readings[i] > 0) code += pow(2, i);
    }
    if(code == 0) zerosCounter++;
    return code;
}

int gestureRecognition(){
    if(code != codeBefore){
        int codeToSend = 0;

        // SAVE THE LAST GESTURE
        if(codeBefore == 0){
            //SAVE a 0 interval.
            codeToSend = calculateDurationZeros(zerosCounter);
        }
        else{
            int maxCounter = 0, i = 0;
            for(i = 0; i < MAXFINGERS; i++){
                if(releases[i] > maxCounter) maxCounter = releases[i];
            }
            codeToSend = calculateDuration(maxCounter) + codeBefore;
        }

        // ADD IT TO THE ARRAY
        if(!firstTime && codeToSend > 0){
            arrayRecognitionAdd(codeToSend);
            Serial.println("Code To Send");
            Serial.println(codeToSend);
            resetCounters();
        }
        else firstTime = false;
    }
}

void startRecognizing(){
    if(code != codeBefore || cycleRecognizing > 0) recognizing = true;
    if(code != 0) cycleRecognizing = CYCLESRECOGNIZING;
}

boolean isRecognizing(){
    return recognizing;
}

boolean isTimeToRecognizePast(){
    return cycleRecognizing == 0;
}

void countRecognition(){
    if(cycleRecognizing > 0) cycleRecognizing--;
}

void resetRecognition(){
    recognizing = false;
    firstTime = true;
    resetCounters();
    resetArray();
}

void arrayRecognitionAdd(int codeToSend){
    gestureArray[indexArray] = codeToSend;
    indexArray++;
}

void sendCommand(){
    if(!isErronousCode()){
      Serial.print("Sending command...  ->   ");
      int i = 0;
      for(i = 0; i < indexArray; i++){
          Serial.print(gestureArray[i]); Serial.print(" ");
      }
      Serial.println(" ");
      digitalWrite(13, HIGH);
    delay(1000);
    digitalWrite(13, LOW);
  }
  else{
      Serial.println("NOT Sending ");
      //BLINK SOMETHING TO ADVICE USER
      //OR DO NOTHING...
  }
}

int calculateDurationZeros(int durationCycles){
    int codeToSend = 0;
    if(durationCycles > CYCLESZEROLOW && durationCycles < CYCLESZEROHIGH) codeToSend = CODECLICK;
    else if(durationCycles > CYCLESZEROHIGH) codeToSend = CODELONGCLICK;
    return codeToSend;
}

int calculateDuration(int durationCycles){
    int codeToSend = -CODELONGCLICK;
    if(durationCycles < CYCLESCLICKLOW) codeToSend = -CODELONGCLICK;
    else if(durationCycles > CYCLESCLICKLOW && durationCycles < CYCLESCLICKHIGH) codeToSend = CODECLICK;
    else if(durationCycles > CYCLESCLICKHIGH) codeToSend = CODELONGCLICK;
    return codeToSend;
}

void resetCounters(){
  int i = 0;
  for(i = 0; i < MAXFINGERS; i++){
    releases[i] = 0;
  }
  zerosCounter = 0;
}

void resetArray(){
    int i = 0;
    for(i = 0; i < indexArray; i++){
        gestureArray[i] = 0;
    }
    indexArray = 0;
}

boolean isErronousCode(){
    boolean result = false;
    if(gestureArray[0] % 100 == 0){
        result = true;
    }
    int i = 0;
    for(i = 0; i < indexArray - 1; i++){
        if(gestureArray[i] % 100 == 0){
            if(gestureArray[i+1] % 100 == 0) result = true;
        }
    }
    if(indexArray == 0) result = true;

    return result;
}

void configureBLE(){
    // We need to change some settings, first, to make this central mode thing
    //  work like we want.

    // When ACON is ON, the BC118 will connect to the first BC118 it discovers,
    //  whether you want it to or not. We'll disable that.
    blemate.stdSetParam("ACON", "OFF");
    // When CCON is ON, the BC118 will immediately start doing something after
    //  it disconnects. In central mode, it immediately starts scanning, and
    //  in peripheral mode, it immediately starts advertising. We don't want it
    //  to scan without our permission, so let's disable that.
    blemate.stdSetParam("CCON", "OFF");
    // Turn off advertising. You actually need to do this, or the presence of
    //  the advertising flag can confuse the firmware when the module is in
    //  central mode.
    blemate.BLENoAdvertise();
    // Put the module in central mode.
    blemate.BLECentral();
    // Store these changes.
    blemate.writeConfig();
    // Reset the module. Write-reset is important here!!!!!!
    blemate.reset();

    // The module is now configured to connect to another external device.
}

void waitToConnect(){
    while(!connected)
}
