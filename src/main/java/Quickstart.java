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

    private static final boolean canWrite = false;

    private static final String MODE = canWrite ? "-write" : "-read";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

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
        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        File file = new File(System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart");

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, secrets, Arrays.asList(SheetsScopes.SPREADSHEETS))
                .setDataStoreFactory(new FileDataStoreFactory(file))
                .setAccessType("offline")
                .build();

        System.out.println("Credentials saved");
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, authorize()).setApplicationName("Sheets API").build();
    }

    public static void main(String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("/home/renan.souza/doc" + MODE));
        String s = scanner.next();
        System.out.println("Valor selecionado - " + s);

        try {
            Integer i = Integer.parseInt(s);
            if (i == 1) {
                print();
            } else if (i == 2) {
                List<String> strs = new ArrayList<>();
                while(true) {
                    s = scanner.next();
                    if (s == null || s.equals(":q")) {
                        break;
                    } else {
                        strs.add(s);
                    }
                    scanner.reset();
                }

                String[] array = new String[strs.size()];
                int index = 0;
                for (String str : strs) {
                    array[index++] = str;
                }

                write(array);
            } else {
                System.out.println("Erro!");
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private static void print() throws IOException {
        String id = "1R9kkICgd6y7T152IvJ4g5yQDnk7NmzKkuwnyw660FwA";
        String range = "teste!A1:B";

        List<List<Object>> values = spreadsheets.values().get(id, range).execute().getValues();

        if (values == null || values.size() == 0) {
            System.out.println("No data found.");
            return;
        }

        int line = 1;
        for (List row : values) {
            try {
                if (row == null) {
                    System.out.println("Erro");
                } else {
                    String str = "";
                    int size = row.size();
                    for (int i = 0; i < size; i++) {
                        str += i == size-1 ? row.get(i) : row.get(i) + ", ";
                    }
                    System.out.println(str);
                }
            } catch (IndexOutOfBoundsException ignored) {
                System.out.println("Erro ao imprimir linha " + line);
            }
        }
    }

    private static void write(String...args) throws IOException {
        String spreadsheetId = "1R9kkICgd6y7T152IvJ4g5yQDnk7NmzKkuwnyw660FwA";

        BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest();

        for (String arg : args) {
            CellData cell = new CellData();
            cell.setUserEnteredValue(new ExtendedValue().setStringValue(arg));

            String fields = "userEnteredValue" + (validate(arg, cell) ? ", userEnteredFormat.numberFormat" : "");

            CellData value = new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("XABLAU"));

            AppendCellsRequest appendRequest = new AppendCellsRequest();
            appendRequest.setSheetId(0).setRows(Arrays.asList(new RowData().setValues(Arrays.asList(cell, value)))).setFields(fields);

            batchRequest.setRequests(Arrays.asList(new Request().setAppendCells(appendRequest)));

            spreadsheets.batchUpdate(spreadsheetId, batchRequest).execute();
        }
    }

    private static boolean validate(String arg, CellData cell) {
        NumberFormat nf = new NumberFormat();
        if (arg.matches("^(\\d+(?:[\\.\\,]\\d{1,2})?)$")) {
            nf.setType("NUMBER");
        } else if (arg.replaceAll("-", "/").matches("[0-9]{2}\\/[0-9]{2}\\/[0-9]{2,4}")) {
            nf.setType("DATE");
        } else {
            return false;
        }

        cell.setUserEnteredFormat(new CellFormat().setNumberFormat(nf));
        return true;
    }

}