package com.delin.tokenizer;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(DataProviderRunner.class)
public class QueryTokenizerTest {

    private static String BASE_FOLDER = "src/test/resources/query/";

    @DataProvider
    public static Object[][] validTokenDataProvider() {
        // Case Name, Content File Name, Result File Name
        return new Object[][]{
                {"TC1", "tokenizer-test01.sql", "tokenizer-test01-tokens.txt"},
                {"TC2", "tokenizer-test02.sql", "tokenizer-test02-tokens.txt"},
        };
    }

    @Test
    @UseDataProvider("validTokenDataProvider")
    public void testValidToken(String caseName, String contentFileName, String resultFileName) throws Exception {
        testValidToken(contentFileName, resultFileName);
    }

    @Test
    public void testEmpty() {
        String[] tokens = new QueryTokenizer().tokenize(null);
        Assert.assertEquals(0, tokens.length);
        new QueryTokenizer().tokenize("\t\r\n   ");
        Assert.assertEquals(0, tokens.length);
    }

    @DataProvider
    public static Object[][] invalidTokenDataProvider() {
        // Case Name, Content File Name, Result File Name
        return new Object[][]{
                {"TC1", "invalid-tokenizer-test01.sql", "invalid-tokenizer-test01-exception.txt"},
                {"TC2", "invalid-tokenizer-test02.sql", "invalid-tokenizer-test02-exception.txt"},
                {"TC3", "invalid-tokenizer-test03.sql", "invalid-tokenizer-test03-exception.txt"},
                {"TC4", "invalid-tokenizer-test04.sql", "invalid-tokenizer-test04-exception.txt"},
        };
    }

    @Test
    @UseDataProvider("invalidTokenDataProvider")
    public void testInvalidToken(String caseName, String contentFileName, String resultFileName) throws Exception {
        testInvalidToken(contentFileName, resultFileName);
    }

    private void testValidToken(String filename, String resultFileame) throws Exception {
        String originContent = FileUtils.readFileToString(new File(BASE_FOLDER + filename), "utf-8");
        String[] expectedResult = FileUtils.readFileToString(new File(BASE_FOLDER + resultFileame), "utf-8").split("[\\r\\n]+");
        String[] actualResult = new QueryTokenizer().tokenize(originContent);
        Assert.assertArrayEquals(expectedResult, actualResult);
    }

    private void testInvalidToken(String filename, String exceptionFilename) throws Exception {
        String originContent = FileUtils.readFileToString(new File(BASE_FOLDER + filename), "utf-8");
        String expectedException = FileUtils.readFileToString(new File(BASE_FOLDER + exceptionFilename), "utf-8");
        try {
            String[] s = new QueryTokenizer().tokenize(originContent);
            for (String c : s) {
                System.out.println(c);
            }
            Assert.assertTrue("Expect exception but not find", false);
        } catch (Exception e) {
            Assert.assertEquals(expectedException, e.getMessage());
        }
    }
}
