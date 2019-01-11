package com.crossover.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class ArgUtil {

    public static final SimpleDateFormat CONSOLE_DATE_FORMAT = new SimpleDateFormat(Utils.DATE_FORMAT);
    public static final String DELETE = "delete";
    public static final String APPEND = "append";
    public static final String GEMBA_HELPER = "gembahelper";
    public static final String FACE_DETECTION = "facedetection";
    public static final String CROSS_USERNAME = "cross.username";
    public static final String CROSS_PASSWORD = "cross.password";
    public static final String TEAM_ROOM_ID = "team.room.id";
    public static final String SHEET_ID = "sheet.id";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String CREDENTIAL = "credential";
    public static final String TYPE = "type";
    public static final String MODE = "mode";

    public static Namespace checkArgs(String[] args) {
        Namespace ns = null;
        ArgumentParser parser = null;
        try {
            parser = initialize();
            ns = parser.parseArgs(args);
            if (!checkDate(ns.getString(START_DATE)) || !checkDate(ns.getString(END_DATE))
                    || !checkFile(ns.get(CREDENTIAL)) || !checkType(ns.getString(TYPE))) {
                return null;
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return ns;
    }

    public static boolean isAppendMode(Namespace namespace) {
        String mode = namespace.getString(MODE);
        if (mode == null || APPEND.equals(mode)) {
            return true;
        }
        return false;
    }

    private static ArgumentParser initialize() {
        ArgumentParser parser = ArgumentParsers.newFor("Genba").build().defaultHelp(true)
                .description("Get your WS data.");
        parser.addArgument("--startDate").dest(START_DATE).type(String.class).required(true)
                .help("WS start date");
        parser.addArgument("--endDate").dest(END_DATE).type(String.class).required(true)
                .help("WS end date");
        parser.addArgument("--credential").dest(CREDENTIAL).type(File.class).required(true)
                .help("Credential properties file.");
        parser.addArgument("--mode").dest(MODE).type(String.class).choices(DELETE, APPEND)
                .required(false).help("Optional (Default is append.)");
        parser.addArgument("--type").dest(TYPE).type(String.class).choices(GEMBA_HELPER, FACE_DETECTION)
                .required(false).help("Optional (Default is gembahelper.)");
        return parser;
    }

    private static boolean checkType(String type){
        if(type != null && FACE_DETECTION.equals(type)){
            System.err.println("Face detection operation is not supported for now.");
            return false;
        }
        return true;
    }

    private static boolean checkDate(String date) throws ParseException {
        try{
            CONSOLE_DATE_FORMAT.parse(date);
        }catch (ParseException e){
            throw new ParseException(e.getMessage(), 0);
        }
        return true;
    }

    private static boolean checkFile(File file) throws IOException {
        if(file == null){
            System.err.println("File is required.");
            return false;
        }
        if (!file.exists()) {
            throw new IOException("File is not exist.");
        }
        if(!file.getName().endsWith(".properties")){
            System.err.println("File is not a supported type. It should be properties file");
            return false;
        }
        Properties prop = Utils.getProperties(file);
        if(prop==null){
            System.err.println("File is not a supported type. It should be properties file");
            return false;
        }else if(!prop.containsKey(CROSS_USERNAME) || !prop.containsKey(CROSS_PASSWORD)
                || !prop.containsKey(TEAM_ROOM_ID) || !prop.containsKey(SHEET_ID)){
            System.err.println("File should include " + CROSS_USERNAME + ", " + CROSS_PASSWORD + ", " + TEAM_ROOM_ID
                    + ", " + SHEET_ID);
            return false;
        }
        return true;
    }

}

