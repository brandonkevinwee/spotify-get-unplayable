package spotify.oauth2.token;

import java.awt.Desktop;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import spotify.oauth2.server.CallbackServer;

public class TokenManager {
	private String clientId;
	private String clientSecret;
	private String scopes;
	private String authorizationURLString;
	private String tokenURLString;
	private final String responseType = "code";
	private final String callbackServerURI = "http://localhost:8080";
	public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
	public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

	public TokenManager(String clientId, String clientSecret, String scopes, String authorizationURLString,
			String tokenURLString) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.scopes = scopes;
		this.authorizationURLString = authorizationURLString;
		this.tokenURLString = tokenURLString;
	}

	public Map<String, String> getTokens(String authorizationCode, String grantType) throws Exception {
		Map<String, String> formData = new HashMap<String, String>();
		formData.put("client_id", clientId);
		formData.put("client_secret", clientSecret);
		formData.put("redirect_uri", callbackServerURI);
		if (grantType.equals(GRANT_TYPE_AUTHORIZATION_CODE)) {
			formData.put("code", authorizationCode);
			formData.put("grant_type", "authorization_code");
		} else if (grantType.equals(GRANT_TYPE_REFRESH_TOKEN)) {
			formData.put("refresh_token", authorizationCode);
			formData.put("grant_type", "refresh_token");
		}

		OkHttpClient okHttpClient = new OkHttpClient();
		RequestBody tokenRequestBody = RequestBody.create(MediaType.get("application/x-www-form-urlencoded"),
				getURLEncodedFormData(formData));
		Request tokenRequest = new Request.Builder().url(tokenURLString).post(tokenRequestBody).build();
		Response tokenResponse = okHttpClient.newCall(tokenRequest).execute();
		JSONObject tokenResponseJSON = new JSONObject(tokenResponse.body().string());
		Map<String, String> tokens = new HashMap<String, String>();
		String accessToken = tokenResponseJSON.getString("access_token");
		String refreshToken;
		try {
			refreshToken = tokenResponseJSON.getString("refresh_token");
		} catch (JSONException e) {
			refreshToken = authorizationCode;
		}
		tokens.put("accessToken", accessToken);
		tokens.put("refreshToken", refreshToken);
		return tokens;
	}

	private String getURLEncodedFormData(Map<String, String> formData) {
		StringBuilder urlEncodedFormData = new StringBuilder();
		formData.forEach((parameter, value) -> urlEncodedFormData.append(parameter + "=" + value + "&"));
		return urlEncodedFormData.substring(0, urlEncodedFormData.length());
	}

	public String getAuthorizationCode() throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		CallbackServer callbackServer = CallbackServer.getInstance();
		callbackServer.start(latch);
		Desktop.getDesktop().browse(getAuthorizationURI());
		latch.await();
		return callbackServer.getAuthorizationCode();
	}

	private URI getAuthorizationURI() throws Exception {
		HttpUrl authorizationURL = HttpUrl.parse(authorizationURLString).newBuilder()
				.addQueryParameter("client_id", clientId).addQueryParameter("response_type", responseType)
				.addQueryParameter("redirect_uri", callbackServerURI).addQueryParameter("scope", scopes).build();
		return authorizationURL.uri();
	}
}
