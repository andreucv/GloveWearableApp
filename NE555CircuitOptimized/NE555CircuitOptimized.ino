#include <SparkFunBLEMate2.h>

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
int pairingGesture[] = {212, 100, 212};
int indexArray = 0;

int clickCycles = 0;

boolean singleClick = false;
boolean longClick = false;

BLEMate2 blemate(&Serial);
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
    Serial.begin(9600);
    cycleRecognizing = 0;
    recognizing = false;
    digitalWrite(13, HIGH);
    delay(1000);
    digitalWrite(13, LOW);
    blemate.reset();
    configureBLE(false);
    //putInIdleMode();
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
            if(connected){
                sendCommand();
                resetRecognition();
            }
            else{
                if(gestureAdvertise){
                    advertiseBLEtoPair();
                    debugTimes(5);
                    resetRecognition();
                }
                checkConnectedBLE();
            }
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
    //   Serial.print("Sending command...  ->   ");
    //   int i = 0;
    //   for(i = 0; i < indexArray; i++){
    //       Serial.print(gestureArray[i]); Serial.print(" ");
    //   }
    //   Serial.println(" ");
      digitalWrite(13, HIGH);
      delay(1000);
      digitalWrite(13, LOW);
      if(!connected && gestureAdvertise()) advertiseBLEtoPair();
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

void configureBLE(boolean advertise){
    boolean inCentralMode = false;
    // A word here on amCentral: amCentral's parameter is passed by reference, so
    //  the answer to the question "am I in central mode" is handed back as the
    //  value in the boolean passed to it when it is called. The reason for this
    //  is the allow the user to check the return value and determine if a module
    //  error occurred: should I trust the answer or is there something larger
    //  wrong than merely being in the wrong mode?
    blemate.amCentral(inCentralMode);
    if (inCentralMode)
    {
      blemate.BLEPeripheral();
      //blemate.BLEAdvertise();
    }

    // There are a few more advance settings we'll probably, but not definitely,
    //  want to tweak before we reset the device.

    // The CCON parameter will enable advertising immediately after a disconnect.
    blemate.stdSetParam("CCON", "OFF");
    // The ADVP parameter controls the advertising rate. Can be FAST or SLOW...
    blemate.stdSetParam("ADVP", "FAST");
    // The ADVT parameter controls the timeout before advertising stops. Can be
    //  0 (for never) to 4260 (71min); integer value, in seconds.
    blemate.stdSetParam("ADVT", "0");
    // The ADDR parameter controls the devices we'll allow to connect to us.
    //  All zeroes is "anyone".
    blemate.stdSetParam("ADDR", "000000000000");

    blemate.writeConfig();
    blemate.reset();

    // We're set up to allow anything to connect to us now.
}

void startAdvertise(boolean fast, int time){
    // The ADVP parameter controls the advertising rate. Can be FAST or SLOW...
    if(fast) blemate.stdSetParam("ADVP", "FAST");
    else blemate.stdSetParam("ADVP", "SLOW");
    // The ADVT parameter controls the timeout before advertising stops. Can be
    //  0 (for never) to 4260 (71min); integer value, in seconds.
    blemate.stdSetParam("ADVT", ""+time);
    blemate.BLEAdvertise();
}

bool gestureAdvertise(){
    for(int i = 0; i < MAXGESTURES; i++){
        if(pairingGesture[i] != gestureArray[i]){
            return false;
        }
    }
    return true;
}

void putInIdleMode(){
//     blemate.stdSetParam("WLVL", "LOW");
//     blemate.stdSetParam("WAKE", "ON");
//     blemate.stdSetParam("SLEEP", "ON");
//     blemate.writeConfig();
//     blemate.reset();
}

void advertiseBLEtoPair(){
    blemate.BLEAdvertise();
}

void checkConnectedBLE(){
    BLEMate2::opResult result = blemate.isConnected();
    if(result == BLEMate2::CONNECTED) connected = true;
    else connected = false;
}

void debugTimes(int times){
  int i = 0;
  for(i = 0; i < times; i++){
    digitalWrite(13, HIGH);
    delay(300);
    digitalWrite(13, LOW);
    delay(300);
  }
  delay(1000);
}
