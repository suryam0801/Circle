package circleapp.circlepackage.circle.Helpers;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Api {
    @FormUrlEncoded
    @POST("send")
    Call<ResponseBody> sendpushNotification(
            @Field("token") String token,
            @Field("title") String title,
            @Field("body") String body,
            @Field("data") String data
    );
}
