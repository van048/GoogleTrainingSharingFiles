package cn.ben.googletrainingsharingfiles;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;

import cn.ben.googletrainingsharingfiles.databinding.ActivityFileSelectBinding;

public class FileSelectActivity extends Activity {
    // The path to the root of this app's internal storage
    private File mPrivateRootDir;
    // The path to the "images" subdirectory
    private File mImagesDir;
    // Array of files in the images subdirectory
    private File[] mImageFiles;
    // Array of filenames corresponding to mImageFiles
    private String[] mImageFilenames;
    private ListView mFileListView;
    private Intent mResultIntent;

    // Initialize the Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityFileSelectBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_file_select);

        // Set up an Intent to send back to apps that request a file??
        mResultIntent =
                new Intent("cn.ben.googletrainingsharingfiles.ACTION_RETURN_FILE");
        // Get the files/ subdirectory of internal storage
        mPrivateRootDir = getFilesDir();
        // Get the files/images subdirectory;
        mImagesDir = new File(mPrivateRootDir, "images");
        // Get the files in the images subdirectory
        mImageFiles = mImagesDir.listFiles();
        // Set the Activity's result to null to begin with??
        setResult(Activity.RESULT_CANCELED, null);
        /*
         * Display the file names in the ListView mFileListView.
         * Back the ListView with the array mImageFilenames, which
         * you can create by iterating through mImageFiles and
         * calling File.getAbsolutePath() for each File
         */
        if (mImageFiles != null) {
            mImageFilenames = new String[mImageFiles.length];
            for (int i = 0; i < mImageFiles.length; i++) {
                mImageFilenames[i] = mImageFiles[i].getAbsolutePath();
            }

            mFileListView = binding.list;
            mFileListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mImageFilenames));
            // Define a listener that responds to clicks on a file in the ListView
            mFileListView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        /*
                         * When a filename in the ListView is clicked, get its
                         * content URI and send it to the requesting app
                         */
                        public void onItemClick(AdapterView<?> adapterView,
                                                View view,
                                                int position,
                                                long rowId) {
                            /*
                             * Get a File for the selected file name.
                             * Assume that the file names are in the
                             * mImageFilenames array.
                             */
                            File requestFile = new File(mImageFilenames[position]);
                            /*
                             * Most file-related method calls need to be in
                             * try-catch blocks.
                             */
                            // Use the FileProvider to get a content URI
                            try {
                                Uri fileUri = FileProvider.getUriForFile(
                                        FileSelectActivity.this,
                                        "cn.ben.googletrainingsharingfiles.fileprovider",
                                        requestFile);
                                if (fileUri != null) {
                                    // Grant temporary read permission to the content URI
                                    mResultIntent.addFlags(
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    // Put the Uri and MIME type in the result Intent
                                    mResultIntent.setDataAndType(
                                            fileUri,
                                            getContentResolver().getType(fileUri));
                                    // Set the result
                                    FileSelectActivity.this.setResult(Activity.RESULT_OK,
                                            mResultIntent);
                                } else {
                                    mResultIntent.setDataAndType(null, "");
                                    FileSelectActivity.this.setResult(RESULT_CANCELED,
                                            mResultIntent);
                                }
                            } catch (IllegalArgumentException e) {
                                Log.e("File Selector",
                                        "The selected file can't be shared: " +
                                                mImageFilenames[position]);
                            }
                            new AlertDialog.Builder(FileSelectActivity.this).setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            }).show();
                        }
                    });
        }
    }
}
