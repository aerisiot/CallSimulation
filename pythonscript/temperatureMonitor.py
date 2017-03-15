import urllib2, base64
import requests
import RPi.GPIO as GPIO
import dht11
from AWSIoTPythonSDK.MQTTLib import AWSIoTMQTTClient
from time import sleep
from datetime import date, datetime
 
# initialize GPIO
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
GPIO.cleanup()
 
# AWS IoT certificate based connection
myMQTTClient = AWSIoTMQTTClient("123afhlss456")
#The broker address can be found in the AWS IoT console under Settings tab 
myMQTTClient.configureEndpoint("<AWS_BrokerAddress>", 8883)
#Specify the path of your certificate files here
myMQTTClient.configureCredentials("/home/pi/cert/CA.pem", "/home/pi/cert/fde0826e9d-private.pem.key", "/home/pi/cert/fde0826e9d-certificate.pem.crt")
myMQTTClient.configureOfflinePublishQueueing(-1)  # Infinite offline Publish queueing
myMQTTClient.configureDrainingFrequency(2)  # Draining: 2 Hz
myMQTTClient.configureConnectDisconnectTimeout(10)  # 10 sec
myMQTTClient.configureMQTTOperationTimeout(5)  # 5 sec
 
#connect and publish
myMQTTClient.connect()
myMQTTClient.publish("temperature/info", "connected", 0)
 
#loop and publish sensor reading
while 1:
    now = datetime.utcnow()
    now_str = now.strftime('%Y-%m-%dT%H:%M:%SZ') #e.g. 2016-04-18T06:12:25.877Z
    instance = dht11.DHT11(pin = 4) #BCM GPIO04
    result = instance.read()
    if result.is_valid():
        payload = '{ "timestamp": "' + now_str + '","temperature": ' + str(result.temperature) + ',"humidity": '+ str(result.humidity) + ' }'
        print payload
        myMQTTClient.publish("temperature/data", payload, 0)
        if result.temperature > 20:
            data = '{"param":"' + str(result.temperature) + '"}'
			#The endpoint of the Rest API to notify on
            url = 'http://<hostname>:<port>/call.simulation/temperature/v1/notify'
            req = urllib2.Request(url, data, {'Content-Type':'application/json'})
            f = urllib2.urlopen(req)
        sleep(4)
    else:
        print (".")
        sleep(1)
