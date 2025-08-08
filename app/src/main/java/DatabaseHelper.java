import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Inventory.db";
    private static final int DATABASE_VERSION = 1;

    //Table for Users
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "username";
    private static final String COLUMN_PASSWORD_HASH = "password_hash"; //kinda of lazy not sure about implementing pointless security atm

    //Table for Inventory
    private static final String TABLE_INVENTORY = "Inventory";
    private static final String COLUMN_ITEM_ID = "item_id";
    private static final String COLUMN_ITEM_NAME = "item_name";
    private static final String COLUMN_ITEM_QUANTITY = "item_quantity";
    private static final String COLUMN_USER_ID_ATT = "user_id"; //not sure if I'll keep this

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creation of tables
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_NAME + " TEXT NOT NULL UNIQUE, " +
                COLUMN_PASSWORD_HASH + " TEXT NOT NULL)";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_INVENTORY_TABLE = "CREATE TABLE " + TABLE_INVENTORY + " (" +
                COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL, " +
                COLUMN_USER_ID_ATT + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_USER_ID_ATT + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "))";
        db.execSQL(CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // seems like good practice but unsure of point here
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS); // Check out ALTER, instead?
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);

        onCreate(db);
    }

    public void addUser(String username, String passwordHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, username);
        values.put(COLUMN_PASSWORD_HASH, passwordHash);
        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    public boolean authentication(String username, String passwordHash) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_NAME + " = ? AND "
                + COLUMN_PASSWORD_HASH + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, passwordHash});
        boolean result = cursor.moveToFirst();
        cursor.close();
        db.close();
        return result;
    }

    public void addItem(String itemName, int itemQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //should I change the item id to be an int and auto increment it? no I think SQL handles that
        values.put(COLUMN_ITEM_NAME, itemName);
        values.put(COLUMN_ITEM_QUANTITY, itemQuantity);

        db.insert(TABLE_INVENTORY, null, values);
        db.close();
    }

    public void deleteItem(String itemName) {
        SQLiteDatabase db = this.getWritableDatabase();

        int rowsAffected = db.delete(TABLE_INVENTORY, COLUMN_ITEM_NAME + " = ?", new String[]{itemName});

        if (rowsAffected > 0) {
            Log.d("DatabaseHelper", "Item Delete Success: " + itemName);
        } else {
          Log.d("DatabaseHelper", "Item Delete Failure: " + itemName);
        }
        db.close();

    }

    public void updateItemQuantity(String itemName, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ITEM_QUANTITY, newQuantity);

        String selection = COLUMN_ITEM_NAME + " = ?";
        String[] selectionArgs = { itemName };

        int rowsAffected = db.update(TABLE_INVENTORY, values, selection, selectionArgs);
        if (rowsAffected > 0) {
            Log.d("DatabaseHelper","Quantity update Success: " + itemName);
        } else {
            Log.d("DatabaseHelper", "Quantity update Failure: " + itemName);
        }

        db.close();

    }

    public String getInventoryTableName() {
        return TABLE_INVENTORY;
    }
    public String getItemName() {
        return COLUMN_ITEM_NAME;
    }
    public String getItemQuantity() {
        return COLUMN_ITEM_QUANTITY;
    }


}
