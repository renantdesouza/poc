import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Quickstart {

    private static final JsonFactory FACTORY = JacksonFactory.getDefaultInstance();

    private static final String PROPERTIES = "quickstart.properties";

    private static Sheets sheets;
    private static Sheets.Spreadsheets spreadsheets;

    private static PropertyReader pr;

    static {
        try {
            sheets = getSheetsService();
            spreadsheets = sheets.spreadsheets();
            pr = new PropertyReader(PROPERTIES);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public static Credential authorize() throws IOException, GeneralSecurityException {
        InputStream in = Quickstart.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets secrets = GoogleClientSecrets.load(FACTORY, new InputStreamReader(in));
        File file = new File(System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart");

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(GoogleNetHttpTransport.newTrustedTransport(), FACTORY, secrets, Arrays.asList(SheetsScopes.SPREADSHEETS))
                .setDataStoreFactory(new FileDataStoreFactory(file))
                .setAccessType("offline")
                .build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), FACTORY, authorize()).setApplicationName("Sheets API").build();
    }

    private static List<String> docValues(Scanner scan) {
        if (scan == null) {
            return null;
        }

        List<String> strs = new ArrayList<>();
        while (scan.hasNext()) {
            String s = scan.nextLine();
            if (s == null) {
                break;
            }
            strs.add(s);
            scan.reset();
        }
        return strs;
    }

    public static void main(String...args) {
        String mode = prop("quickstart.mode");
        if (mode == null) {
            return;
        }

        boolean isWrite = mode.equalsIgnoreCase("write");
        boolean isRead = mode.equalsIgnoreCase("read");

        try {
            if (isWrite) {
                Scanner scan = getScan();
                if (scan == null) {
                    return;
                }

                write(docValues(scan));
            } else if (isRead){
                print();
            } else {
                System.out.println("Invalid value!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Scanner getScan() {
        try {
            String path = "/home/renan.souza/doc-" + prop("quickstart.mode");
            return new Scanner(new File(path));
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            return null;
        }
    }

    private static void print() throws IOException {
        String id = prop("quickstart.spreadsheetId");

        GoogleSpreadsheetID sheetId = getSheetId();
        if (sheetId == null) {
            return;
        }

        String range = sheetId.getDesc();

        List<List<Object>> values = spreadsheets.values().get(id, range).execute().getValues();

        if (values == null || values.size() == 0) {
            return;
        }

        for (List row : values) {
            if (row == null) {
                continue;
            }

            String str = "";
            int size = row.size();
            for (int i = 0; i < size; i++) {
                str += i == size-1 ? row.get(i) : row.get(i) + ", ";
            }
            System.out.println(str);
        }
    }

    private static void write(List<String> args) throws IOException {
        if (args == null) {
            return;
        }

        String spreadsheetId = "1R9kkICgd6y7T152IvJ4g5yQDnk7NmzKkuwnyw660FwA";
        Integer id = getSheetId().getValue();

        List<RowData> rows = new ArrayList<>();
        List<Request> requests = new ArrayList<>();
        for (String arg : args) {
            String[] splited = arg.split(",");
            if (splited == null) {
                return;
            }

            List<CellData> cells = new ArrayList<>();
            for (String s : splited) {
                cells.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(s)));
            }

            rows.add(new RowData().setValues(cells));
        }
        AppendCellsRequest append = new AppendCellsRequest().setSheetId(id).setRows(rows).setFields("userEnteredValue");
        requests.add(new Request().setAppendCells(append));
        spreadsheets.batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest().setRequests(requests)).execute();
    }

    private static GoogleSpreadsheetID getSheetId() {
        try {
            return GoogleSpreadsheetID.findByValue(Integer.parseInt(prop("quickstart.gid")));
        } catch (ArrayIndexOutOfBoundsException | ClassCastException | NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String prop(String name) {
        try {
            return (String) pr.prop(name);
        } catch (ClassCastException e) {
            return null;
        }
    }

}