package com.delin.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryTokenizer implements Tokenizer {

    @Override
    public String[] tokenize(String expression) {

        if (expression == null) {
            return new String[0];
        }

        List<String> tokens = new ArrayList<>();
        String[] lines = expression.split("[\\r\\n]+");
        // tokenize line by line
        loopLine:
        for (String line : lines) {
            int cursor = 0;
            int sz = line.length();
            for (int i = 0; i < sz; i++) {
                char c = line.charAt(i);
                switch (c) {
                    case '\"':
                    case '\'': // Text will be quote by " or '.
                        tokens.addAll(splitWithoutQuotationAndComments(line.substring(cursor, i)));
                        String text = detectQuotation(line, i);
                        tokens.add(text);
                        i += text.length() - 1;
                        cursor = i + 1;
                        break;
                    case '#': // Content behind # is the comment, ignore comments
                        tokens.addAll(splitWithoutQuotationAndComments(line.substring(cursor, i)));
                        continue loopLine;
                }
            }
            tokens.addAll(splitWithoutQuotationAndComments(line.substring(cursor, sz)));
        }
        return tokens.toArray(new String[0]);
    }

    private List<String> splitWithoutQuotationAndComments(String singleLineExpression) {
        // name: [a-zA-Z_][a-zA-Z0-9_]*
        // number: [0-9]+(\.[0-9]*)?|\.[0-9]+
        // word: [a-zA-Z0-9_.]+
        Pattern nameOrNumber = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*|([0-9]+(\\.[0-9]*)?|\\.[0-9]+))");
        Pattern word = Pattern.compile("[a-zA-Z0-9_.]+");
        Matcher matcher = word.matcher(singleLineExpression);
        List<String> tokens = new ArrayList<>();
        int cursor = 0;

        // Find a word
        while (matcher.find()) {
            int start = matcher.start();
            if (start > cursor) {
                // The content between two words is special characters
                tokens.addAll(splitSpecial(singleLineExpression.substring(cursor, start)));
            }
            String token = matcher.group();

            // Make sure the word is a name or number
            if (!nameOrNumber.matcher(token).matches()) {
                throw new InvalidTokenException("Unexpected token: " + token);
            }
            tokens.add(token);
            cursor = matcher.end();
        }
        if (singleLineExpression.length() > cursor) {
            tokens.addAll(splitSpecial(singleLineExpression.substring(cursor)));
        }
        return tokens;
    }

    private List<String> splitSpecial(String specialWords) {
        List<String> special = new ArrayList<>();
        for (char c : specialWords.toCharArray()) {
            switch (c) {
                // Only support below special characters in query
                case '=':
                case '>':
                case '<':
                case '(':
                case ')':
                case ';':
                case ',':
                case '-':
                case '+':
                    special.add(String.valueOf(c));
                    break;
                // Ignore blank characters
                case '\t':
                case ' ':
                case '\r':
                case '\n':
                    break;
                // Don't support others
                default:
                    throw new InvalidTokenException("Unexpected token: " + c);
            }
        }
        return special;
    }

    /**
     * Input:  "123"456, 0
     * Output: "123"
     * Input:  a"123"456, 1
     * Output: "123"
     * Input:  '123"\''456, 0
     * Output: '123"\''
     * Input:  "123\\"456, 0
     * Output: "123\\"
     * Input:  '123, 0
     * Output: InvalidTokenException
     * Input:  '123\\\'456
     * Output: InvalidTokenException
     *
     * @param str
     * @param start
     * @return return the content in quotation.
     */
    private String detectQuotation(String str, int start) {
        char quotation = str.charAt(start);
        int sz = str.length();
        for (int i = start + 1; i < sz; i++) {
            char c = str.charAt(i);
            if (c == '\\') {
                // If we find \, then it together with the next char is a escape character. Ignore it and the next char.
                i++;
            } else if (c == quotation) {
                return str.substring(start, i + 1);
            }
        }
        throw new InvalidTokenException("Text does NOT end with quotation: " + str.substring(start));
    }
}
