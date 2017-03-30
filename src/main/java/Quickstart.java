import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Quickstart {

    private static final String PROPERTIES = "quickstart.properties";
    private static final PropertyReader pr = new PropertyReader(PROPERTIES);

    private static final String GID = prop("quickstart.gid");
    private static final String SPREADSHEET_ID = prop("quickstart.spreadsheet.id");
    private static final String MODE = prop("quickstart.mode");

    private static Sheets.Spreadsheets spreadsheets;

    static {
        try {
            spreadsheets = Auth.getSheetsService().spreadsheets();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
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
        boolean isWrite = MODE.equalsIgnoreCase("write");
        boolean isRead = MODE.equalsIgnoreCase("read");

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
        GoogleSpreadsheetID sheetId = getSheetId();
        if (sheetId == null) {
            return;
        }

        String range = sheetId.getDesc();

        List<List<Object>> values = spreadsheets.values().get(prop("quickstart.spreadsheet.id"), range).execute().getValues();
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

        List<RowData> rows = new ArrayList<>();
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
        Integer id = getSheetId().getValue();

        AppendCellsRequest append = new AppendCellsRequest().setSheetId(id).setRows(rows).setFields("userEnteredValue");
        List<Request> requests = Arrays.asList(new Request().setAppendCells(append));
        spreadsheets.batchUpdate(SPREADSHEET_ID, new BatchUpdateSpreadsheetRequest().setRequests(requests)).execute();
    }

    private static GoogleSpreadsheetID getSheetId() {
        try {
            return GoogleSpreadsheetID.findByValue(Integer.parseInt(GID));
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