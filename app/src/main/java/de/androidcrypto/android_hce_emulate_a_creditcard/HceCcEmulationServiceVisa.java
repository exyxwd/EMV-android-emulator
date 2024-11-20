package de.androidcrypto.android_hce_emulate_a_creditcard;

import static de.androidcrypto.android_hce_emulate_a_creditcard.Utils.bytesToHexNpe;
import static de.androidcrypto.android_hce_emulate_a_creditcard.Utils.concatenateByteArrays;
import static de.androidcrypto.android_hce_emulate_a_creditcard.Utils.hexStringToByteArray;

import android.content.Context;
import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Arrays;

/**
 * This service class uses a static approach to serve the data (command and response data) of a
 * sample Visa Card. 
 * 
 * The provided data are from an outdated Visa Card that is no longer in use and the card is 
 * blocked by the bank. The data was recorded with the app "Talk to your Credit Card" 
 * available here: https://github.com/AndroidCrypto/TalkToYourCreditCardG8
 * 
 * Please make sure that the AID A0000000031010 is included in the apduservice.xml file or the 
 * service will not called by Android, the card will not get selected and the workflow stops.
 * 
 */

public class HceCcEmulationServiceVisa extends HostApduService {

    // this is a static approach to serve the byte arrays to the service

    private static final byte[] RESPONSE_OK_SW = hexStringToByteArray("9000");
    // Note: the responses does not include the 0x9000h terminator
    private static final byte[] SELECT_PPSE_COMMAND = hexStringToByteArray("00a404000e325041592e5359532e444446303100");
    private static final byte[] SELECT_PPSE_RESPONSE = hexStringToByteArray("6f2b840e325041592e5359532e4444463031a519bf0c1661144f07a00000000310109f0a080001050100000000");

    private static final byte[] SELECT_AID_COMMAND = hexStringToByteArray("00a4040007a000000003101000");
    private static final byte[] SELECT_AID_RESPONSE = hexStringToByteArray("6f5d8407a0000000031010a5525010564953412044454249542020202020208701029f38189f66049f02069f03069f1a0295055f2a029a039c019f37045f2d02656ebf0c1a9f5a0531082608269f0a080001050100000000bf6304df200180");

    // this needs to be dynamic as the response from the real NFC reader is not predictable (e.g. different amounts)
    private static final byte[] GET_PROCESSING_OPTONS_COMMAND_SHORTED = hexStringToByteArray("80a8");
    // private static final byte[] GET_PROCESSING_OPTONS_COMMAND_READER = hexStringToByteArray("80a8000023832127000000000000001000000000000000097800000000000978230301003839303100");
    private static final byte[] GET_PROCESSING_OPTONS_RESPONSE = hexStringToByteArray("7781c68202200094041003060057134921828094896752d25022013650000000000f9f100706040a03a020009f26089ffe5b7ee37376179f2701809f360203aa9f4b81804703a7be03f1e3dfed25d08fed5739de0d7ba5062b95eeab5aba503e447502d25bfc7d3ea6959b6e9fb9fed7e32f0382654ea3ad32b29728128f5e6fcd31ed2eb7859e9db84a9ca84e5366afac92792bff27adddf55816727e61740b1ae03ab89e3e5cf475099512896a9936f24b1cca97aacb975c4aa2fc827519d69d1d93be9f6c021600");

    private static final byte[] READ_FILE_10_03_COMMAND = hexStringToByteArray("00b2031400");
    private static final byte[] READ_FILE_10_03_RESPONSE = hexStringToByteArray("7081fb9081f830f056de40a950bec2a870c59d5462222605a8f31cdef39a0537c7c175115e352ad0c55470fce5737c4e769897623e01401da73e01644bb0b491aa1aadb27fc360c0089f7c2e52a64e96a3f8a59f76e49aa6dd9a6792644f2b0b513b1a1a93b98a3cc19f0bec45e9f8edd70f893a8cafb21b62f3b8f15983775f14fd16cb36a19120e5a5068ef9f05ffaea4e714d80f134a298d167a65a92f6f57963db94ab5d3967f6675b3609a0fceb5fbb70f07cfdeab1352c6a34d6be737aa74848f3f56932f08b51f54aa3040f1ace4a0ced38684df900a395c5cd88562eb2af8d35601210c20d6c3425dcd813b9b358d1356d52a8ebd8fb5a19915d");

    private static final byte[] READ_FILE_10_04_COMMAND = hexStringToByteArray("00b2041400");
    private static final byte[] READ_FILE_10_04_RESPONSE = hexStringToByteArray("70078f01099f320103");

    private static final byte[] READ_FILE_10_05_COMMAND = hexStringToByteArray("00b2051400");
    private static final byte[] READ_FILE_10_05_RESPONSE = hexStringToByteArray("7081b49f4681b047461ffca14b5dfdc209569c8a14f17644251aa3f4abea251262134b920982f0250741f96fccb40800293054c0d89824ba7ac44ee7bab06fa157fccf7e52d3c64b4d8acd41b9774b801519ed6fec827ec2ec29f8991167c453776559a4a06fd98c4b9bd1548a65af2f56002a836bdf9a040a9253e653584c92833c3d1aa8e08c4de9cda1026044f80f39a9326a57496598987a6b3e18a5f56a8bdede752870e8793776db9d325ccd9c7ca5db33c28f04");

