package xmlchecker2;

import fsm.impl.Cache;

import static xmlchecker2.SyntaxState.*;

public class XmlSyntaxChecker {

    public String check(String src) {
        src = src + " ";
        char[] reader = src.toCharArray();
        Cache cache = new Cache();

        XmlSyntaxReader syntaxReader = new XmlSyntaxReader(cache);

        for (char c : reader) {
            syntaxReader.listen(c);
        }

        if (CONTENT.equals(syntaxReader.getCurrentState())) {
            String content = cache.getContent().toString().trim().replace("&", "&amp;");
            cache.getWriter().append(content);
        }

        while (!cache.getStack().isEmpty()) {
            cache.getWriter().append(LT)
                    .append(SLASH)
                    .append(cache.getStack().pop())
                    .append(GT);
        }

        return cache.getWriter().toString();
    }
}
