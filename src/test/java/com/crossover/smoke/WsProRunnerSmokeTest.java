package com.crossover.smoke;

import com.crossover.sheet.GoogleSheetHelper;
import com.crossover.util.ArgUtil;
import com.crossover.util.Utils;
import com.crossover.wspro.WSProRunner;
import com.crossover.wspro.pack.XOService;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class WsProRunnerSmokeTest {

    private static final String CREDENTIALS_PROPERTIES =
            "C:\\Users\\user\\Desktop\\BC-WSPro-Work-Pattern-Analyzer\\credentials.properties";
    private static final String START_DATE = "2019-01-01";
    private static final String END_DATE = "2019-01-01";
    private static final String DELETE_MODE = "delete";
    private static final int TEST_CANDIDATE_ID = 65548;

    @Test
    public void allRecordsShouldBeTaken() throws Exception{
        final String[] args = {"--startDate", START_DATE, "--endDate", END_DATE, "--credential",
                CREDENTIALS_PROPERTIES};
        final Namespace namespace = ArgUtil.checkArgs(args);

        clearSheet();
        generateSheet();
        Object[] candidateStats = getPairOfCandidate(getUrlForSummary(namespace), namespace);
        final int count = GoogleSheetHelper.countRowsOnSheet(namespace, candidateStats[0].toString());
        int timing = (int) candidateStats[1];

        Assert.assertTrue(count== timing || count==(timing+1));
    }

    private Object[] getPairOfCandidate(String url, Namespace namespace) throws Exception {
        List<LinkedHashMap> map = (List<LinkedHashMap>) new XOService().getEntity(List.class,url,namespace);
        Double hours = 0.0 ;
        String candidate = null;
        for(LinkedHashMap<String, Object> lhm: map){
            int assignmentId = (int) lhm.get("assignmentId");
            if(TEST_CANDIDATE_ID == assignmentId){
                List<LinkedHashMap> list = (List<LinkedHashMap>) lhm.get("stats");
                for(LinkedHashMap<String, Object> s : list){
                    Date statDate = Utils.stringToDate(s.get("date").toString());
                    if(statDate.compareTo(Utils.stringToDate(START_DATE))==0){
                        hours = (Double) s.get("hours");
                        candidate = lhm.get("name").toString();
                        break;
                    }
                }
            }
        }
        int timing = (int) ((hours * 60)/10);
        Object[] objects = {candidate,timing};
        return objects;
    }

    private Map<String, Integer> getAllCandidateTimings(String url, Namespace namespace) throws Exception {
        Map<String, Integer> summaryMap = new LinkedHashMap<>();
        List<LinkedHashMap> map = (List<LinkedHashMap>) new XOService().getEntity(List.class,url,namespace);
        for(LinkedHashMap<String, Object> lhm: map){
                List<LinkedHashMap> list = (List<LinkedHashMap>) lhm.get("stats");
                for(LinkedHashMap<String, Object> s : list){
                    Date statDate = Utils.stringToDate(s.get("date").toString());
                    if(statDate.compareTo(Utils.stringToDate(START_DATE))==0){
                        Double hours = (Double) s.get("hours");
                        summaryMap.put(lhm.get("name").toString(), (int) ((hours * 60)/10));
                    }
                }
        }
        return summaryMap;
    }

    private String getUrlForSummary(final Namespace namespace) throws Exception{
        String firstDay = Utils.dateToString(Utils.getFirstDayOfWeek(START_DATE));
        Properties prop = Utils.getProperties(namespace.get(ArgUtil.CREDENTIAL));
        String teamRoomId = prop.getProperty(ArgUtil.TEAM_ROOM_ID);
        return "v2/timetracking/timesheets/assignment?date="+firstDay+"&fullTeam=true&period=WEEK&teamId="+teamRoomId;
    }

    private void clearSheet(){
        final String[] args = {"--startDate", START_DATE, "--endDate", END_DATE, "--mode", DELETE_MODE, "--credential",
                CREDENTIALS_PROPERTIES};
        WSProRunner.main(args);
    }

    private void generateSheet(){
        final String[] args = {"--startDate", START_DATE, "--endDate", END_DATE, "--credential",
                CREDENTIALS_PROPERTIES};
        WSProRunner.main(args);
    }
}
