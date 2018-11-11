package by.bsu.regex;

final class Matcher {

    private BsuRegex pattern;
    private String text;
    int to;

    Matcher(BsuRegex pattern, String text) {
        this.pattern = pattern;
        this.text = text;
        to = text.length();
    }

    boolean matches() {
        return pattern.matchRoot.match(this, 0, text);
    }
}
