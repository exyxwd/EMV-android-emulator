package de.androidcrypto.android_hce_emulate_a_creditcard;

import static de.androidcrypto.android_hce_emulate_a_creditcard.Utils.bytesToHexNpe;
import static de.androidcrypto.android_hce_emulate_a_creditcard.Utils.concatenateByteArrays;
import static de.androidcrypto.android_hce_emulate_a_creditcard.Utils.hexStringToByteArray;

import android.content.Context;
import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * This service class uses a static approach to serve the data (command and response data) of a
 * sample MasterCard.
 * 
 * The provided data are from an outdated Visa Card that is no longer in use and the card is 
 * blocked by the bank. The data was recorded with the app "Talk to your Credit Card" 
 * available here: https://github.com/AndroidCrypto/TalkToYourCreditCardG8
 * 
 * Please make sure that the AID A0000000041010 is included in the apduservice.xml file or the
 * service will not called by Android, the card will not get selected and the workflow stops.
 * 
 */

public class HceCcEmulationServiceMastercard extends HostApduService {

    private static final String POS_TERMINAL_IP = "192.168.183.87";
    private static final int POS_TERMINAL_PORT = 52346;

    private static final byte[] RESPONSE_OK_SW = hexStringToByteArray("9000");
    // Note: the responses does not include the 0x9000h terminator
    private static final byte[] SELECT_PPSE_COMMAND = hexStringToByteArray("00a404000e325041592e5359532e444446303100");
    //private static final byte[] SELECT_PPSE_RESPONSE = hexStringToByteArray("6f3c840e325041592e5359532e4444463031a52abf0c2761254f07a000000004101050104465626974204d6173746572436172648701019f0a0400010101");
    private static final byte[] SELECT_PPSE_RESPONSE = hexStringToByteArray("6F3C840E325041592E5359532E4444463031A52ABF0C2761254F07A000000004101087010150104465626974204D6173746572636172649F0A0400010101");

    private static final byte[] SELECT_AID_COMMAND = hexStringToByteArray("00a4040007a000000004101000");
    //private static final byte[] SELECT_AID_RESPONSE = hexStringToByteArray("6f528407a0000000041010a54750104465626974204d6173746572436172649f12104465626974204d6173746572436172648701019f1101015f2d046465656ebf0c119f0a04000101019f6e0702800000303000");
    private static final byte[] SELECT_AID_RESPONSE = hexStringToByteArray("6F618407A0000000041010A55650104465626974204D6173746572636172645F2D086875656E646566728701019F1101019F12104465626974204D617374657243617264BF0C1C9F5D030100009F4D020B0A9F6E07034800003030009F0A0400010101");

    // this needs to be dynamic as the response from the real NFC reader is not predictable (e.g. different amounts)
    private static final byte[] GET_PROCESSING_OPTONS_COMMAND_SHORTED = hexStringToByteArray("80a8");
    // private static final byte[] GET_PROCESSING_OPTONS_COMMAND_READER = hexStringToByteArray("80a8000002830000");
    //private static final byte[] GET_PROCESSING_OPTONS_RESPONSE = hexStringToByteArray("771282021980940c080101001001010120010200");
    private static final byte[] GET_PROCESSING_OPTONS_RESPONSE = hexStringToByteArray("771282021981940C080101001001010120010200");

    private static final byte[] READ_FILE_08_01_COMMAND = hexStringToByteArray("00b2010c00");
    //private static final byte[] READ_FILE_08_01_RESPONSE = hexStringToByteArray("70759f6c0200019f6206000000000f009f63060000000000fe563442353337353035303030303136303131305e202f5e323430333232313237393433323930303030303030303030303030303030309f6401029f65020f009f660200fe9f6b135375050000160110d24032210000000000000f9f670102");
    private static final byte[] READ_FILE_08_01_RESPONSE = hexStringToByteArray("70759F6C0200019F62060000000000709F6306000000000F8E563442353332313830343137323633303332355E202F5E323730313232313038303330303734333234383030303030303030303030309F6401059F650200709F66020F8E9F6B135321804172630325D27012210000000000000F9F6701059000");

    private static final byte[] READ_FILE_10_01_COMMAND = hexStringToByteArray("00b2011400");

