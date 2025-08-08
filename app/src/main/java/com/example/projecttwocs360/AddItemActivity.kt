package com.example.projecttwocs360

import DatabaseHelper
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        val editTextItemName = findViewById<EditText>(R.id.editTextItemName)
        val editTextItemQuantity = findViewById<EditText>(R.id.editTextItemQuantity)
        val buttonSaveItem = findViewById<Button>(R.id.buttonSaveItem)

        buttonSaveItem.setOnClickListener {
            val itemName = editTextItemName.text.toString()
            val itemQuantity = editTextItemQuantity.text.toString().toIntOrNull() ?: 0

            if (itemName.isNotBlank() && itemQuantity > 0) {
                val dbHelper = DatabaseHelper(this)
                dbHelper.addItem(itemName, itemQuantity)

                val resultIntent = Intent()
                resultIntent.putExtra("Item_Name", itemName)
                resultIntent.putExtra("Item_Quantity", itemQuantity)
                setResult(RESULT_OK, resultIntent)

                finish()
            }
        }
    }
}
