package xmlchecker;

public class SyntaxState {

    public static final String CONTENT = "content";
    public static final String OPEN_BRACKET = "openBracket";
    public static final String OPEN_TAG_NAME = "openTagName";
    public static final String TAG_INNER = "tagInner";
    public static final String ATTR_NAME = "attributeName";
    public static final String EQUAL_WAIT = "equalWait";
    public static final String EQUAL = "equal";
    public static final String VALUE_WAIT = "valueWait";
    public static final String ATTR_VALUE_NQ = "nonQuotedAttributeValue";
    public static final String ATTR_VALUE_Q = "quotedAttributeValue";
    public static final String EMPTY_SLASH = "emptyTagSlash";
    public static final String CLOSE_BRACKET = "closeBracket";
    public static final String CLOSE_TAG_SLASH = "closeTagSlash";
    public static final String CLOSE_TAG_NAME = "closeTagName";
    public static final String WAIT_END_TAG_CLOSE = "waitEndTagClose";
    public static final String PROCESS_INSTRUCTION = "processInstruction";


    public static final char LT = '<';
    public static final char SLASH = '/';
    public static final char GT = '>';
    public static final char EQ = '=';
    public static final char D_QUOT = '"';
    public static final char S_QUOT = '\'';
    public static final char QUESTION_MARK = '?';
    public static final char NEW_LINE = '\n';

    public static final char UNDERSCORE = '_';
    public static final char COLON = ':';
    public static final char HYPHEN = '-';
    public static final char PERIOD = '.';

    private static boolean isStartChar(char c) {
        return Character.isLetter(c) || UNDERSCORE == c || COLON == c;
    }

    private static boolean isNamedChar(char c) {
        return Character.isLetterOrDigit(c) || UNDERSCORE == c 
                || HYPHEN == c || PERIOD == c || COLON == c;
    }

    public static boolean isStartTagChars(char c) {
        return isStartChar(c);
    }

    public static boolean isStartAttrChars(char c) {
        return isStartChar(c);
    }

    public static boolean isTagChars(char c) {
        return isNamedChar(c);
    }

    public static boolean isAttrChars(char c) {
        return isNamedChar(c);
    }

    public static boolean isSpace(char c) {
        return Character.isSpaceChar(c) || c == NEW_LINE;
    }
}
