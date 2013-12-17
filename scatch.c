#include <Servo.h> 
#define MINA 10
#define MAXA 125
#define MAXB 140
#define INTERV 12
#define UP_INTERV 500

Servo myservo;  
int pos = MINA; 

void setup() {
  Serial.begin(9600);
   myservo.attach(9);
   myservo.write(pos);
}

void loop() {
  if (Serial.available() > 0) {
    int inByte = Serial.read();
    switch (inByte) {
    case 'h':
      Serial.println("Hello");
      break;
    case 'c':    
      move(MINA, MAXA);
      myservo.write(MAXB);
      delay(UP_INTERV);
      move(MAXB, MINA);
      break;
    case 'r':
      pos = MINA;
      myservo.write(pos);
      break;
    default:
      Serial.println("Unknown command");      
    }
  }
}

void move(int a, int b) 
{ 
  if (a < b) {
    for(pos = a; pos < b; pos += 1) {
      myservo.write(pos);
      delay(INTERV);
    }
  } else {
    for(pos = a; pos>=b; pos-=1) {                                
      myservo.write(pos); 
      delay(INTERV); 
    }
  }
}
