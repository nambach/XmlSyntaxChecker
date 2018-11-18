package test;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import utils.FileUtils;
import utils.TextUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class TextUtilsTest {

    @Test
    public void testWellformed() throws IOException {
        String urlString = "https://pibook.vn/moi-phat-hanh";
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        connection.setReadTimeout(8 * 1000);
        connection.setConnectTimeout(8 * 1000);

        String textContent = FileUtils.getString(connection.getInputStream());

        textContent = TextUtils.refineHtml(textContent);

        Assert.assertTrue(checkWellformedXml(textContent));
    }

    private static boolean checkWellformedXml(String src) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return false;
        }

        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                System.out.println(exception.getMessage());
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                System.out.println(exception.getMessage());
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                System.out.println(exception.getMessage());
            }
        });

        try {
            Document document = builder.parse(new ByteArrayInputStream(src.getBytes(StandardCharsets.UTF_8)));
            return true;
        } catch (SAXException | IOException e) {
            return false;
        }
    }
}