    private static final byte[] READ_FILE_10_06_COMMAND = hexStringToByteArray("00b2061400");
    private static final byte[] READ_FILE_10_06_RESPONSE = hexStringToByteArray("702e9f4701035a0849218280948967525f3401005f24032502285f280208269f6e04207000009f690701c1b626881600");

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle bundle) {
        sendMessageToActivity("# processCommandApdu received", bytesToHexNpe(commandApdu));
        sendMessageToActivity("------------------------", "----");
        sendMessageToActivity("# Visa Card #", bytesToHexNpe(commandApdu));

        // step 01
        if (Arrays.equals(commandApdu, SELECT_PPSE_COMMAND)) {
            // step 01 selecting the PPSE
            sendMessageToActivity("step 01 Select PPSE Command ", bytesToHexNpe(commandApdu));
            sendMessageToActivity("step 01 Select PPSE Response", bytesToHexNpe(SELECT_PPSE_RESPONSE));
            return concatenateByteArrays(SELECT_PPSE_RESPONSE, RESPONSE_OK_SW);
        }

        // step 02
        if (Arrays.equals(commandApdu, SELECT_AID_COMMAND)) {
            // step 02 selecting the AID
            sendMessageToActivity("step 02 Select AID Command ", bytesToHexNpe(commandApdu));
            sendMessageToActivity("step 02 Select AID Response", bytesToHexNpe(SELECT_AID_RESPONSE));
            return concatenateByteArrays(SELECT_AID_RESPONSE, RESPONSE_OK_SW);
        }

        // step 03
        // dynamic - just compare the beginning of the commandApdu with the sample
        byte[] shortedCommandApdu = Arrays.copyOf(commandApdu, 2);
        if (Arrays.equals(shortedCommandApdu, GET_PROCESSING_OPTONS_COMMAND_SHORTED)) {
            sendMessageToActivity("step 03 Get Processing Options (GPO) Command", bytesToHexNpe(commandApdu));
            sendMessageToActivity("step 03 Get Processing Options (GPO) Response", bytesToHexNpe(GET_PROCESSING_OPTONS_RESPONSE));
            return concatenateByteArrays(GET_PROCESSING_OPTONS_RESPONSE, RESPONSE_OK_SW);
        }

        // step 04
        if (Arrays.equals(commandApdu, READ_FILE_10_03_COMMAND)) {
            // step 04 Read File 10/03 Command
            sendMessageToActivity("step 04 Read File 10/03 Command", bytesToHexNpe(commandApdu));
            sendMessageToActivity("step 04 Read File 10/03 Response", bytesToHexNpe(READ_FILE_10_03_RESPONSE));
            return concatenateByteArrays(READ_FILE_10_03_RESPONSE, RESPONSE_OK_SW);
        }

        // step 05
        if (Arrays.equals(commandApdu, READ_FILE_10_04_COMMAND)) {
            // step 05 Read File 10/04 Command
            sendMessageToActivity("step 05 Read File 10/04 Command", bytesToHexNpe(commandApdu));
            sendMessageToActivity("step 05 Read File 10/05 Response", bytesToHexNpe(READ_FILE_10_04_RESPONSE));
            return concatenateByteArrays(READ_FILE_10_04_RESPONSE, RESPONSE_OK_SW);
        }

        // step 06
        if (Arrays.equals(commandApdu, READ_FILE_10_05_COMMAND)) {
            // step 06 Read File 10/05 Command
            sendMessageToActivity("step 06 Read File 10/05 Command", bytesToHexNpe(commandApdu));
            sendMessageToActivity("step 06 Read File 10/05 Response", bytesToHexNpe(READ_FILE_10_05_RESPONSE));
            return concatenateByteArrays(READ_FILE_10_05_RESPONSE, RESPONSE_OK_SW);
        }

        // step 07
        if (Arrays.equals(commandApdu, READ_FILE_10_06_COMMAND)) {
            // step 07 Read File 10/06 Command
            sendMessageToActivity("step 08 Read File 10/06 Command", bytesToHexNpe(commandApdu));
            sendMessageToActivity("step 08 Read File 10/06 Response", bytesToHexNpe(READ_FILE_10_06_RESPONSE));
            return concatenateByteArrays(READ_FILE_10_06_RESPONSE, RESPONSE_OK_SW);
        }

        // in any other case response an 'OK'
        return RESPONSE_OK_SW;
    }

    @Override
    public void onDeactivated(int reason) {
        sendMessageToActivity("------------------------", "----");
        sendMessageToActivity("onDeactivated with reason", String.valueOf(reason));
    }

    /**
     * A utility method to send data with an Broadcast Intent from this service to the MainActivity class
     *
     * @param msg
     * @param data
     */
    private void sendMessageToActivity(String msg, String data) {
        Intent intent = new Intent("HceCcEmulatorService");
        // You can also include some extra data.
        intent.putExtra("Message", msg);
        intent.putExtra("Data", data);
        Context context = this;
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
