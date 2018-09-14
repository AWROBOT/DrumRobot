#include <Servo.h>
#include <SoftwareSerial.h>

SoftwareSerial BTSerial(12, 13); // RX | TX

Servo servo1; 
Servo servo2;

bool inited = 0;

void setup() 
{ 
  Serial.begin(9600);

  Serial.println("start");

  BTSerial.begin(9600);

  BTSerial.println("btstart");
}

void loop() 
{
  if(BTSerial.available() > 0)
  {
    char data =  BTSerial.read();

    Serial.write(data);

    parseData(data);
  }

  if(Serial.available() > 0)
  {
    char data =  Serial.read();

    Serial.write(data);

    parseData(data);
  }
}

void parseData(char data)
{
    switch (data)
    {
        case 'I':
            Serial.println("init");
            if(!inited)
            {
              servo1.attach(4);
              servo2.attach(5);

              servo1.write(110);
              servo2.write(55);
            }
            inited = 1;
            break;

       case 'L':
            Serial.println("left");
            servo1.write(150);
            delay(80);
            servo1.write(110);
            break;

       case 'R':
            Serial.println("right");
            servo2.write(20);
            delay(80);
            servo2.write(55);
            break;

       case 'B':
            Serial.println("both");
            servo1.write(150);
            servo2.write(25);
            delay(80);
            servo1.write(110);
            servo2.write(55);
            break;
    }
}
