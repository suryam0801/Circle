//package circleapp.circlepackage.circle.LocalDatabase;
//
//import androidx.room.Dao;
//import androidx.room.Delete;
//import androidx.room.Insert;
//import androidx.room.Query;
//import androidx.room.TypeConverter;
//import androidx.room.Update;
//
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//
//import java.lang.reflect.Type;
//import java.util.HashMap;
//import java.util.List;
//
//import circleapp.circlepackage.circle.ObjectModels.User;
//import circleapp.circlepackage.circle.ObjectModels.UserStorage;
//
//@Dao
//public interface UserDao {
//
//
//    @Query("SELECT * FROM user")
//    List<UserStorage> loadalluser();
//
//    @Insert
//    void insertUser(String user);
//
//    @Update
//    void updateUser(String user);
//
//    @Delete
//    void delete(String user);
//
//    @Query("SELECT * FROM user WHERE id =:id")
//    User loadUser(int id);
//
//}
//
