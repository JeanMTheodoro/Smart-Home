/*
    This sketch demonstrates how to set up a simple HTTP-like server.
    The server will set a GPIO pin depending on the request
      http://server_ip/gpio/0 will set the GPIO2 low,
      http://server_ip/gpio/1 will set the GPIO2 high
    server_ip is the IP address of the ESP8266 module, will be
    printed to Serial when the module is connected.
*/

/*#include "lwip/tcp_impl.h"

void tcpCleanup(){
  while(tcp_tw_pcbs != null){
    tcp_abort(tcp_tw_pcbs);
  }
  
}*/

#include <ESP8266WiFi.h>


#ifndef STASSID
#define STASSID "your-ssid"
#define STAPSK  "your-password"
#define sensorGas 0
#define sensorFire D2
#define cozinha 15
#define sala 14
#define meuQuarto 13
#define wc 12
#endif



IPAddress ip(192,168,15,150);
IPAddress gateway(192,168,15,1);
IPAddress subnet(255,255,255,0);
IPAddress dns(192,168,15,1);

const char* ssid = "VIVO-AD00";
const char* password = "14010910";

// Create an instance of the server
// specify the port to listen on as an argument
WiFiServer server(80);


void setup() {
  Serial.begin(9600);

  pinMode(sensorGas,INPUT);
  // prepare LED
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, 0);
  pinMode(sala, OUTPUT);
  pinMode(meuQuarto, OUTPUT);
  pinMode(wc, OUTPUT);
  pinMode(cozinha, OUTPUT);
   pinMode(sensorFire,INPUT);

  digitalWrite(sala, LOW);
  digitalWrite(meuQuarto, LOW);
  digitalWrite(wc, LOW);
  digitalWrite(cozinha,LOW);


  // Connect to WiFi network
  Serial.println();
  Serial.println();
  Serial.print(F("Connecting to "));
  Serial.println(ssid);


  WiFi.config(ip,dns,gateway, subnet);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(F("."));
  }
  Serial.println();
  Serial.println(F("WiFi connected"));

  // Start the server
  server.begin();
  Serial.println(F("Server started"));

  // Print the IP address
  Serial.println(WiFi.localIP());
}

void loop() {
  
    
  // Check if a client has connected
  WiFiClient client = server.available();
  if (!client) {
    return;
  }
  Serial.println(F("new client"));

  client.setTimeout(800); // default is 1000

  // Read the first line of the request
  String request = client.readStringUntil('\r');
  /*Serial.println(F("request: "));
  Serial.println(request);*/
  client.flush();


 
  if(request.indexOf("gpio/on") != -1) digitalWrite(meuQuarto, 1);
  else if(request.indexOf("gpio/off") > 0) digitalWrite(meuQuarto,0);

 if(request.indexOf("gpio1/on") != -1)digitalWrite(sala, 1);
 else if(request.indexOf("gpio1/off") != -1) digitalWrite(sala,0);
 
  if(request.indexOf("gpio2/on") != -1) digitalWrite(cozinha,1);
  else if(request.indexOf("gpio2/off") != -1) digitalWrite(cozinha,0);

   if(request.indexOf("gpio3/on") != -1)digitalWrite(wc,1);
     else if(request.indexOf("gpio3/off") != -1) digitalWrite(wc,0);

  // read/ignore the rest of the request
  // do not client.flush(): it is for output only, see below
  while (client.available()) {
    // byte by byte is not very efficient
    client.read();
  }

  // Send the response to the client
  // it is OK for multiple small client.print/write,
  // because nagle algorithm will group them into one single packet
  client.print("HTTP/1.1 200 OK");
  client.println("Content-Type: text/html");
  client.println("");

  int fire = digitalRead(sensorFire);
  Serial.print("fire: ");
  Serial.println(fire);
  int gas = digitalRead(sensorGas);
  Serial.print("gas: ");
  Serial.println(gas);

  
   if(digitalRead(meuQuarto)){
    client.print("meuQuartoOn"); 
  }else{
    client.print("meuQuartoOff");
  }

   client.print(",");

  if(digitalRead(sala)){
    client.print("salaOn"); 
  }else{
    client.print("salaOff");
  }

  client.print(",");
  
 if(digitalRead(cozinha)){
    client.print("cozinhaOn"); 
  }else{
    client.print("cozinhaOff");
  }

   client.print(",");
  
  if(digitalRead(wc)){
    client.print("wcOn"); 
  }else{
    client.print("wcOff");
  }

  client.print(",");

  if(digitalRead(sensorFire)){
    client.print("fogoOff"); 
  }else{
    client.print("fogoOn");
  }

  client.print(",");

if(digitalRead(sensorGas)){
    client.print("gasOff"); 
  }else{
    client.print("gasOn");
  }

    
    delay(1);

}
