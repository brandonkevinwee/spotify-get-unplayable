package spotify.oauth2.server;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import fi.iki.elonen.NanoHTTPD;

public class CallbackServer extends NanoHTTPD {
  private static CallbackServer callbackServer;
  private String authorizationCode;
  private CountDownLatch latch;

  private CallbackServer() {
    super(8080);
  }

  public static CallbackServer getInstance() {
    if (callbackServer == null) callbackServer = new CallbackServer();
    return callbackServer;
  }

  public void start(CountDownLatch latch) throws IOException {
    super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    this.latch = latch;
  }

  @Override
  public Response serve(IHTTPSession session) {
    this.setAuthorizationCode(session.getQueryParameterString());
    String msg = "<html><body><h1>Callback Server</h1></body></html>\n";
    return newFixedLengthResponse(msg);
  }

  public String getAuthorizationCode() {
    return authorizationCode;
  }

  private void setAuthorizationCode(String query) {
    authorizationCode =
        Stream.of(query.split("&"))
            .map(s -> s.split("="))
            .filter(pair -> pair[0].equals("code"))
            .map(pair -> pair[1])
            .findAny()
            .orElseThrow(
                () -> new IllegalArgumentException("No code found in querystring " + query));
    latch.countDown();
  }
}
