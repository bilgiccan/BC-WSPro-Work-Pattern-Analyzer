package com.crossover.wspro.pack

import com.crossover.util.ArgUtil
import com.crossover.util.Utils
import groovy.util.logging.Log
import net.sourceforge.argparse4j.inf.Namespace

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

@Component
@Log
class AnalyzeManager {

    def bulkGemba(Namespace namespace) {
        def startDate = namespace.getString(ArgUtil.START_DATE)
        def endDate = namespace.getString(ArgUtil.END_DATE)

        validateInput(namespace)

        def dataSet = []
        getAllUsers(namespace).collect { user ->
            new Date().parse(Utils.DATE_FORMAT, startDate).upto(new Date().parse(Utils.DATE_FORMAT, endDate)) { Date today ->
                dataSet.addAll(getWorkDiaries(user, today, namespace).collect({ singleEntry ->
                    [
                            "IcName"        : user.candidate.get("firstName") + " " + user.candidate.get("lastName"),
                            "Date"          : singleEntry.date,
                            "ActivityLevel" : singleEntry.activityLevel,
                            "MouseEvents"   : singleEntry.mouseEvents,
                            "KeyboardEvents": singleEntry.keyboardEvents,
                            "IntensityScore": singleEntry.intensityScore,
                            "WindowTitle"   : singleEntry.windowTitle,
                            "Manager"       : user.manager.get("printableName"),
                            "MomentManager" : Utils.activeTimeManager(user.assignmentHistories, singleEntry.date, user.manager.get("printableName")),
                            "Day"           : Utils.getDayOfDate(singleEntry.date, user.candidate.location.timeZone.get("offset")),
                            "Local"         : Utils.getLocalFormattedHour(singleEntry.date, user.candidate.location.timeZone.get("offset"))
                    ]
                }))
            }
        }

        dataSet
    }

    private Object getWorkDiaries(user, Date today, Namespace namespace) {
        new XOService().getEntity(List, "timetracking/workdiaries?assignmentId=${user.get("id")}&date=${today.format(Utils.DATE_FORMAT)}", namespace)
    }

    def getMissingFaceShots(Namespace namespace) {
        def startDate = namespace.getString(ArgUtil.START_DATE)
        def endDate = namespace.getString(ArgUtil.END_DATE)

        validateInput()

        def dataSet = []

        getAllUsers(namespace).collect { user ->
            new Date().parse(Utils.DATE_FORMAT, startDate).upto(new Date().parse(Utils.DATE_FORMAT, endDate)) { Date today ->
                def candidateMap = user.candidate
                String userName = "${candidateMap.get("userId")}-${candidateMap.get("firstName")} ${candidateMap.get("lastName")}"
                dataSet.addAll(getWorkDiaries(user, today, namespace).collect({ singleEntry ->
                    String url = singleEntry.webcam ? singleEntry.webcam.url.toString() : ""
                    if (!StringUtils.isEmpty(url) && !FaceDetection.hasFace(LocalDateTime.now().toString().replace(':', '-'),
                            singleEntry.date.toString().replace(':', '-') + "-" + userName, url)) {
                        [
                                "date": singleEntry.date,
                                "user": userName,
                                "url" : url
                        ]
                    }
                }))
            }
        }
    }

    private def getAllUsers(Namespace namespace) {
        Properties prop = Utils.getProperties(namespace.get(ArgUtil.CREDENTIAL));
        def teamRoomId = prop.getProperty(ArgUtil.TEAM_ROOM_ID)
        def startDate = namespace.getString(ArgUtil.START_DATE)
        def endDate = namespace.getString(ArgUtil.END_DATE)
        def c = new XOService().getEntity(
                Map,
                "v2/teams/assignments?from=${startDate}&to=${endDate}&fullTeam=true&limit=1000&page=0&status=ACTIVE,MANAGER_SETUP&teamId=${teamRoomId}",
                namespace
        ).content
        println(c)
        return c
    }

    private void validateInput(Namespace namespace) {
        Properties prop = Utils.getProperties(namespace.get(ArgUtil.CREDENTIAL));
        def teamRoomId = prop.getProperty(ArgUtil.TEAM_ROOM_ID)
        def startDate = namespace.getString(ArgUtil.START_DATE)
        def endDate = namespace.getString(ArgUtil.END_DATE)
        if(!Utils.validateDates(startDate, endDate)){
            throw new RuntimeException("Dates cannot be null and start date cannot be before end date.")
        }
        if(!Utils.isNumber(teamRoomId)){
            throw new RuntimeException("Team id cannot be null and should be number")
        }
    }
}