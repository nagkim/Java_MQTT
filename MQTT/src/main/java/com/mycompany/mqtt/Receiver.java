/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author nagkim
 */
public class Receiver {

    public static void main(String[] args) {
        String brokerAddress = "tcp://10.37.129.2:61616";
        String username = "admin";
        String password = "admin";
        String topicName = "test/mqtt";

        MqttClient client;
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());

        try {
            client = new MqttClient(brokerAddress, MqttClient.generateClientId(), new MemoryPersistence());
            client.connect(connOpts);

            // Subscribe to the MQTT topic
            client.subscribe(topicName);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // Handle the received message here
                    byte[] compressedData = message.getPayload();

                    // Gzip decompression
                    try ( ByteArrayInputStream compressedStream = new ByteArrayInputStream(compressedData);  GZIPInputStream gzipStream = new GZIPInputStream(compressedStream)) {

                        byte[] decompressedData = new byte[1024];
                        int length;
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((length = gzipStream.read(decompressedData)) > 0) {
                            stringBuilder.append(new String(decompressedData, 0, length));
                        }

                        // Process the decompressed JSON data
                        String jsonMessage = stringBuilder.toString();
                        System.out.println("Received JSON Message:");
                        System.out.println(jsonMessage);

                        // You can parse and handle the JSON data as needed
                        // Example: Parse JSON using Gson library
                        // Gson gson = new Gson();
                        // YourDataClass data = gson.fromJson(jsonMessage, YourDataClass.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // This method is called when the message has been delivered (QoS 1 or 2).
                    // You can handle it as needed.
                }
            });

            System.out.println("Waiting for MQTT messages.");

            // Keep the receiver running indefinitely
            while (true) {
                Thread.sleep(1000);
            }
        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
