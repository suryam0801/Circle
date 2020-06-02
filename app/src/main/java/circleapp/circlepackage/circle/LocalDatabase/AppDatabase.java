//package circleapp.circlepackage.circle.LocalDatabase;
//
//import android.content.Context;
//import android.util.Log;
//
//import androidx.room.Database;
//import androidx.room.Room;
//import androidx.room.RoomDatabase;
//import androidx.room.TypeConverters;
//
//import circleapp.circlepackage.circle.ObjectModels.User;
//import circleapp.circlepackage.circle.ObjectModels.UserStorage;
//
//@Database(entities = {UserStorage.class},version = 1,exportSchema = false)
////@TypeConverters({Converters.class})
//public abstract class AppDatabase extends RoomDatabase {
//    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
//    private static final Object LOCK = new Object();
//    private static final String DATABASE_NAME = "userslist";
//    private static AppDatabase sInstance;
//    public static AppDatabase getInstance(Context context) {
//        if (sInstance == null) {
//            synchronized (LOCK) {
//                Log.d(LOG_TAG, "Creating new database instance");
//                sInstance = Room.databaseBuilder(context.getApplicationContext(),
//                        AppDatabase.class, AppDatabase.DATABASE_NAME)
//                        .build();
//            }
//        }
//        Log.d(LOG_TAG, "Getting the database instance");
//        return sInstance;
//    }
//
//    public abstract UserDao userDao();
//}
