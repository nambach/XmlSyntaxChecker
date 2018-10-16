import parser.EntitySyntaxChecker;
import parser.XmlSyntaxChecker;

public class Main {
    public static void main(String[] args) {
        XmlSyntaxChecker checker = new XmlSyntaxChecker();
        String src = "<h1 checked aa=aa a=\"1\"b='\"2\"&'c=3 kl>     <img a=1 a=/>kjkk  <p l p>YEAH</h1>";
//        String src = "<table><li></h3><li></h3>";
        src = checker.check(src);
        System.out.println(src);

        EntitySyntaxChecker entitySyntaxChecker = new EntitySyntaxChecker();
        src = entitySyntaxChecker.check(src);

        System.out.println(src);
    }
}
