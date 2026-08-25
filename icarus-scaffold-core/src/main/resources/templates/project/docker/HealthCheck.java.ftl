import java.net.HttpURLConnection;
import java.net.URI;

/** Minimal JRE-only container health probe; no curl/wget package is required. */
public final class HealthCheck {

    private HealthCheck() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.exit(2);
        }
        HttpURLConnection connection = (HttpURLConnection) URI.create(args[0]).toURL().openConnection();
        connection.setConnectTimeout(2_000);
        connection.setReadTimeout(2_000);
        connection.setRequestMethod("GET");
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            System.exit(1);
        }
    }
}