    private static final byte[] READ_FILE_10_01_RESPONSE = hexStringToByteArray("7081A69F420209785F25032203015F240327015A0853750500001601105F3401009F0702FFC09F080200028C279F02069F03069F1A0295055F2A029A039C019F37049F35019F45029F4C089F34039F21039F7C148D0C910A8A0295059F37049F4C088E0E000000000000000042031E031F039F0D05B4508400009F0E0500000000009F0F05B4708480005F280202809F4A018257135375050000160110D24032212794329000000F");
    // was 7081a69f420209785f25032203015f24032403315a0853750500001601105f3401009f0702ffc09f080200028c279f02069f03069f1a0295055f2a029a039c019f37049f35019f45029f4c089f34039f21039f7c148d0c910a8a0295059f37049f4c088e0e000000000000000042031e031f039f0d05b4508400009f0e0500000000009f0f05b4708480005f280202809f4a018257135375050000160110d24032212794329000000f

    private static final byte[] READ_FILE_20_01_COMMAND = hexStringToByteArray("00b2012400");
    private static final byte[] READ_FILE_20_01_RESPONSE = hexStringToByteArray("7081b49f4681b047461ffca14b5dfdc209569c8a14f17644251aa3f4abea251262134b920982f0250741f96fccb40800293054c0d89824ba7ac44ee7bab06fa157fccf7e52d3c64b4d8acd41b9774b801519ed6fec827ec2ec29f8991167c453776559a4a06fd98c4b9bd1548a65af2f56002a836bdf9a040a9253e653584c92833c3d1aa8e08c4de9cda1026044f80f39a9326a57496598987a6b3e18a5f56a8bdede752870e8793776db9d325ccd9c7ca5db33c28f04");

    private static final byte[] READ_FILE_20_02_COMMAND = hexStringToByteArray("00b2022400");
    private static final byte[] READ_FILE_20_02_RESPONSE = hexStringToByteArray("7081e08f01059f3201039224abfd2ebc115c3796e382be7e9863b92c266ccabc8bd014923024c80563234e8a11710a019081b004cc60769cabe557a9f2d83c7c73f8b177dbf69288e332f151fba10027301bb9a18203ba421bda9c2cc8186b975885523bf6707f287a5e88f0f6cd79a076319c1404fcdd1f4fa011f7219e1bf74e07b25e781d6af017a9404df9fd805b05b76874663ea88515018b2cb6140dc001a998016d28c4af8e49dfcc7d9cee314e72ae0d993b52cae91a5b5c76b0b33e7ac14a7294b59213ca0c50463cfb8b040bb8ac953631b80fa85a698b00228b5ff44223");

//    @Override
//    public byte[] processCommandApdu(byte[] commandApdu, Bundle bundle) {
//        // sendMessageToActivity("------------------------", "----");
//        // sendMessageToActivity("# Mastercard #", bytesToHexNpe(commandApdu));
//        sendToServer(bytesToHexNpe(commandApdu));
//
//        // step 01
//        if (Arrays.equals(commandApdu, SELECT_PPSE_COMMAND)) {
//            // step 01 selecting the PPSE
//            sendMessageToActivity("step 01 Select PPSE Command ", bytesToHexNpe(commandApdu));
//            sendMessageToActivity("step 01 Select PPSE Response", bytesToHexNpe(SELECT_PPSE_RESPONSE));
//            return concatenateByteArrays(SELECT_PPSE_RESPONSE, RESPONSE_OK_SW);
//        }
//
//        // step 02
//        if (Arrays.equals(commandApdu, SELECT_AID_COMMAND)) {
//            // step 02 selecting the AID
//            sendMessageToActivity("step 02 Select AID Command ", bytesToHexNpe(commandApdu));
//            sendMessageToActivity("step 02 Select AID Response", bytesToHexNpe(SELECT_AID_RESPONSE));
//            return concatenateByteArrays(SELECT_AID_RESPONSE, RESPONSE_OK_SW);
//        }
//
//        // step 03
//        // dynamic - just compare the beginning of the commandApdu with the sample
//        byte[] shortedCommandApdu = Arrays.copyOf(commandApdu, 2);
//        if (Arrays.equals(shortedCommandApdu, GET_PROCESSING_OPTONS_COMMAND_SHORTED)) {
//            sendMessageToActivity("step 03 Get Processing Options (GPO) Command", bytesToHexNpe(commandApdu));
//            sendMessageToActivity("step 03 Get Processing Options (GPO) Response", bytesToHexNpe(GET_PROCESSING_OPTONS_RESPONSE));
//            return concatenateByteArrays(GET_PROCESSING_OPTONS_RESPONSE, RESPONSE_OK_SW);
//        }
//
//        // step 04
//        if (Arrays.equals(commandApdu, READ_FILE_08_01_COMMAND)) {
//            // step 04 Read File 10/03 Command
//            sendMessageToActivity("step 04 Read File 08/01 Command", bytesToHexNpe(commandApdu));
//            sendMessageToActivity("step 04 Read File 08/01 Response", bytesToHexNpe(READ_FILE_08_01_RESPONSE));
//            return concatenateByteArrays(READ_FILE_08_01_RESPONSE, RESPONSE_OK_SW);
//        }
//
//        // step 05
//        if (Arrays.equals(commandApdu, READ_FILE_10_01_COMMAND)) {
//            // step 05 Read File 10/04 Command
//            sendMessageToActivity("step 05 Read File 10/01 Command", bytesToHexNpe(commandApdu));
//            sendMessageToActivity("step 05 Read File 10/01 Response", bytesToHexNpe(READ_FILE_10_01_RESPONSE));
//            return concatenateByteArrays(READ_FILE_10_01_RESPONSE, RESPONSE_OK_SW);
//        }
//
//        // step 06
//        if (Arrays.equals(commandApdu, READ_FILE_20_01_COMMAND)) {
//            // step 06 Read File 10/05 Command
//            sendMessageToActivity("step 06 Read File 20/01 Command", bytesToHexNpe(commandApdu));
//            sendMessageToActivity("step 06 Read File 20/01 Response", bytesToHexNpe(READ_FILE_20_01_RESPONSE));
//            return concatenateByteArrays(READ_FILE_20_01_RESPONSE, RESPONSE_OK_SW);
//        }
//
//        // step 07
//        if (Arrays.equals(commandApdu, READ_FILE_20_02_COMMAND)) {
//            // step 07 Read File 10/06 Command
//            sendMessageToActivity("step 08 Read File 20/02 Command", bytesToHexNpe(commandApdu));
//            sendMessageToActivity("step 08 Read File 20/02 Response", bytesToHexNpe(READ_FILE_20_02_RESPONSE));
//            return concatenateByteArrays(READ_FILE_20_02_RESPONSE, RESPONSE_OK_SW);
//        }
//        sendMessageToActivity("UNKNOWN command", bytesToHexNpe(commandApdu));
//
//        return RESPONSE_OK_SW;
//
//    }

