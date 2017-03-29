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

    private static final String MODE = "-write" /*"-read"*/;

     private static final String NUMBER_REGEX = "^(\\d+(?:[\\.\\,]\\d{1,2})?)$";
    private static final String DATE_REGEX = "[0-9]{2}\\/[0-9]{2}\\/[0-9]{2,4}";

    private static Sheets sheets;
    private static Sheets.Spreadsheets spreadsheets;

    static {
        try {
            sheets = getSheetsService();
            spreadsheets = sheets.spreadsheets();
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

        System.out.println("Credentials saved");
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), FACTORY, authorize()).setApplicationName("Sheets API").build();
    }

    private static List<String> docValues(Scanner scan) {
        List<String> strs = new ArrayList<>();
        while (scan.hasNext()) {
            String s = scan.next();
            if (s == null || s.equals(":q")) {
                break;
            } else {
                strs.add(s);
            }
            scan.reset();
        }
        return strs;
    }

    private static String[] toArray(List<String> strs) {
        String[] array = new String[strs.size()];
        int index = 0;
        for (String str : strs) {
            array[index++] = str;
        }
        return array;
    }

    public static void main(String...args) {
        Scanner scan = getScan();
        if (scan == null) {
            return;
        }

        String s = scan.next();
        try  {
            if (s.equalsIgnoreCase("r")) {
                safePrint(toArray(docValues(scan)));
            } else if (s.equalsIgnoreCase("w")) {
                safeWrite(toArray(docValues(scan)));
            } else {
                System.out.println("Invalid value!");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static Scanner getScan() {
        try {
            return new Scanner(new File("/home/renan.souza/doc" + MODE));
        } catch (FileNotFoundException fnfe) {
            System.out.print("File not found");
            return null;
        }
    }

    private static void safePrint(String...args) throws IOException {
        String id = "1R9kkICgd6y7T152IvJ4g5yQDnk7NmzKkuwnyw660FwA";

        GoogleSpreadsheetID sheetId = safeGetSheetId(args);
        if (sheetId == null) {
            return;
        }

        String range = sheetId.getDesc();

        List<List<Object>> values = spreadsheets.values().get(id, range).execute().getValues();

        if (values == null || values.size() == 0) {
            System.out.println("No data found.");
            return;
        }

        for (List row : values) {
            if (row == null) {
                System.out.println("Error");
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

    private static void safeWrite(String...args) throws IOException {
        if (args == null) {
            return;
        }

        Integer id = null;
        if (args.length > 0) {
            GoogleSpreadsheetID sheetId = safeGetSheetId(args);
            if (sheetId == null) {
                return;
            }
            id = sheetId.getValue();
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        String spreadsheetId = "1R9kkICgd6y7T152IvJ4g5yQDnk7NmzKkuwnyw660FwA";

        BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest();
        for (String arg : args) {
            CellData cell = new CellData();
            cell.setUserEnteredValue(new ExtendedValue().setStringValue(arg));

            String fields = "userEnteredValue" + (validate(arg, cell) ? ", userEnteredFormat.numberFormat" : "");

            List<CellData> cells = Arrays.asList(cell);
            List<RowData> rows = Arrays.asList(new RowData().setValues(cells));

            AppendCellsRequest appendRequest = new AppendCellsRequest();
            appendRequest.setSheetId(id).setRows(rows).setFields(fields);

            batchRequest.setRequests(Arrays.asList(new Request().setAppendCells(appendRequest)));

            spreadsheets.batchUpdate(spreadsheetId, batchRequest).execute();
        }
    }

    private static GoogleSpreadsheetID safeGetSheetId(String...args) {
        try {
            return GoogleSpreadsheetID.findByValue(Integer.parseInt(args[0]));
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean validate(String arg, CellData cell) {
        NumberFormat nf = new NumberFormat();
        if (arg.matches(NUMBER_REGEX)) {
            nf.setType("NUMBER");
        } else if (arg.replaceAll("-", "/").matches(DATE_REGEX)) {
            nf.setType("DATE");
        } else {
            return false;
        }

        cell.setUserEnteredFormat(new CellFormat().setNumberFormat(nf));
        return true;
    }

}