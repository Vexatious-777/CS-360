package com.example.projecttwocs360

import DatabaseHelper
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class InventoryActivity : AppCompatActivity() {

    companion object {
        private const val ADD_ITEM_REQUEST_CODE = 1
        private const val SMS_PERMISSION_CODE = 100
    }

    private lateinit var adapter: ItemAdapter
    private val itemList = mutableListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inventory)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Inventory"

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewInventory)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Initialize item list (Still need database integration)
        itemList.addAll(getInventoryItems())

        // Adapter with SMS sending functionality
        adapter = ItemAdapter(itemList, { position -> deleteItem(position) }, this, ::sendSms)
        recyclerView.adapter = adapter

        //Refresh the inventory screen??
        refreshRecyclerView()

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            startActivityForResult(intent, ADD_ITEM_REQUEST_CODE)
        }

        checkSmsPermission()
    }

    override fun onResume() {
        super.onResume()
        refreshRecyclerView() //Finally got the RecyclerView to correctly display new additions (This should have been obvious!)
    }

    private fun sendSms(message: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            val phoneNumber = "YOUR_PHONE_NUMBER_HERE" // Replace with the actual phone number
            SMSUtils.sendSms(this, phoneNumber, message)
        } else {
            Toast.makeText(this, "SMS permission is not granted.", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "SMS permission is required for SMS alerts.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_ITEM_REQUEST_CODE && resultCode == RESULT_OK) {
            val itemName = data?.getStringExtra("ITEM_NAME") ?: ""
            val itemQuantity = data?.getIntExtra("ITEM_QUANTITY", 0) ?: 0

            if (itemName.isNotBlank() && itemQuantity > 0) {

                itemList.add(Item(itemName, itemQuantity))

                adapter.notifyItemInserted(itemList.size - 1)
            }
        }
    }

    private fun deleteItem(position: Int) {
        val item = itemList[position]

        val dbHelper = DatabaseHelper(this)
        dbHelper.deleteItem(item.name)

        itemList.removeAt(position)
        adapter.notifyItemRemoved(position)
    }

    private fun getInventoryItems(): List<Item> {
        // Replaced!
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val tableName = dbHelper.inventoryTableName


        val cursor = db.query(
            tableName,
            arrayOf(dbHelper.itemName, dbHelper.itemQuantity),
            null,
            null,
            null,
            null,
            null
        )
    //Initially thought of hardcoded approach seems like getters and setters maybe best
        val items = mutableListOf<Item>()
        if (cursor.moveToFirst()) {
            do{
                val name = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.itemName))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(dbHelper.itemQuantity))
                items.add(Item(name, quantity))
            }while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return items

    }

    private fun refreshRecyclerView() {
        val items = getInventoryItems()
        adapter.updateItems(items)
    }
}




