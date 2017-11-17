package com.example.srujana.security;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Share extends AppCompatActivity {
    DatabaseHelper myDB;
    EditText edit_name,edit_surname,edit_mark;
    Button btn_addData;
    Button view_All;
    Button btnDelete;
    Button btnviewUpdate;
    EditText editTextID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        myDB = new DatabaseHelper(this);
        edit_name=(EditText)findViewById(R.id.editName);
        edit_surname=(EditText)findViewById(R.id.editSurname);
        edit_mark=(EditText)findViewById(R.id.editMark);
        btn_addData=(Button)findViewById(R.id.btnAdd);
        view_All=(Button)findViewById(R.id.btnGetAll);
        btnDelete=(Button)findViewById(R.id.btnDel);
        btnviewUpdate=(Button)findViewById(R.id.btnUpdate);
        editTextID=(EditText)findViewById(R.id.editTextId);
        addData();
        viewAll();
        UpdateData();
        DeleteData();

    }
    public void addData(){
        btn_addData.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isInserted = myDB.insertData(edit_name.getText().toString(),
                                edit_surname.getText().toString(),
                                edit_mark.getText().toString());

                        if (isInserted)
                            Toast.makeText(Share.this,"Data inserted", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(Share.this,"Data not inserted", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
    public void viewAll() {
        view_All.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor res = myDB.getAllData();
                        if(res.getCount() == 0) {
                            // show message
                            showMessage("Error","Nothing found");
                            return;
                        }

                        StringBuffer buffer = new StringBuffer();
                        while (res.moveToNext()) {
                            buffer.append("Id :"+ res.getString(0)+"\n");
                            buffer.append("Name :"+ res.getString(1)+"\n");
                            buffer.append("Surname :"+ res.getString(2)+"\n");
                            buffer.append("Marks :"+ res.getString(3)+"\n\n");
                        }

                        // Show all data
                        showMessage("Data",buffer.toString());
                    }
                }
        );
    }

    public void showMessage(String title, String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }
    public void DeleteData() {
        btnDelete.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor res = myDB.getAllData();
                        Integer deletedRows = myDB.deleteData(editTextID.getText().toString());
                        if(deletedRows > 0)
                            Toast.makeText(Share.this,"Data Deleted", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(Share.this,"Data not Deleted", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
    public void UpdateData() {
        btnviewUpdate.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor res = myDB.getAllData();
                        boolean isUpdate = myDB.updateData(editTextID.getText().toString(),
                                edit_name.getText().toString(),
                                edit_surname.getText().toString(),edit_mark.getText().toString());
                        if(isUpdate == true)
                            Toast.makeText(Share.this,"Data Update", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(Share.this,"Data not Updated", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

}
