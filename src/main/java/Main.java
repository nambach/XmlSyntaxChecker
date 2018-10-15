import parser.XmlSyntaxChecker;

public class Main {
    public static void main(String[] args) {
        XmlSyntaxChecker checker = new XmlSyntaxChecker();
        String src = "<h1 checked aa=aa a=\"1\"b='\"2\"'c=3 kl>     <img a=1 a=/>kjkk  <p l p>";
        System.out.println(checker.check(src));
    }
}
