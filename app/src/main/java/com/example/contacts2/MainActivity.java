package com.example.contacts2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.EditText;


import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.SaveDateListener, AdapterView.OnItemSelectedListener {
    TextView birthdayTextView;
    private Contact currentContact;
    final int PERMISSION_REQUEST_PHONE = 102;
    final int PERMISSION_REQUEST_CAMER = 103;
    final int CAMERA_REQUEST = 1888;

    ActivityResultLauncher<Intent> resultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initListButton();
        initSettingButton();
        initMapButton();
        initToggleButton();
        initStateSpinner();
        initCallFunction();
        initImageButton();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int contactId = extras.getInt("contactId");
            Toast.makeText(this, "Recieved Contact ID: "+contactId, Toast.LENGTH_LONG).show();

            initContact(extras.getInt("contactId"));
        }
        else {

            currentContact = new Contact();
        }
        setForEditing(false);
        initChangeDateButton();
        initTextChangeEvents();
        initSaveButton();


    }
    @Override
    public void onRequestPermissionResult(int requestCode,
                                          @NonNull String permissions[], @NonNull int[] grantResults){
        switch (requestCode){
            case PERMISSION_REQUEST_PHONE:{
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "You may now call from this app.", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(MainActivity.this, "You will not be able to make calls form this app.",
                            Toast.LENGTH_LONG).show();
                }
            }
            case PERMISSION_REQUEST_CAMER:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                }
                else{
                    Toast.makeText(MainActivity.this,
                            "You will not be able to save conact pictures from this app",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }

        }

    }

    private void takePhoto(){
        Intent cameraIntent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                Bitmap scaledPhoto = Bitmap.createScaledBitmap(photo, 144, 144, true);
                ImageButton imageContact = (ImageButton) findViewById(R.id.imageContact);
                imageContact.setImageBitmap(scaledPhoto);
                currentContact.setImage(scaledPhoto);
            }
        }
    }

    private void callContact(String phonenNumber){
        Intent intent = new Intent (Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phonenNumber));
        if(Build.VERSION.SDK_INT >= 23 &&
        ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.CALL_PHONE) !=
        PackageManager.PERMISSION_GRANTED){
            return;
        }
        else{
            startActivity(intent);
        }
    }

    private void initCallFunction() {
        EditText editPhone = (EditText) findViewById(R.id.home_number);
        editPhone.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                checkPhonePermission(currentContact.getHomeNumber());
                return false;
            }
        });
        EditText editCell = (EditText) findViewById(R.id.cell_number);
        editPhone.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                checkPhonePermission(currentContact.getCellNumber());
                return false;
            }
        });



    }

    private void checkPhonePermission(String phoneNumber){
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        android.Manifest.permission.CALL_PHONE)){
                    Snackbar.make(findViewById(R.id.activity_main),
                            "MyConactList requires this permission to place a call from the app.",
                            Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String [] {android.Manifest.permission.CALL_PHONE},
                                    PERMISSION_REQUEST_PHONE);
                        }
                    })
                            .show();
                }else{
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.CALL_PHONE},
                            PERMISSION_REQUEST_PHONE);
                }
            }
            else {
                callContact(phoneNumber);
                }
            }else {
            callContact(phoneNumber);
        }
    }

    private void initListButton() {
        ImageButton ibList = findViewById(R.id.contact_button);
        ibList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContactListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
    private void initMapButton() {
        ImageButton ibList = findViewById(R.id.map_button);
        ibList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContactMapActivity.class);
                if(currentContact.getContactID() == -1){
                    Toast.makeText(getBaseContext(), "Contact must be saved before it can be mapped", Toast.LENGTH_LONG).show();

                } else{
                    intent.putExtra("contactId", currentContact.getContactID());
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                //Sensor:
                //Intent intent = new Intent(MainActivity.this, ContactMapSensorActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //startActivity(intent);
            }
        });
    }
    private void initSettingButton() {
        ImageButton ibList = findViewById(R.id.setting_button);
        ibList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContactSettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void initToggleButton() {
        final ToggleButton editToggle = findViewById(R.id.edit_toggle);
        editToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setForEditing(editToggle.isChecked());
            }
        });
    }
    private void setForEditing(boolean isEnabled) {
        EditText editName = findViewById(R.id.nameInput);
        EditText editAddress = findViewById(R.id.street_address);
        EditText editCity = findViewById(R.id.city_textEdit);
        Spinner editState = findViewById(R.id.spinnerState);
        EditText editZipCode = findViewById(R.id.zipCode);
        EditText editHome = findViewById(R.id.home_number);
        EditText editCell = findViewById(R.id.cell_number);
        EditText editEmail = findViewById(R.id.emailInput);
        Button buttonChangeDate = findViewById(R.id.birthdayButton);
        Button buttonSave = findViewById(R.id.save_Button);
        ImageButton picture = findViewById(R.id.imageContact);

        editName.setEnabled(isEnabled);
        editAddress.setEnabled(isEnabled);
        editCity.setEnabled(isEnabled);
        editState.setEnabled(isEnabled);
        editZipCode.setEnabled(isEnabled);

        editEmail.setEnabled(isEnabled);
        buttonChangeDate.setEnabled(isEnabled);
        buttonSave.setEnabled(isEnabled);
        picture.setEnabled(isEnabled);


        if (isEnabled) {
            editHome.setInputType(InputType.TYPE_CLASS_PHONE);
            editCell.setInputType(InputType.TYPE_CLASS_PHONE);
            editName.requestFocus();
        }
        else{
            editHome.setInputType(InputType.TYPE_NULL);
            editCell.setInputType(InputType.TYPE_NULL);

        }
    }
    @Override
    public void didFinishDatePickerDialog(Calendar selectedTime) {
        TextView birthday = findViewById(R.id.birthdayTextView);
        birthday.setText(DateFormat.format("MM/dd/yyyy", selectedTime));
        currentContact.setBirthday(selectedTime);
    }

    private void initChangeDateButton() {
        Button changeDate = findViewById(R.id.birthdayButton);
        changeDate.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                DatePickerDialog datePickerDialog = new DatePickerDialog();
                datePickerDialog.show(fm, "DatePick");
            }
        });
    }
    private void initTextChangeEvents() {
        final EditText etContactName = findViewById(R.id.nameInput);
        etContactName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setContactName(etContactName.getText().toString());

            }
        });

        final EditText etStreetAddress = findViewById(R.id.street_address);
        etStreetAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setStreetAddress(etStreetAddress.getText().toString());

            }
        });
        final EditText etHomePhone = findViewById(R.id.home_number);
        etHomePhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setHomeNumber(etHomePhone.getText().toString());

            }
        });

        final EditText etCellNumber = findViewById(R.id.cell_number);
        etCellNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setCellNumber(etCellNumber.getText().toString());

            }
        });

        final EditText etCity = findViewById(R.id.city_textEdit);
        etCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setCity(etCity.getText().toString());

            }
        });

        final EditText etZipCode = findViewById(R.id.zipCode);
        etZipCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setZipCode(etZipCode.getText().toString());

            }
        });

        final EditText etEmail = findViewById(R.id.emailInput);
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setEMail(etEmail.getText().toString());

            }
        });
        etCellNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        etHomePhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
    }

    private void initSaveButton () {
        Button saveButton = findViewById(R.id.save_Button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean wasSuccesful;
                ContactDataSource ds = new ContactDataSource(MainActivity.this);
                try{
                    ds.open();

                    if (currentContact.getContactID() == -1) {
                        wasSuccesful = ds.insertContact(currentContact);

                    }
                    else {
                        wasSuccesful = ds.updateContact(currentContact);
                    }

                    ds.close();
                }
                catch (Exception e) {
                    wasSuccesful = false;
                }

                if(wasSuccesful) {
                    ToggleButton editToggle = findViewById(R.id.edit_toggle);
                    editToggle.toggle();
                    setForEditing(false);

                }
            }
        });
    }

    private void initStateSpinner(){
        Spinner stateSpinner = findViewById(R.id.spinnerState);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapter);
        stateSpinner.setOnItemSelectedListener(this);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        currentContact.setState(text);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }



    private void initContact (int id) {
        ContactDataSource ds = new ContactDataSource(MainActivity.this);
        try {
            ds.open();
            currentContact = ds.getSpecificContact(id);
            ds.close();
        }
        catch (Exception e) {
            Toast.makeText(this, "Load Contact Failed", Toast.LENGTH_LONG).show();
        }

        EditText editName = findViewById(R.id.nameInput);
        EditText editAddress = findViewById(R.id.street_address);
        EditText editCity = findViewById(R.id.city_textEdit);
        Spinner editState = findViewById(R.id.spinnerState);
        EditText editZipCode = findViewById(R.id.zipCode);
        EditText editHome = findViewById(R.id.home_number);
        EditText editCell = findViewById(R.id.cell_number);
        EditText editEmail = findViewById(R.id.emailInput);
        TextView birthDay = findViewById(R.id.birthdayTextView);
        ImageButton picture = findViewById(R.id.imageContact);
        if (currentContact.getImage() != null){
            picture.setImageBitmap(currentContact.getImage());
        }
        else{
            picture.setImageResource(R.drawable.person_icon);
        }


        editName.setText(currentContact.getContactName());
        editAddress.setText(currentContact.getStreetAddress());
        editCity.setText(currentContact.getCity());

        editZipCode.setText(currentContact.getZipCode());
        editHome.setText(currentContact.getHomeNumber());
        editCell.setText(currentContact.getCellNumber());
        editEmail.setText(currentContact.getEMail());
        birthDay.setText(DateFormat.format("MM/dd/yyyy", currentContact.getBirthday().getTimeInMillis()).toString());

        String state = currentContact.getState();
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) editState.getAdapter();
        int index = adapter.getPosition(state);
        if (index != -1) {
            editState.setSelection(index);
        }
    }
    private void initImageButton() {
        ImageButton ib = findViewById(R.id.imageContact);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23 ) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            android.Manifest.permission.CAMERA) !=
                            PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                android.Manifest.permission.CAMERA)) {
                            Snackbar.make(findViewById(R.id.activity_main),
                                            "The app needs permission to take a picture.",
                                            Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ActivityCompat.requestPermissions(MainActivity.this,
                                                    new String[]{android.Manifest.permission.CAMERA},
                                                    PERMISSION_REQUEST_CAMER);


                                        }
                                    })
                                    .show();
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.CAMERA},
                                    PERMISSION_REQUEST_CAMER);
                        }
                    } else {
                        takePhoto();
                    }
                }else{
                    takePhoto();
                }
            }
        });


    }
}