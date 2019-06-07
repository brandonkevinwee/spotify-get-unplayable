package spotify;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import spotify.data.UserAccess;
import spotify.oauth2.token.TokenManager;

public class Launcher {

	public static void main(String[] args) throws Exception {
		String clientId = "Client ID here";
		String clientSecret = "Client Secret here";
		String scopes = "user-read-private user-library-read";
		String authorizationURLString = "https://accounts.spotify.com/authorize";
		String tokenURLString= "https://accounts.spotify.com/api/token";
		
		TokenManager tokenManager = new TokenManager(clientId, clientSecret, scopes, authorizationURLString, tokenURLString);
		String authorizationCode = tokenManager.getAuthorizationCode();
		String accessToken = tokenManager.getTokens(authorizationCode, TokenManager.GRANT_TYPE_AUTHORIZATION_CODE).get("accessToken");
		UserAccess userAccess = new UserAccess(accessToken);
		String country = userAccess.getUserProfile().getString("country");
		List<JSONObject> listOfSavedTracks = userAccess.getUserSavedTracks(country);
		List<JSONObject> listOfUnplayableTracks = listOfSavedTracks.stream().filter(track -> !track.getJSONObject("track").getBoolean("is_playable")).collect(Collectors.toList());
		listOfUnplayableTracks.forEach(track -> System.out.println(track.getJSONObject("track").getString("name")));
	}
}
