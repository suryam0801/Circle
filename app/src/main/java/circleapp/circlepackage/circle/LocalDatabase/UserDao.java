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
//
//@Dao
//public interface UserDao {
//
//
//    @Query("SELECT * FROM users")
//    List<User> loadalluser();
//
//    @Insert
//    void insertUser(User user);
//
//    @Update
//    void updateUser(User user);
//
//    @Delete
//    void delete(User user);
//
//    @Query("SELECT * FROM users WHERE userId =:uid")
//    User loadUser(String uid);
//
//}
//
