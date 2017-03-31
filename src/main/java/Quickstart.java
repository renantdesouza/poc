import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Quickstart {

    private static final String PROPERTIES = "quickstart.properties";
    private static final PropertyReader pr = new PropertyReader(PROPERTIES);

    private static final String QUICKSTART_SPREADSHEET_ID = "quickstart.spreadsheet.id";
    private static final String QUICKSTART_GID = "quickstart.gid";
    private static final String QUICKSTART_MODE = "quickstart.mode";

    private static final String GID = pr.stringProp(QUICKSTART_GID);
    private static final String SPREADSHEET_ID = pr.stringProp(QUICKSTART_SPREADSHEET_ID);
    public static final String MODE = pr.stringProp(QUICKSTART_MODE);

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

    public static void main(String...args) {
        try {
            if (MODE.equalsIgnoreCase(MODE_WRITE)) {
                write(DocumentAccess.scannedValues());
            } else if (MODE.equalsIgnoreCase(MODE_READ)){
                print();
            } else {
                System.out.println("Invalid value!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void print() throws IOException {
        GoogleSpreadsheetID sheetId = getSheetId();
        if (sheetId == null) return;

        List<List<Object>> values = spreadsheets.values().get(SPREADSHEET_ID, sheetId.getDesc()).execute().getValues();
        if (values == null || values.size() == 0)return;

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
        if (args == null) return;

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
        AppendCellsRequest append = new AppendCellsRequest();
        append.setSheetId(getSheetId().getValue()).setRows(rows).setFields("userEnteredValue");
        List<Request> requests = Arrays.asList(new Request().setAppendCells(append));
        return new BatchUpdateSpreadsheetRequest().setRequests(requests);
    }

    private static GoogleSpreadsheetID getSheetId() {
        try {
            return GoogleSpreadsheetID.findByValue(Integer.parseInt(GID));
        } catch (ArrayIndexOutOfBoundsException | ClassCastException | NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

}