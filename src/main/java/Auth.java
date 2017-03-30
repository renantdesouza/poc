import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;

public class Auth {

    private static final JsonFactory FACTORY = JacksonFactory.getDefaultInstance();

    public static Credential authorize() throws IOException, GeneralSecurityException {
        InputStream in = Quickstart.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets secrets = GoogleClientSecrets.load(FACTORY, new InputStreamReader(in));

        File file = new File(System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart");
        FileDataStoreFactory dataStore = new FileDataStoreFactory(file);

        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();

        Collection<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);
        GoogleAuthorizationCodeFlow.Builder b = new GoogleAuthorizationCodeFlow.Builder(transport, FACTORY, secrets, scopes);
        GoogleAuthorizationCodeFlow flow = b.setDataStoreFactory(dataStore).setAccessType("offline").build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        Sheets.Builder b = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), FACTORY, authorize());
        return b.setApplicationName("Sheets API").build();
    }

}
