package com.crossover.sheet;

import com.crossover.util.ArgUtil;
import com.crossover.util.Utils;
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
import com.google.api.services.sheets.v4.model.*;
import net.sourceforge.argparse4j.inf.Namespace;
import org.springframework.format.datetime.DateFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.*;

public class GoogleSheetHelper {
    private static final String APPLICATION_NAME = "Genba";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    //Headers
    private static final String CANDIDATE_NAME = "Candidate Name";
    private static final String DATE = "Date";
    private static final String DAY = "Day";
    private static final String ACTIVITY_LEVEL = "Activity Level";
    private static final String MOUSE_EVENTS = "Mouse Events";
    private static final String KEYBOARD_EVENTS = "Keyboard Events";
    private static final String INTENSITY_SCORE = "Intensity Score";
    private static final String WINDOW_TITLE = "Window Title";
    private static final String CURRENT_MANAGER = "Current Manager";
    private static final String MOMENT_MANAGER = "Moment Manager";
    private static final String LOCAL_HOUR = "Local Hour";

    private static final String DATA_SHEET = "Sheet1";
    private static final String QUERY_SHEET = "Sheet3";
    private static final String ALL_DATA_RANGE = DATA_SHEET + "!A1:K";
    private static final String ALL_DATA_RANGE_CANDIDATE = DATA_SHEET + "!A";
    private static final String DATA_RANGE_WITHOUT_TITLE = DATA_SHEET + "!A2:B";
    private static final String QUERY_HOUR_TEMPLATE_RANGE = QUERY_SHEET + "!A1";
    private static final String DATA_TITLE_RANGE = DATA_SHEET + "!A1";

    private static final String RAW = "RAW";
    private static final String INSERT_ROWS = "INSERT_ROWS";
    private static final String ROWS = "ROWS";

