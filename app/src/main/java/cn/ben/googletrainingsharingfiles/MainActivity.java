package cn.ben.googletrainingsharingfiles;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import cn.ben.googletrainingsharingfiles.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Intent mRequestFileIntent;
    @SuppressWarnings("FieldCanBeLocal")
    private ParcelFileDescriptor mInputPFD;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mRequestFileIntent = new Intent(Intent.ACTION_PICK);
        mRequestFileIntent.setType("image/jpg");
    }

    private void requestFile() {
        /**
         * When the user requests a file, send an Intent to the
         * server app.
         * files.??
         */
        startActivityForResult(mRequestFileIntent, 0);
    }

    public void requestFile(@SuppressWarnings("UnusedParameters") View view) {
        requestFile();
    }

    /*
     * When the Activity of the app that hosts files?? sets a result and calls
     * finish(), this method is invoked. The returned Intent contains the
     * content URI of a selected file. The result code indicates if the
     * selection worked or not.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent returnIntent) {
        long fileSize = 0;
        // If the selection didn't work
        if (resultCode != RESULT_OK) {
            // Exit without doing anything else
            Log.d(TAG, "cancel");
        } else {
            // Get the file's content URI from the incoming Intent
            Uri returnUri = returnIntent.getData();
            String mimeType = getContentResolver().getType(returnUri);
            /*
             * Get the file's content URI from the incoming Intent,
             * then query the server app to get the file's display name
             * and size.
             */
            Cursor returnCursor =
                    getContentResolver().query(returnUri, null, null, null, null);
            /*
             * Get the column indexes of the data?? in the Cursor,
             * move to the first row in the Cursor, get the data,
             * and display it.
             */
            if (returnCursor != null) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                Log.d(TAG, mimeType + " " + returnCursor.getString(nameIndex) + " " + Long.toString(returnCursor.getLong(sizeIndex)));
                fileSize = returnCursor.getLong(sizeIndex);
                returnCursor.close();
            }
            /*
             * Try to open the file for "read" access?? using the
             * returned URI. If the file isn't found, write to the
             * error log and return.
             */
            try {
                /*
                 * Get the content resolver instance for this context, and use it
                 * to get a ParcelFileDescriptor for the file.
                 */
                mInputPFD = getContentResolver().openFileDescriptor(returnUri, "r");
                if (mInputPFD != null) {
                    // Get a regular file descriptor for the file
                    FileDescriptor fd = mInputPFD.getFileDescriptor();
                    FileInputStream fileInputStream = new FileInputStream(fd);
                    try {
                        byte[] buffer = new byte[(int) fileSize + 1];
                        int len = fileInputStream.read(buffer, 0, (int) fileSize + 1);
                        mBinding.iv.setImageBitmap(BitmapFactory.decodeByteArray(buffer, 0, len));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "File not found.");
            }
        }
    }

}
