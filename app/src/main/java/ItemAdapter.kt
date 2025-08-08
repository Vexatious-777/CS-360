package com.example.projecttwocs360

import DatabaseHelper
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

data class Item(val name: String, val quantity: Int)

class ItemAdapter(
    private var items: MutableList<Item>, //changed to variable was initially value. potential concern
    private val onDelete: (Int) -> Unit,
    private val context: Context,
    private val sendSms: (String) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val itemQuantity: TextView = itemView.findViewById(R.id.itemQuantity)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        init {
            deleteButton.setOnClickListener {
                onDelete(adapterPosition)
            }

            itemView.setOnClickListener {
                showEditQuantityDialog(adapterPosition)
            }
        }

        private fun showEditQuantityDialog(position: Int) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_quantity, null)
            val editTextNewQuantity = dialogView.findViewById<EditText>(R.id.editTextNewQuantity)

            AlertDialog.Builder(context)
                .setTitle("Edit Quantity")
                .setView(dialogView)
                .setPositiveButton("Save") { dialog, _ ->
                    val newQuantity = editTextNewQuantity.text.toString().toIntOrNull()
                    if (newQuantity != null && newQuantity >= 0) {
                        val item = items[position]
                        val oldQuantity = item.quantity

                        //added Database Integration
                        val dbHelper = DatabaseHelper(context)
                        dbHelper.updateItemQuantity(item.name, newQuantity)

                        items[position] = item.copy(quantity = newQuantity)

                        notifyItemChanged(position)

                        if (newQuantity == 1 && oldQuantity > 1) {
                            sendSms("Warning: Item ${items[position].name} has dropped to quantity 1.")
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.name
        holder.itemQuantity.text = item.quantity.toString()
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Item>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

}

