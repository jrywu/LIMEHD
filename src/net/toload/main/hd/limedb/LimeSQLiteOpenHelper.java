// Jeremy '12,4,7 derived from android SQLiteOpenHelper.java with stored in device or sdcard option 
//and static dbconnection (all instances share the same db connection).

package net.toload.main.hd.limedb;

import java.io.File;
import net.toload.main.hd.global.LIME;
import net.toload.main.hd.global.LIMEPreferenceManager;
import net.toload.main.hd.global.LIMEUtilities;
import android.content.Context;
//import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * A helper class to manage database creation and version management.
 *
 * <p>You create a subclass implementing {@link #onCreate}, {@link #onUpgrade} and
 * optionally {@link #onOpen}, and this class takes care of opening the database
 * if it exists, creating it if it does not, and upgrading it as necessary.
 * Transactions are used to make sure the database is always in a sensible state.
 *
 * <p>This class makes it easy for {@link android.content.ContentProvider}
 * implementations to defer opening and upgrading the database until first use,
 * to avoid blocking application startup with long-running database upgrades.
 *
 * <p>For an example, see the NotePadProvider class in the NotePad sample application,
 * in the <em>samples/</em> directory of the SDK.</p>
 *
 * <p class="note"><strong>Note:</strong> this class assumes
 * monotonically increasing version numbers for upgrades.</p>
 */
public abstract class LimeSQLiteOpenHelper {

	private final boolean DEBUG = false;
	private final String TAG = "LimeSQLiteOpenHelper";
	
	private LIMEPreferenceManager mLIMEPref;
	
    //private final Context mContext;
    private final String mName;
   // private final CursorFactory mFactory;
    private final int mNewVersion;

    private  SQLiteDatabase mDatabase = null; 
    private boolean mIsInitializing = false;
   // private final DatabaseErrorHandler mErrorHandler;

    /**
     * Create a helper object to create, open, and/or manage a database.
     * This method always returns very quickly.  The database is not actually
     * created or opened until one of {@link #getWritableDatabase} or
     * {@link #getReadableDatabase} is called.
     *
     * @param context to use to open or create the database
     * @param name of the database file, or null for an in-memory database
     * @param factory to use for creating cursor objects, or null for the default
     * @param version number of the database (starting at 1); if the database is older,
     *     {@link #onUpgrade} will be used to upgrade the database; if the database is
     *     newer, {@link #onDowngrade} will be used to downgrade the database
     */
    public LimeSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
        
        if (version < 1) throw new IllegalArgumentException("Version must be >= 1, was " + version);
        //if (errorHandler == null) {
        //    throw new IllegalArgumentException("DatabaseErrorHandler param value can't be null.");
        //}

        //mContext = context;
        mName = name;
        //mFactory = factory;
        mNewVersion = version;
        //mErrorHandler = errorHandler;
        
        mLIMEPref = new LIMEPreferenceManager(context.getApplicationContext());

    }

	private String getDBPath(String dbTarget){
		String dbLocationPrefix = (dbTarget.equals("sdcard"))
				?LIME.DATABASE_DECOMPRESS_FOLDER_SDCARD:LIME.DATABASE_DECOMPRESS_FOLDER;
		
		return dbLocationPrefix + File.separator + LIME.DATABASE_NAME;
	}
	private String getDBPath(){
		String dbtarget = mLIMEPref.getParameterString("dbtarget");
		if(DEBUG)
			Log.i(TAG, "getDBPath(): " + getDBPath(dbtarget));			
		return getDBPath(dbtarget);
		
	}
	
    /**
     * Return the name of the SQLite database being opened, as given tp
     * the constructor.
     */
    public String getDatabaseName() {
        return mName;
    }

    /**
     * Create and/or open a database that will be used for reading and writing.
     * The first time this is called, the database will be opened and
     * {@link #onCreate}, {@link #onUpgrade} and/or {@link #onOpen} will be
     * called.
     *
     * <p>Once opened successfully, the database is cached, so you can
     * call this method every time you need to write to the database.
     * (Make sure to call {@link #close} when you no longer need the database.)
     * Errors such as bad permissions or a full disk may cause this method
     * to fail, but future attempts may succeed if the problem is fixed.</p>
     *
     * <p class="caution">Database upgrade may take a long time, you
     * should not call this method from the application main thread, including
     * from {@link android.content.ContentProvider#onCreate ContentProvider.onCreate()}.
     *
     * @throws SQLiteException if the database cannot be opened for writing
     * @return a read/write database object valid until {@link #close} is called
     */
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (mDatabase != null) {
            if (!mDatabase.isOpen()) {
                // darn! the user closed the database by calling mDatabase.close()
                mDatabase = null;
            } else if (!mDatabase.isReadOnly()) {
                return mDatabase;  // The database is already open for business
            }
        }
        
        if(LIMEUtilities.isFileExist(getDBPath())==null) return null; //database file is not exist. return null Jeremy '12,5,1

        if (mIsInitializing) {
            throw new IllegalStateException("getWritableDatabase called recursively");
        }

        // If we have a read-only database open, someone could be using it
        // (though they shouldn't), which would cause a lock to be held on
        // the file, and our attempts to open the database read-write would
        // fail waiting for the file lock.  To prevent that, we acquire the
        // lock on the read-only database, which shuts out other users.

        boolean success = false;
        SQLiteDatabase db = null;
        //if (mDatabase != null) mDatabase. .lock();
        try {

        	db = SQLiteDatabase.openDatabase(getDBPath(), null, SQLiteDatabase.OPEN_READWRITE
        			| SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        }catch(Exception e){
        	return null; //return null if db opened failed.
        }
        try {
        	 mIsInitializing = true;
            int version = db.getVersion();
            if(DEBUG)
            	Log.i(TAG,"getWritableDatabase(), db version= "+ version +"; newversion = " + mNewVersion);
            if (version != mNewVersion) {
                db.beginTransaction();
                try {
                    if (version != mNewVersion) {
                       onUpgrade(db, version, mNewVersion);
                    }
                    db.setVersion(mNewVersion);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            onOpen(db);
            success = true;
            return db;
        } finally {
            mIsInitializing = false;
            if (success) {
            	if(DEBUG)
            		Log.i(TAG,"getWritableDatabse(), success in finally section");
                //if (mDatabase != null) {
                   // try { mDatabase.close(); } catch (Exception e) { }
                   // mDatabase.unlock();
                //}
                mDatabase = db;
                return db;
            } else {
            	Log.i(TAG,"getWritableDatabse(), not success in finally section and db closed");
                //if (mDatabase != null) mDatabase.unlock();
                if (db != null) db.close();
            }
        }
    }

    /**
     * Create and/or open a database.  This will be the same object returned by
     * {@link #getWritableDatabase} unless some problem, such as a full disk,
     * requires the database to be opened read-only.  In that case, a read-only
     * database object will be returned.  If the problem is fixed, a future call
     * to {@link #getWritableDatabase} may succeed, in which case the read-only
     * database object will be closed and the read/write object will be returned
     * in the future.
     *
     * <p class="caution">Like {@link #getWritableDatabase}, this method may
     * take a long time to return, so you should not call it from the
     * application main thread, including from
     * {@link android.content.ContentProvider#onCreate ContentProvider.onCreate()}.
     *
     * @throws SQLiteException if the database cannot be opened
     * @return a database object valid until {@link #getWritableDatabase}
     *     or {@link #close} is called.
     */
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (mDatabase != null) {
            if (!mDatabase.isOpen()) {
                // darn! the user closed the database by calling mDatabase.close()
                mDatabase = null;
            } else {
                return mDatabase;  // The database is already open for business
            }
        }

        if(LIMEUtilities.isFileExist(getDBPath())==null) return null; //database file is not exist. return null Jeremy '12,5,1

        
        if (mIsInitializing) {
            throw new IllegalStateException("getReadableDatabase called recursively");
        }

        try {
            return getWritableDatabase();
        } catch (SQLiteException e) {
            if (mName == null) throw e;  // Can't open a temp database read-only!
            Log.e(TAG, "Couldn't open " + mName + " for writing (will try read-only):", e);
        }

        SQLiteDatabase db = null;
        try {
        	db = SQLiteDatabase.openDatabase(getDBPath(), null, SQLiteDatabase.OPEN_READONLY
					| SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        }catch(Exception e){
        	return null;  //return null if db opened failed.
        }
        try {
            mIsInitializing = true;
            if (db.getVersion() != mNewVersion) {
                throw new SQLiteException("Can't upgrade read-only database from version " +
                        db.getVersion() + " to " + mNewVersion + ": " + getDBPath());
            }

            onOpen(db);
            Log.w(TAG, "Opened " + mName + " in read-only mode");
            mDatabase = db;
            return mDatabase;
        } finally {
            mIsInitializing = false;
            if (db != null && db != mDatabase) db.close();
        }
    }

    /**
     * Close any open database object.
     */
    public synchronized void close() {
        if (mIsInitializing) throw new IllegalStateException("Closed during initialization");

        if (mDatabase != null && mDatabase.isOpen()) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    //public abstract void onCreate(SQLiteDatabase db);  //Jeremy '12,4,7 We do not need onCreate()

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     *
     * <p>The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    /**
     * Called when the database needs to be downgraded. This is stricly similar to
     * onUpgrade() method, but is called whenever current version is newer than requested one.
     * However, this method is not abstract, so it is not mandatory for a customer to
     * implement it. If not overridden, default implementation will reject downgrade and
     * throws SQLiteException
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    //public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //    throw new SQLiteException("Can't downgrade database from version " +
   //             oldVersion + " to " + newVersion);
    //}

    /**
     * Called when the database has been opened.  The implementation
     * should check {@link SQLiteDatabase#isReadOnly} before updating the
     * database.
     *
     * @param db The database.
     */
    public void onOpen(SQLiteDatabase db) {}
}
