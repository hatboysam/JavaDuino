package com.habosa.javaduino;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class Main {

    public static void main(String[] args) {
        // Open the connection
        ArduinoConnection ac = new ArduinoConnection();
        boolean connected = ac.connectToBoard();

        if (connected) {
            System.out.println("Connected!");
        } else {
            System.out.println("Could not connect to Arduino :-(");
            return;
        }

        // Add listener for Arduino serial output
        ac.addListener(new SampleListener(ac));

        // Write a message to the Arduino over Serial
        ac.sendString("Hello, Arduino!");

        // Make sure to call this!
        ac.close();
    }

    private static class SampleListener implements SerialPortEventListener {

        private ArduinoConnection ac;

        public SampleListener(ArduinoConnection ac) {
            this.ac = ac;
        }

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                String inLine = ac.readLine();
                System.out.println("GOT: " + inLine);
            }
        }
    }
}
