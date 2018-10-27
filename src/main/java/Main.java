import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import utils.FileUtils;
import utils.TextUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class Main {
    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();

        String urlString = "https://hangchinhhieu.vn/collections/laptop";
        test2(urlString);

        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    private static void test1(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        String src = FileUtils.getString(connection.getInputStream());

        src = TextUtils.refineHtml(src);

        FileUtils.exportFile(src, FileUtils.getFilePath("file-checker.xml"));
    }

    private static void test2(String urlString) throws IOException {
        Document doc = Jsoup.connect(urlString).get();

        Elements body = doc.select("body");
        String src = body.outerHtml();

        Document.OutputSettings settings = new Document.OutputSettings();
        settings.escapeMode(Entities.EscapeMode.xhtml);

        src = Jsoup.clean(src, "", Whitelist.relaxed(), settings);

        FileUtils.exportFile(src, FileUtils.getFilePath("file-jsoup.xml"));
    }
}

//        String src = "<h1 checked aa=aa a=\"1\"b='\"2\"&'c=3 kl>     <img a=1 a=/>kjkk  <p l p>YEAH</h1>";
//        String src = "<table><li></h3><li></h3>";
