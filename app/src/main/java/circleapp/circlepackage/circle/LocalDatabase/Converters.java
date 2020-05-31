//package circleapp.circlepackage.circle.LocalDatabase;
//
//import androidx.room.TypeConverter;
//
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.lang.reflect.Type;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//
//public class Converters
//{
//    @TypeConverter
//    public static HashMap<String, Object> jsonToMap(JSONObject json) throws JSONException {
//        HashMap<String, Object> retMap = new HashMap<String, Object>();
//
//        if(json != JSONObject.NULL) {
//            retMap = toMap(json);
//        }
//        return retMap;
//    }
//    public static HashMap<String, Object> toMap(JSONObject object) throws JSONException {
//        HashMap<String, Object> map = new HashMap<String, Object>();
//
//        Iterator<String> keysItr = object.keys();
//        while(keysItr.hasNext()) {
//            String key = keysItr.next();
//            Object value = object.get(key);
//
//            if(value instanceof JSONArray) {
////                value = toList((JSONArray) value);
//            }
//
//            else if(value instanceof JSONObject) {
//                value = toMap((JSONObject) value);
//            }
//            map.put(key, value);
//        }
//        return map;
//    }
//
//    @TypeConverter
//    public static String fromHashmap(HashMap<String,Boolean> tags)
//    {
//        Gson gson = new Gson();
//        String json = gson.toJson(tags);
//        return json;
//    }
//
//}