    private static final int GOOGLE_AUTH_PORT = 8888;
    private static final String AUTH_USER = "user";
    private static final String ACCESS_TYPE = "offline";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleSheetHelper.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType(ACCESS_TYPE)
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(GOOGLE_AUTH_PORT).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize(AUTH_USER);
    }

    private static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        return service;
    }

    public static int getRowCountOfSheet(Namespace namespace) throws IOException, GeneralSecurityException {
        final String spreadsheetId = Utils.getProperties(namespace.get(ArgUtil.CREDENTIAL)).getProperty(ArgUtil.SHEET_ID);
        Sheets service = getSheetsService();
        ValueRange body = service.spreadsheets().values().get(spreadsheetId, ALL_DATA_RANGE).execute();
        int lastRow = body.getValues().size();
        return lastRow;
    }


    public static void delete(Namespace namespace) throws IOException, GeneralSecurityException, ParseException {
        final String spreadsheetId = Utils.getProperties(namespace.get(ArgUtil.CREDENTIAL)).getProperty(ArgUtil.SHEET_ID);
        final String strDate = namespace.getString(ArgUtil.START_DATE);
        final String endDate = namespace.getString(ArgUtil.END_DATE);
        List<Integer> deleteList = findRowsOnSheet(spreadsheetId, Utils.stringToDate(strDate), Utils.stringToDate(endDate));
        if (deleteList != null && !deleteList.isEmpty()) {
            deleteDimension(spreadsheetId, deleteList.get(0), deleteList.get(deleteList.size() - 1) + 1);
        }
    }

    public static void write(final Namespace namespace, List<LinkedHashMap<Object, Object>> values)
            throws IOException, GeneralSecurityException {
        final String spreadsheetId = Utils.getProperties(namespace.get(ArgUtil.CREDENTIAL)).getProperty(ArgUtil.SHEET_ID);
        Sheets service = getSheetsService();
        updateHeaders(service, spreadsheetId);
        updateTimes(service, spreadsheetId);
        List<List<Object>> appendValues = new ArrayList<>();
        for (LinkedHashMap<Object, Object> l : values) {
            List<Object> list = new ArrayList<>();
            for (Object o : l.values()) {
                list.add(o);
            }
            appendValues.add(list);
        }
        ValueRange body = service.spreadsheets().values().get(spreadsheetId, ALL_DATA_RANGE).execute();
        int lastRow = body.getValues().size();
        String range = ALL_DATA_RANGE_CANDIDATE + (lastRow + 1);
        body = new ValueRange().setValues(appendValues);
        Sheets.Spreadsheets.Values.Append request =
                service.spreadsheets().values().append(spreadsheetId, range, body)
                        .setValueInputOption(RAW)
                        .setInsertDataOption(INSERT_ROWS);

        AppendValuesResponse response = request.execute();
        System.out.printf("%d cells updated.", response.getUpdates().getUpdatedCells());
    }

    public static int countRowsOnSheet(final Namespace namespace, String candidate)
            throws IOException, GeneralSecurityException, ParseException {
        final String spreadsheetId = Utils.getProperties(namespace.get(ArgUtil.CREDENTIAL)).getProperty(ArgUtil.SHEET_ID);
        final String strDate = namespace.getString(ArgUtil.START_DATE);
        final String endDateStr = namespace.getString(ArgUtil.END_DATE);
        final Date startDate = Utils.stringToDate(strDate);
        final Date endDate = Utils.stringToDate(endDateStr);
        Sheets service = getSheetsService();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, DATA_RANGE_WITHOUT_TITLE)
                .execute();
        List<List<Object>> values = response.getValues();
        int count = 0;
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).get(0) != null && values.get(i).get(1) != null) {
                String candidateName = values.get(i).get(0).toString();
                Date clmDate = Utils.stringToDate(values.get(i).get(1).toString());
                if (candidate.equals(candidateName) && clmDate.compareTo(startDate) >= 0 && clmDate.compareTo(endDate) <= 0) {
                    ++count;
                }
            }
        }
        return count;
    }

    private static void updateTimes(final Sheets service, final String spreadsheetId) throws IOException{
        List<String> hourList = new ArrayList<>(), minuteList = new ArrayList<>();
        for(int i=0; i<24; i++){
            hourList.add(i<10?"0"+i:i+"");
        }
        for(int i=0; i<6; i++){
            minuteList.add(i==0?"00":(i*10)+"");
        }
        List<List<Object>> values = new ArrayList<>();
        values.add(Arrays.asList(LOCAL_HOUR));
        for(String h:hourList){
            for(String m:minuteList){
                values.add(Arrays.asList(h+":"+m));
            }
        }
        ValueRange body = new ValueRange()
                .setValues(values);
        UpdateValuesResponse result =
                service.spreadsheets().values().update(spreadsheetId, QUERY_HOUR_TEMPLATE_RANGE, body)
                        .setValueInputOption(RAW)
                        .execute();
        System.out.printf("%d cells updated.", result.getUpdatedCells());
    }

    private static void updateHeaders(final Sheets service, final String spreadsheetId) throws IOException {
        List<List<Object>> values = Arrays.asList(
                Arrays.asList(CANDIDATE_NAME, DATE, ACTIVITY_LEVEL,
                        MOUSE_EVENTS, KEYBOARD_EVENTS, INTENSITY_SCORE,
                        WINDOW_TITLE, CURRENT_MANAGER, MOMENT_MANAGER, DAY, LOCAL_HOUR)
        );
        ValueRange body = new ValueRange()
                .setValues(values);
        UpdateValuesResponse result =
                service.spreadsheets().values().update(spreadsheetId, DATA_TITLE_RANGE, body)
                        .setValueInputOption(RAW)
                        .execute();
        System.out.printf("%d cells updated.", result.getUpdatedCells());
    }

    private static List<Integer> findRowsOnSheet(final String spreadsheetId, Date startDate, Date endDate) throws IOException, GeneralSecurityException, ParseException {
        Sheets service = getSheetsService();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, DATA_RANGE_WITHOUT_TITLE)
                .execute();
        List<List<Object>> values = response.getValues();
        List<Integer> containList = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) != null && values.get(i).size() > 1) {
                Date clmDate = Utils.stringToDate(values.get(i).get(1).toString());
                if (clmDate.compareTo(startDate) >= 0 && clmDate.compareTo(endDate) <= 0) {
                    containList.add(i + 1);
                }
            }
        }
        return containList;
    }

    private static void deleteDimension(final String spreadsheetId, int startIndex, int endIndex)
            throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();
        Request request = new Request()
                .setDeleteDimension(new DeleteDimensionRequest()
                        .setRange(new DimensionRange()
                                .setSheetId(0)
                                .setDimension(ROWS)
                                .setStartIndex(startIndex)
                                .setEndIndex(endIndex)
                        )
                );
        content.setRequests(Arrays.asList(request));
        BatchUpdateSpreadsheetResponse response = service.spreadsheets().batchUpdate(spreadsheetId, content).execute();
        System.out.println("Rows are deleted from spreadsheet : " + response.getSpreadsheetId());
    }

    public static void main(String...args) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        updateTimes(service,"13IZShJa96dp_y2TGyntJCbPhAW_zl5OgxiHQsvhkkEU");
    }

}
