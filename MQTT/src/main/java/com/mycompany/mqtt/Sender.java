/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mqtt;




import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPOutputStream;
import com.google.gson.Gson;

/**
 *
 * @author nagkim
 */


public class Sender {

    public static void main(String[] args) {
        String brokerAddress = "tcp://10.37.129.2:61616";
        String username = "admin";
        String password = "admin";
        String topicName = "test/mqtt";
        int arraySize = 10;

        MqttClient client;
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());

        try {
            client = new MqttClient(brokerAddress, MqttClient.generateClientId(), new MemoryPersistence());
            client.connect(connOpts);

            // Create a JSON representation of your data
            Map<String, Object> jsonMessage = new HashMap<>();
            jsonMessage.put("intArray", new int[arraySize]);
            jsonMessage.put("floatArray", new float[arraySize]);
            jsonMessage.put("stringArray", new String[arraySize]);

            // Fill the int array with values from 1 to arraySize
            for (int i = 0; i < arraySize; i++) {
                ((int[]) jsonMessage.get("intArray"))[i] = i + 1;
            }

            // Fill the float array with values from 1 to arraySize
            for (int i = 0; i < arraySize; i++) {
                ((float[]) jsonMessage.get("floatArray"))[i] = i + 1;
            }

            // Fill the string array with random strings
            Random rand = new Random();
            for (int i = 0; i < arraySize; i++) {
                String randomString = generateRandomString(10, rand);
                ((String[]) jsonMessage.get("stringArray"))[i] = randomString;
            }

            // Marshal the JSON data into bytes
            String jsonData = new Gson().toJson(jsonMessage);
            byte[] jsonBytes = jsonData.getBytes();

            // Gzip compress the JSON data
            byte[] compressedData = compressData(jsonBytes);

            // Publish the zipped data to the MQTT topic
            MqttMessage message = new MqttMessage(compressedData);
            message.setQos(0);
            client.publish(topicName, message);

            System.out.println("JSON data sent");

            // Sleep to allow time for the message to be delivered
            Thread.sleep(2000);

            client.disconnect();
        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String generateRandomString(int length, Random rand) {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return result.toString();
    }

    private static byte[] compressData(byte[] data) {
        ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(compressedData)) {
            gzipOut.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compressedData.toByteArray();
    }
}