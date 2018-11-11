package by.bsu.regex;

import java.util.function.Predicate;

public final class BsuRegex {

    private static final Node lastNode = new LastNode();

    private String pattern;

    private Node root;

    Node matchRoot;

    private char[] buffer = new char[32];

    private char[] temp;

    private int cursor;

    public static BsuRegex compile(String regex) {
        return new BsuRegex(regex);
    }

    public Matcher matcher(String input) {
        return new Matcher(this, input);
    }

    private BsuRegex(String p) {
        pattern = p;

        if (pattern.length() > 0) {
            compile();
        } else {
            throw new RuntimeException("pattern is empty");
        }
    }

    private void compile() {
        temp = new char[pattern.length() + 2];

        for (int x = 0; x < pattern.length(); x++) {
            temp[x] = pattern.charAt(x);
        }

        matchRoot = expr(lastNode);
        if (pattern.length() != cursor) {
            throw new RuntimeException("Incorrect pattern");
        }
    }

    private Node expr(Node end) {
        Node prev = null;
        for (;;) {
            Node node = sequence(end);
            prev = prev != null ? new Branch(prev, node) : node;
            if (temp[cursor] != '|') {
                return prev;
            }
            cursor++;
        }
    }

    private Node sequence(Node end) {
        Node head = null;
        Node tail = null;
        Node node;
        LOOP:
        for (;;) {
            char ch = temp[cursor];
            switch (ch) {
                case '(':
                    node = group();
                    if (head == null)
                        head = node;
                    else
                        tail.next = node;
                    tail = root;
                    continue;
                case '|':
                case ')':
                case 0:
                    break LOOP;
                case '*':
                    throw new RuntimeException("Found extra '*' character");
                default:
                    node = atom();
                    break;
            }

            node = closure(node);

            if (head == null) {
                head = tail = node;
            } else {
                tail.next = node;
                tail = node;
            }
        }
        tail.next = end;
        root = tail;
        return head;
    }

    private Node atom() {
        int first = 0;
        int prev = -1;
        char ch = temp[cursor];
        for (;;) {
            switch (ch) {
                case '*':
                    if (first > 1) {
                        cursor = prev;
                        first--;
                    }
                    break;
                case '(':
                case '|':
                case ')':
                    break;
                case 0:
                    if (cursor >= pattern.length()) {
                        break;
                    }
                default:
                    prev = cursor;
                    append(ch, first++);
                    ch = temp[++cursor];
                    continue;
            }
            break;
        }
        if (first == 1) {
            final char c = buffer[0];
            return new CharProperty(chr -> chr == c);
        } else {
            char[] buf = new char[first];
            System.arraycopy(buffer, 0, buf, 0, first);
            return new Slice(buf);
        }
    }

    private void append(char ch, int len) {
        if (len >= buffer.length) {
            char[] tmp = new char[2 * len];
            System.arraycopy(buffer, 0, tmp, 0, len);
            buffer = tmp;
        }
        buffer[len] = ch;
    }

    private Node group() {
        cursor++;

        Node tail = new Group();
        Node head = expr(tail);

        if (')' != temp[cursor++]) {
            throw new RuntimeException("Unclosed group");
        }

        Node node = closure(head);
        if (node == head) {
            root = tail;
            return node;
        }

        Loop loop = new Loop();

        loop.body = head;
        tail.next = loop;
        root = loop;
        return loop;
    }

    private Node closure(Node prev) {
        switch (temp[cursor]) {
            case '*':
                cursor++;
                return prev instanceof CharProperty ? new CharPropertyGreedy((CharProperty)prev) : null;
            default:
                return prev;
        }
    }

    static abstract class Node {
        Node next;
        abstract boolean match(Matcher matcher, int i, String str);
    }

    static class LastNode extends Node {
        boolean match(Matcher matcher, int i, String str) {
            return i == matcher.to;
        }
    }

    static class CharProperty extends Node {
        Predicate<Character> predicate;

        CharProperty(Predicate<Character> predicate) {
            this.predicate = predicate;
        }

        boolean match(Matcher matcher, int i, String str) {
            return i < matcher.to && predicate.test(str.charAt(i)) &&
                    next.match(matcher, i + 1, str);
        }
    }

    static class Slice extends Node {
        char[] buffer;
        Slice(char[] buf) {
            buffer = buf;
        }
        boolean match(Matcher matcher, int i, String str) {
            for (int j=0; j<buffer.length; j++) {
                if (((i+j) >= matcher.to) || (buffer[j] != str.charAt(i+j))) {
                    return false;
                }
            }
            return next.match(matcher, i+buffer.length, str);
        }
    }

    static final class CharPropertyGreedy extends Node {
        final Predicate<Character> predicate;

        CharPropertyGreedy(CharProperty cp) {
            this.predicate = cp.predicate;
        }

        boolean match(Matcher matcher, int i, String str) {
            int n = 0;
            int to = matcher.to;
            while (i < to && predicate.test(str.charAt(i))) {
                i++; n++;
            }
            while (n >= 0) {
                if (next.match(matcher, i, str))
                    return true;
                i--; n--;
            }
            return false;
        }
    }

    static final class Branch extends Node {
        Node[] atoms = new Node[2];
        Branch(Node first, Node second) {
            atoms[0] = first;
            atoms[1] = second;
        }

        boolean match(Matcher matcher, int i, String str) {
            return atoms[0].match(matcher, i, str) || atoms[1].match(matcher, i, str);
        }
    }

    static final class Group extends Node {
        boolean match(Matcher matcher, int i, String str) {
            return next.match(matcher, i, str);
        }
    }

    static class Loop extends Node {
        Node body;
        boolean match(Matcher matcher, int i, String str) {
            return body.match(matcher, i, str) || next.match(matcher, i, str);
        }
    }
}
