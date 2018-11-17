package test;

import org.junit.Assert;
import org.junit.Test;
import xmlchecker.XmlSyntaxChecker;

public class CheckerTest {

    private XmlSyntaxChecker checker = new XmlSyntaxChecker();

    @Test
    public void checkAloneAttribute() {
        String src = "<h1 checked>YEAH</h1>";
        String result = checker.check(src);

        System.out.println(src);
        System.out.println(result);

        Assert.assertEquals(result, "<h1 checked=\"true\">YEAH</h1>");

//        src = "<h1 checked aa=aa a=\"1\"b='\"2\"&'c=3 kl>     <img a=1 a=/>kjkk  <p l p>YEAH</h1>";
    }

    @Test
    public void checkMissingQuot() {
        String src = "<h1 aa=  aa><img a=a />YEAH</h1>";
        String result = checker.check(src);

        System.out.println(src);
        System.out.println(result);

        Assert.assertEquals(result, "<h1 aa=\"aa\"><img a=\"a\"/>YEAH</h1>");
    }

    @Test
    public void checkStickyAttributes() {
        String src = "<h1 a=\"1\"b='2'c=3>YEAH</h1>";
        String result = checker.check(src);

        System.out.println(src);
        System.out.println(result);

        Assert.assertEquals(result, "<h1 a=\"1\" b=\"2\" c=\"3\">YEAH</h1>");
    }

    @Test
    public void checkSpecificEntity() {
        String src = "<h1 b='\"2\"'>YEAH</h1>";
        String result = checker.check(src);

        System.out.println(src);
        System.out.println(result);

        Assert.assertEquals(result, "<h1 b=\"&quot;2&quot;\">YEAH</h1>");
    }

    @Test
    public void checkOmmitInvalidAttributeName() {
        String src = "<h1 1 a>YEAH</h1>";
        String result = checker.check(src);

        System.out.println(src);
        System.out.println(result);

        Assert.assertEquals(result, "<h1 a=\"true\">YEAH</h1>");
    }

    @Test
    public void checkEmptyElementCorrect() {
        String src = "<h1 >  s <img src=\"\"><br><hr/></h1>";
        String result = checker.check(src);

        System.out.println(src);
        System.out.println(result);

        Assert.assertEquals(result, "<h1>s<img src=\"\"/><br/><hr/></h1>");
    }
}
