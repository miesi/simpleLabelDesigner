/*
 * copied from https://github.com/BelmoMusta/regex-scanner
 * original license: unknown
 * 
 * The MIT License
 *
 * Copyright 2020 Mustapha Belmokhtar
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package utils;

import java.io.Closeable;
import java.util.Iterator;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scanner for text using regex to match full tokens.
 *
 * @author Mustapha Belmokhtar
 * @version 0.1
 */
public class RegexScanner implements Iterator<String>, Closeable {

    private Pattern pattern;
    private String input;
    private boolean closed;
    private boolean endReached;

    /**
     * Consumes string tokens that matches the given regex
     *
     * @param input the input string.
     * @param regex the regex to match against.
     */
    public RegexScanner(String input, String regex) {
        this.input = input;
        pattern = Pattern.compile(regex);
    }

    /**
     * Constructor Consumes each non blank token of the input string.
     *
     * @param input the input {@link String}
     */
    public RegexScanner(String input) {
        this(input, "[^\\s]+");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        if (closed) {
            throw new IllegalStateException("Scanner is closed");
        }

        return !endReached && !input.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String next() {
        if (closed) {
            throw new IllegalStateException("Scanner is closed");
        } else {
            String fullMatch = null;
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                int end = matcher.end();
                int start = matcher.start();

                fullMatch = input.substring(start, end);
                input = input.substring(end);
                verifyEndReached();

            } else {
                do {
                    if (!input.isEmpty()) {
                        input = input.substring(1);
                    }
                    matcher = pattern.matcher(input);
                } while (!endReached && !matcher.find() && !input.isEmpty());
            }
            return fullMatch;
        }
    }

    public <R> R next(Function<String, R> mapper) {
        final String next = next();
        return mapper.apply(next);
    }

    /**
     * after each next invocation, a verification is done to avoid null and
     * empty strings when the whole rest of the string does not contain any
     * match of the given regex
     */
    private void verifyEndReached() {
        String temp = input;
        Matcher matcher;
        do {
            if (!temp.isEmpty()) {
                temp = temp.substring(1);
            }
            matcher = pattern.matcher(temp);
        } while (!matcher.find() && !temp.isEmpty());

        if (temp.isEmpty()) {
            endReached = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        closed = true;
        pattern = null;
        input = null;
    }
}
