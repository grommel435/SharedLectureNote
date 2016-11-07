package com.sharedlecturenote;

import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by DJ on 2016-10-31.
 */

public class convertFile {

    public static byte[] convertToByteArray(String sourcePath) {
        byte[] byteArray = null;

        try {
            // file을 inputStream에 넣음
            InputStream inputStream = new FileInputStream(sourcePath);

            byte[] buffer = new byte[8192];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bytesRead;

            try {
                while((bytesRead = inputStream.read(buffer)) != -1)
                {
                    baos.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            byteArray = baos.toByteArray();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    public static boolean convertToPdf(byte[] byteArray, String filePath, String fileName) {
        OutputStream out = null;

        try {
            out = new FileOutputStream(filePath + "/" + fileName);
            out.write(byteArray, 0, byteArray.length);
            out.flush();
            out.close();
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
