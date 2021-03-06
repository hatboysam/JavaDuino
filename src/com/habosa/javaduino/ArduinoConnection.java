package com.habosa.javaduino;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;

import java.io.*;
import java.util.Enumeration;
import java.util.TooManyListenersException;

/**
 * Manage connection to Arduino microcontroller.
 *
 * Adapted/copied from http://www.drdobbs.com/jvm/control-an-arduino-from-java/240163864
 */
public class ArduinoConnection {

    private static final String MAC_PORT_NAME_MOD = "/dev/tty.usbmodem";
    private static final String MAC_PORT_NAME_SER = "/dev/tty.usbserial";
    private static final String WIN_PORT_NAME = "COM3";
    private static final String LINUX_PORT_NAME_DEV = "/dev/usbdev";
    private static final String LINUX_PORT_NAME_TTY = "/dev/tty";
    private static final String LINUX_PORT_NAME_SER = "/dev/serial";
    private static final String[] PORT_NAMES = new String[]{
            MAC_PORT_NAME_MOD,
            MAC_PORT_NAME_SER,
            WIN_PORT_NAME,
            LINUX_PORT_NAME_DEV,
            LINUX_PORT_NAME_SER,
            LINUX_PORT_NAME_TTY
    };
    private static final String APP_NAME = ArduinoConnection.class.getSimpleName();

    private static final int TIME_OUT = 1000;
    private static final int DATA_RATE = 9600;

    private SerialPort serialPort;

    private InputStream arduinoInput;
    private OutputStream arduinoOutput;

    private BufferedReader inputReader;

    public ArduinoConnection() {
        // Nothing to initialize
    }

    public boolean connectToBoard() {
        try {
            CommPortIdentifier portId = null;
            Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

            while ((portId == null) && (portEnum.hasMoreElements())) {
                CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                for (String portName : PORT_NAMES) {
                    if (currPortId.getName().equals(portName) || currPortId.getName().startsWith(portName)) {
                        System.out.println("Attempting to Open");
                        // Open serial port
                        serialPort = (SerialPort) currPortId.open(APP_NAME, TIME_OUT);
                        portId = currPortId;
                        System.out.println( "Connected on port" + currPortId.getName() );
                    }
                }
            }

            if (portId == null || serialPort == null) {
                System.out.println("Error: Could not connect to Arduino");
                return false;
            }

            serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            // Give the Arduino some time
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                // Do Nothing
            }

            // Get streams
            arduinoInput = serialPort.getInputStream();
            arduinoOutput = serialPort.getOutputStream();

            // Make reader
            inputReader = new BufferedReader(new InputStreamReader(arduinoInput));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fail
        return false;
    }

    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    public void addListener(SerialPortEventListener serialPortEventListener) {
        try {
            serialPort.addEventListener(serialPortEventListener);
            serialPort.notifyOnDataAvailable(true);
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
    }

    public void sendString(String msg) {
        try {
            //outputWriter.write(msg.toCharArray());
            arduinoOutput.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readLine() {
        try {
            return inputReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
