import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Quickstart {

    private static final String PROPERTIES = "quickstart.properties";
    private static final PropertyReader pr = new PropertyReader(PROPERTIES);

    private static final String QUICKSTART_SPREADSHEET_ID = "quickstart.spreadsheet.id";
    private static final String QUICKSTART_GID = "quickstart.gid";
    private static final String QUICKSTART_MODE = "quickstart.mode";

    private static final String GID = prop(QUICKSTART_GID);
    private static final String SPREADSHEET_ID = prop(QUICKSTART_SPREADSHEET_ID);
    private static final String MODE = prop(QUICKSTART_MODE);

    private static final String MODE_WRITE = "write";
    private static final String MODE_READ = "read";

    private static Sheets.Spreadsheets spreadsheets;

    static {
        try {
            spreadsheets = Auth.getSheetsService().spreadsheets();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static List<String> docValues() {
        Scanner scan = getScan();
        if (scan == null) {
            return null;
        }

        List<String> strs = new ArrayList<>();
        int i = 0;
        for (; scan.hasNext(); i++) {
            strs.add(scan.nextLine());
            scan.reset();
        }
        return i == 0 ? null : strs;
    }

    public static void main(String...args) {
        try {
            if (MODE.equalsIgnoreCase(MODE_WRITE)) {
                write(docValues());
            } else if (MODE.equalsIgnoreCase(MODE_READ)){
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
            return new Scanner(new File("/home/renan.souza/doc-" + prop("quickstart.mode")));
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

        List<List<Object>> values = spreadsheets.values().get(SPREADSHEET_ID, sheetId.getDesc()).execute().getValues();
        if (values == null || values.size() == 0) {
            return;
        }

        for (List row : values) {
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
            List<CellData> cells = new ArrayList<>();
            for (String cellValue : arg.split(",")) {
                cells.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(cellValue)));
            }

            rows.add(new RowData().setValues(cells));
        }
        spreadsheets.batchUpdate(SPREADSHEET_ID, request(rows)).execute();
    }

    private static BatchUpdateSpreadsheetRequest request(List<RowData> rows) {
        Integer id = getSheetId().getValue();
        AppendCellsRequest append = new AppendCellsRequest().setSheetId(id).setRows(rows).setFields("userEnteredValue");
        return new BatchUpdateSpreadsheetRequest().setRequests(Arrays.asList(new Request().setAppendCells(append)));
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