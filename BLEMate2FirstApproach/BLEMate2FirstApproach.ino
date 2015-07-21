#include <SparkFunBLEMate2.h>

BLEMate2 blemate(&Serial);

int controlIsHere = 13;

void setup() {
  // put your setup code here, to run once:
  pinMode(controlIsHere, OUTPUT);
  Serial.begin(9600);
  digitalWrite(controlIsHere, HIGH);

  if (blemate.reset() != BLEMate2::SUCCESS)
  {
    selectPC();
    Serial.println("Module reset error!");
    debugTimes(15);
    while (1);
  }
  debugTimes(2);

  // restore() resets the module to factory defaults; you'll need to perform
  //  a writeConfig() and reset() to make those settings take effect. We don't
  //  do that automatically because there may be things the user wants to
  //  change before committing the settings to non-volatile memory and
  //  resetting.
  if (blemate.restore() != BLEMate2::SUCCESS)
  {
    selectPC();
    Serial.println("Module restore error!");
    debugTimes(3);
    while (1);
  }
  debugTimes(2);


  // writeConfig() stores the current settings in non-volatile memory, so they
  //  will be in place on the next reboot of the module. Note that some, but
  //  not all, settings changes require a reboot. It's probably in general best
  //  to write/reset when changing anything.
  if (blemate.writeConfig() != BLEMate2::SUCCESS)
  {
    selectPC();
    Serial.println("Module write config error!");
    debugTimes(5);
    while (1);
  }
  debugTimes(2);
  // One more reset, to make the changes take effect.
  if (blemate.reset() != BLEMate2::SUCCESS)
  {
    selectPC();
    Serial.println("Second module reset error!");
    debugTimes(7);
    while (1);
  }
  debugTimes(2);

  selectBLE();

  setUpCentralExample();
}

void loop() {
  // put your main code here, to run repeatedly:
  // doCentralExample();
  putInAdvertiseMode();
}

void setUpCentralExample()
{
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

void doCentralExample()
{
  // We're going to tstart with an assumption of module error. That way, we
  //  can easily check against the result while we're iterating.
  BLEMate2::opResult result = BLEMate2::MODULE_ERROR;
  // This while loop will continue to scan the world for addresses until it
  //  finds some. Why? Why not?
  while(1)
  {
    debugTimes(14);
    selectBLE();
    result = blemate.BLEScan(2);
    if (result == BLEMate2::SUCCESS)
    {
      selectPC();
      Serial.println("Success!");
      break;
    }
    else if (result == BLEMate2::REMOTE_ERROR)
    {
      selectPC();
      Serial.println("Remote error!");
    }
    else if (result == BLEMate2::MODULE_ERROR)
    {
      selectPC();
      Serial.println("Module error! Everybody panic!");
    }
  }

  byte numAddressesFound = blemate.numAddresses();

  // BC118Address is where we'll store the index of the first BC118 device we
  //  find. We'll know it because the address will start with "20FABB". By
  //  starting at 10, we know when we've found something b/c it'll be 4 or less.
  byte BC118Address = 0;
  String address;

  selectPC();
  Serial.print("We found ");
  Serial.print(numAddressesFound);
  Serial.println(" BLE devices!");
  // We're going to iterate over numAddressesFound, print each address, and
  //  check to see if each one belongs to a BC118. The first BC118 we find,
  //  we'll connect to, but only after we report our address list.
  for (byte i = 0; i < numAddressesFound; i++)
  {
    blemate.getAddress(i, address);
    Serial.println("Found address: " + address);
    if (address.startsWith("20FABB"))
    {
      BC118Address = i;
    }
  }
  selectBLE();
  blemate.connect(address);
  blemate.sendData("Hello world! I can see my house from here! Whee!");
  blemate.disconnect();
  delay(500);
  selectPC();
  Serial.println("The End!");
  while(1);
}

void selectPC(){
  digitalWrite(controlIsHere, LOW);
  Serial.flush();
}

void selectBLE(){
  digitalWrite(controlIsHere, HIGH);
  Serial.flush();
}

void putInAdvertiseMode(){
  blemate.BLEAdvertise();
  blemate.reset();
  debugTimes(10);
  int connectedIntents = 0;
  BLEMate2::status stat = BLEMate2::INIT;
  BLEMate2::opResult result = BLEMate2::MODULE_ERROR;
  while(1){
      result = blemate.BLEStatus(stat);
      if(result == BLEMate2::TIMEOUT_ERROR && stat == 4){
          blemate.sendData(""+connectedIntents);
          connectedIntents++;
      }
      else{
          debugTimes(10);
          blemate.BLEAdvertise();
          blemate.reset();
      }
      delay(5000);
  }
}

void debugTimes(int times){
  int i = 0;
  for(i = 0; i < times; i++){
    selectBLE();
    delay(300);
    selectPC();
    delay(300);
  }
  delay(1000);
}
