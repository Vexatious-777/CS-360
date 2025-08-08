package com.example.projecttwocs360

import DatabaseHelper
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val SMS_PERMISSION_CODE = 100
    }

    private lateinit var userNameEditText: EditText
    private lateinit var passWordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var newUser: Button
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userNameEditText = findViewById(R.id.editTextUser)
        passWordEditText = findViewById(R.id.editTextTextPassword)
        loginButton = findViewById(R.id.LogIn)
        newUser = findViewById(R.id.NewUser)

        dbHelper = DatabaseHelper(this)

        loginButton.setOnClickListener {
            val username = userNameEditText.text.toString()
            val password = passWordEditText.text.toString()

            if (username.isNotBlank() && password.isNotBlank()) {
                if (authenticateUser(username, password)) {
                    checkSmsPermission()
                } else {
                    Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_LONG)
                    .show()
            }
        }

        newUser.setOnClickListener {
            val username = userNameEditText.text.toString()
            val password = passWordEditText.text.toString()

            if (username.isNotBlank() && password.isNotBlank()) {
                if (authenticateUser(username, password)) {
                    Toast.makeText(this, "This User already exists", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "User Created", Toast.LENGTH_LONG).show()
                    addUser(username, password)
                }
            } else {
                Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_LONG)
                    .show()
            }
        }

    }

    private fun authenticateUser(username: String, password: String): Boolean {
        return dbHelper.authentication(username, password)
    }

    private fun addUser(username: String, password: String) {
        return dbHelper.addUser(username, password)
    }


    private fun checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                SMS_PERMISSION_CODE
            )
        } else {
            goToInventory()
        }
    }

    private fun goToInventory() {
        val intent = Intent(this, InventoryActivity::class.java)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goToInventory()
            } else {
                Toast.makeText(
                    this,
                    "SMS permission is required for SMS alerts.",
                    Toast.LENGTH_LONG
                ).show()
                goToInventory()
            }
        }
    }
}

