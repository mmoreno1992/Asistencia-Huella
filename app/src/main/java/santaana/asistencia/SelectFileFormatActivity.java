package santaana.asistencia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;

public class SelectFileFormatActivity extends Activity {

    private Button mButtonOK;
    private RadioGroup mRadioGroup;
    private RadioButton mRadioBitmap;
    private RadioButton mRadioWSQ;
    private RadioButton mRadioSample;
    private RadioButton mRadioANSI;
    private RadioButton mRadioISO;
    private EditText mEditFileName;
    private TextView mMessage;

    private static File mDir;
    private String mFileFormat = "BITMAP";
    private String mFileName;
    // Return Intent extra
    public static String EXTRA_FILE_FORMAT = "file_format";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectfileformatactivity);
        mButtonOK = findViewById(R.id.buttonOK);
        mRadioGroup = findViewById(R.id.radioGroup1);
        mRadioBitmap = findViewById(R.id.radioBitmap);
        mRadioWSQ = findViewById(R.id.radioWSQ);
        mRadioSample = findViewById(R.id.radioSample);
        mRadioANSI = findViewById(R.id.radioANSI);
        mRadioISO = findViewById(R.id.radioISO);
        mEditFileName = findViewById(R.id.editFileName);
        mMessage = findViewById(R.id.textMessage);
        setResult(Activity.RESULT_CANCELED);

        Intent intent = getIntent();
        int fileFormat = intent.getIntExtra("DEFAULT_FILE_FORMAT", 0);

        if (fileFormat == BluetoothDataService.DATA_TYPE_FT_SAMPLE) {
            mRadioBitmap.setEnabled(false);
            mRadioWSQ.setEnabled(false);
            mRadioSample.setEnabled(true);
            mRadioANSI.setEnabled(false);
            mRadioISO.setEnabled(false);
            mRadioSample.setChecked(true);
            mFileFormat = "FUTRONIC SAMPLE";
        } else if (fileFormat == BluetoothDataService.DATA_TYPE_ANSI_SAMPLE) {
            mRadioBitmap.setEnabled(false);
            mRadioWSQ.setEnabled(false);
            mRadioSample.setEnabled(false);
            mRadioANSI.setEnabled(true);
            mRadioISO.setEnabled(false);
            mRadioANSI.setChecked(true);
            mFileFormat = "ANSI SAMPLE";
        } else if (fileFormat == BluetoothDataService.DATA_TYPE_ISO_SAMPLE) {
            mRadioBitmap.setEnabled(false);
            mRadioWSQ.setEnabled(false);
            mRadioSample.setEnabled(false);
            mRadioANSI.setEnabled(false);
            mRadioISO.setEnabled(true);
            mRadioISO.setChecked(true);
            mFileFormat = "ISO SAMPLE";
        } else if (fileFormat == BluetoothDataService.DATA_TYPE_WSQIMAGE) {
            mRadioBitmap.setEnabled(true);
            mRadioWSQ.setEnabled(true);
            mRadioWSQ.setChecked(true);
            mRadioSample.setEnabled(false);
            mRadioANSI.setEnabled(false);
            mRadioISO.setEnabled(false);
            mRadioANSI.setEnabled(false);
            mFileFormat = "WSQ";
        } else  //DATA_TYPE_RAWIMAGE
        {
            mRadioBitmap.setEnabled(true);
            mRadioWSQ.setEnabled(false);
            mRadioSample.setEnabled(false);
            mRadioANSI.setEnabled(false);
            mRadioISO.setEnabled(false);
            mRadioBitmap.setChecked(true);
            mFileFormat = "BITMAP";
        }

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == mRadioBitmap.getId())
                    mFileFormat = "BITMAP";
                else if (checkedId == mRadioWSQ.getId())
                    mFileFormat = "WSQ";
                else if (checkedId == mRadioSample.getId())
                    mFileFormat = "FUTRONIC SAMPLE";
                else if (checkedId == mRadioANSI.getId())
                    mFileFormat = "ANSI SAMPLE";
                else if (checkedId == mRadioISO.getId())
                    mFileFormat = "ISO SAMPLE";
            }
        });

        mButtonOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFileName = mEditFileName.getText().toString();
                if (mFileName.trim().length() == 0) {
                    ShowAlertDialog();
                    return;
                }
                if (!isImageFolder())
                    return;

                if (mFileFormat.compareTo("BITMAP") == 0)
                    mFileName = mFileName + ".bmp";
                else if (mFileFormat.compareTo("WSQ") == 0)
                    mFileName = mFileName + ".wsq";
                else if (mFileFormat.compareTo("FUTRONIC SAMPLE") == 0)
                    mFileName = mFileName + ".bin";
                else if (mFileFormat.compareTo("ANSI SAMPLE") == 0)
                    mFileName = mFileName + ".ansi";
                else if (mFileFormat.compareTo("ISO SAMPLE") == 0)
                    mFileName = mFileName + ".iso1";
                CheckFileName();
            }
        });
    }

    private void ShowAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("File name")
                .setMessage("File name can not be empty!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void SetFileName() {
        String[] extraString = new String[2];
        extraString[0] = mFileFormat;
        extraString[1] = mDir.getAbsolutePath() + "/" + mFileName;
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FILE_FORMAT, extraString);
        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void CheckFileName() {
        File f = new File(mDir, mFileName);
        if (f.exists()) {
            new AlertDialog.Builder(this)
                    .setTitle("File name")
                    .setMessage("File already exists. Do you want replace it?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            SetFileName();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            mMessage.setText("Cancel");
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else
            SetFileName();
    }

    public boolean isImageFolder() {
        // File extStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        File extStorageDirectory = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        mDir = new File(extStorageDirectory.getAbsolutePath());
        if (mDir.exists()) {
            if (!mDir.isDirectory()) {
                mMessage.setText("Can not create image folder " + mDir.getAbsolutePath() +
                        ". File with the same name already exist.");
                return false;
            }
        } else {
            try {
                mDir.mkdirs();
            } catch (SecurityException e) {
                mMessage.setText("Can not create image folder " + mDir.getAbsolutePath() +
                        ". Access denied.");
                return false;
            }
        }
        return true;
    }
}
