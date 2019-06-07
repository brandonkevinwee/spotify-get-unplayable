package spotify.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserAccess {
  private String accessToken;

  public UserAccess(String accessToken) {
    this.accessToken = accessToken;
  }

  public JSONObject getUserProfile() throws IOException {
    OkHttpClient okHTTPClient = new OkHttpClient();
    HttpUrl userDataURL = HttpUrl.parse("https://api.spotify.com/v1/me").newBuilder().build();
    Request userDataRequest =
        new Request.Builder()
            .url(userDataURL)
            .addHeader("Authorization", " Bearer " + accessToken)
            .build();
    Response userDataResponse = okHTTPClient.newCall(userDataRequest).execute();
    return new JSONObject(userDataResponse.body().string());
  }

  public List<JSONObject> getUserSavedTracks(String country) throws IOException {
    OkHttpClient okHTTPClient = new OkHttpClient();
    String market = country;
    int offset = 0;
    int limit = 50;
    int total = 0;
    List<JSONObject> userSavedTracks = new ArrayList<JSONObject>();
    do {
      HttpUrl userSavedTracksURL =
          HttpUrl.parse("https://api.spotify.com/v1/me/tracks")
              .newBuilder()
              .addQueryParameter("market", market)
              .addQueryParameter("offset", Integer.toString(offset))
              .addQueryParameter("limit", Integer.toString(limit))
              .build();
      Request userSavedTracksRequestPage =
          new Request.Builder()
              .url(userSavedTracksURL)
              .addHeader("Authorization", "Bearer " + accessToken)
              .build();
      Response userSavedTracksResponsePage =
          okHTTPClient.newCall(userSavedTracksRequestPage).execute();
      JSONObject userSavedTracksPage = new JSONObject(userSavedTracksResponsePage.body().string());
      if (total == 0) total = userSavedTracksPage.getInt("total");
      JSONArray userSavedTracksPageArray = userSavedTracksPage.getJSONArray("items");
      for (int i = 0; i < userSavedTracksPageArray.length(); i++) {
        userSavedTracks.add(userSavedTracksPageArray.getJSONObject(i));
      }
      offset += 50;
    } while (offset < total);
    return userSavedTracks;
  }
}
