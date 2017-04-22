package org.example.i2c.address.scanner;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity {

    private final static String TAG = "I2C address scanner";
    private final static long DELAY_BETWEEN_SCANS = 5; //in seconds
    private final static int TEST_REGISTER = 0x0;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final PeripheralManagerService peripheralManagerService = new PeripheralManagerService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //headless application

        if (savedInstanceState == null) {

            final Callable<Void> scan = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    while (true) {
                        TimeUnit.SECONDS.sleep(DELAY_BETWEEN_SCANS);
                        performScan();
                    }
                }
            };

            executorService.submit(scan);
        }

    }

    @Override
    protected void onDestroy() {
        executorService.shutdownNow();
        super.onDestroy();
    }

    private void performScan() {
        for (int address = 0; address < 256; address++) {

            //auto-close the devices
            try (final I2cDevice device = peripheralManagerService.openI2cDevice(BoardDefaults.getI2cBus(), address)) {

                try {
                    device.readRegByte(TEST_REGISTER);
                    Log.i(TAG, String.format(Locale.US, "Trying: 0x%02X - SUCCESS", address));
                } catch (final IOException e) {
                    Log.i(TAG, String.format(Locale.US, "Trying: 0x%02X - FAIL", address));
                }

            } catch (final IOException e) {
                //in case the openI2cDevice(name, address) fails
            }
        }
    }

}