    private String sendAndWaitForResponse(String apduCommandHex) {
        final String[] responseHolder = new String[1]; // To hold the response from the server
        final Object lock = new Object(); // Synchronization lock

        new Thread(() -> {
            try (Socket socket = new Socket(POS_TERMINAL_IP, POS_TERMINAL_PORT);
                 OutputStream outputStream = socket.getOutputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Send the APDU command to the POS terminal
                outputStream.write((apduCommandHex + "\n").getBytes());
                outputStream.flush();
                sendMessageToActivity("Data sent to the POS emulator", apduCommandHex);

                // Wait for the response from the POS terminal
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                sendMessageToActivity("Data received from the POS emulator", responseBuilder.toString());
                // Store the response and notify the waiting thread
                synchronized (lock) {
                    responseHolder[0] = responseBuilder.toString();
                    lock.notify();
                }
            } catch (Exception e) {
                Log.e("HceCcEmulationService", "Error communicating with POS terminal: " + e.getMessage(), e);
            }
        }).start();

        // Wait for the response to be populated
        synchronized (lock) {
            try {
                lock.wait(5000); // Wait for a maximum of 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e("HceCcEmulationService", "Thread interrupted while waiting for POS response", e);
            }
        }

        // Return the response or null if none was received
        return responseHolder[0];
    }


    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle bundle) {
        String apduCommandHex = bytesToHexNpe(commandApdu);
        // sendToServer(apduCommandHex);

        String responseFromPos = sendAndWaitForResponse(apduCommandHex);

        // Convert the response back to a byte array and return it
        return responseFromPos != null ? hexStringToByteArray(responseFromPos) : new byte[0];
    }

    @Override
    public void onDeactivated(int reason) {
        sendMessageToActivity("------------------------", "----");
        sendMessageToActivity("onDeactivated with reason", String.valueOf(reason));
    }

    private void sendMessageToActivity(String msg, String data) {
        Logger.log("Activity", msg + " | " + data + "\n");
    }
}
