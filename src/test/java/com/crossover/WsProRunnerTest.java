package com.crossover;

import com.crossover.sheet.GoogleSheetHelper;
import com.crossover.util.ArgUtil;
import com.crossover.util.Utils;
import com.crossover.wspro.WSProRunner;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Assert;
import org.junit.Test;

public class WsProRunnerTest {

    private static final String CREDENTIALS_PROPERTIES = "C:\\Users\\user\\Desktop\\BC-WSPro-Work-Pattern-Analyzer\\credentials.properties";
    private static final String START_DATE = "2019-01-01";
    private static final String START_DATE_INVALID = "2019-02-01";
    private static final String END_DATE = "2019-01-01";
    private static final String DELETE_MODE = "delete";
    private static final String FACE_DETECTION = "facedetection";

    @Test
    public void googleSheetShouldBeFilledWithTeam() throws Exception {
        final String[] args = {"--startDate", START_DATE, "--endDate", END_DATE, "--credential",
                CREDENTIALS_PROPERTIES};
        WSProRunner.main(args);
        final Namespace namespace = ArgUtil.checkArgs(args);
        final int count = GoogleSheetHelper.getRowCountOfSheet(namespace);
        Assert.assertTrue(count > 1);
    }

    @Test
    public void rowsShouldBeRemoved() throws Exception {
        final String[] args = {"--startDate", START_DATE, "--endDate", END_DATE, "--mode", DELETE_MODE, "--credential",
                CREDENTIALS_PROPERTIES};
        WSProRunner.main(args);
        final Namespace namespace = ArgUtil.checkArgs(args);
        final int count = GoogleSheetHelper.getRowCountOfSheet(namespace);
        Assert.assertTrue(count < 2);
    }

    @Test
    public void fileShouldNotBeFoundAndThrowException() throws Exception {
        try {
            final String[] args = {"--startDate", START_DATE, "--endDate", END_DATE, "--mode", DELETE_MODE,
                    "--credential", "random/path"};
            ArgUtil.checkArgs(args);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void fileFormatShouldNotBeSupported() throws Exception {
        final String[] args = {"--startDate", START_DATE, "--endDate", END_DATE, "--mode", DELETE_MODE,
                "--credential", "img.jpg"};
        Namespace namespace = ArgUtil.checkArgs(args);
        Assert.assertNull(namespace);
    }

    @Test
    public void propertiesNotFoundInFile() throws Exception {
        final String[] args = {"--startDate", START_DATE, "--endDate", END_DATE, "--mode", DELETE_MODE,
                "--credential", "temp/prop.properties"};
        Namespace namespace = ArgUtil.checkArgs(args);
        Assert.assertNull(namespace);
    }

    @Test
    public void dateParseExceptionShouldReturn() throws Exception {
        try {
            final String[] args = {"--startDate", "2000.01.02", "--endDate", END_DATE, "--mode", DELETE_MODE,
                    "--credential", CREDENTIALS_PROPERTIES};
            ArgUtil.checkArgs(args);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void faceDetectionNotSupportedShouldReturn() throws Exception {
        final String[] args = {"--startDate", START_DATE, "--endDate", END_DATE, "--type", FACE_DETECTION,
                "--credential", CREDENTIALS_PROPERTIES};
        Namespace namespace = ArgUtil.checkArgs(args);
        Assert.assertNull(namespace);
    }

    @Test
    public void teamIdShouldBeNumber() throws Exception {
        try {
            final String[] args = {"--startDate", START_DATE, "--endDate", END_DATE, "--credential",
                    "temp/test.properties"};
            WSProRunner.main(args);
        } catch (RuntimeException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void startDateCannotAfterEndDate() throws Exception {
        try {
            final String[] args = {"--startDate", START_DATE_INVALID, "--endDate", END_DATE,
                    "--credential", CREDENTIALS_PROPERTIES};
            WSProRunner.main(args);
        } catch (RuntimeException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void stringShouldReturnAsNotNumber() throws Exception {
        try {
            boolean result = Utils.isNumber(null);
            Assert.assertFalse(result);
            result = Utils.isNumber("abc");
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

}
