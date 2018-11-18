import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import utils.TextUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class TestTextUtils {

    public static void main(String[] args) throws IOException {
        String[] urls = { "https://pibook.vn/moi-phat-hanh", "https://www.vinabook.com/sach-moi-phat-hanh", "https://meta.vn/may-khoan-c681" };

        for (String url : urls) {
            testWellformed(url);
        }
    }

    private static void testWellformed(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        connection.setReadTimeout(8 * 1000);
        connection.setConnectTimeout(8 * 1000);

        String textContent = getString(connection.getInputStream());

        textContent = TextUtils.refineHtml(textContent);

        if (checkWellformedXml(textContent)) {
            System.out.println(urlString + " đã well-formed.");
        }
    }

    private static String getString(InputStream stream) {

        StringBuilder stringBuilder = new StringBuilder();
        String line;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException ignored) {
        }

        return stringBuilder.toString();
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
            builder.parse(new ByteArrayInputStream(src.getBytes(StandardCharsets.UTF_8)));
            return true;
        } catch (SAXException | IOException e) {
            return false;
        }
    }
}
