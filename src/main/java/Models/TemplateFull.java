/*
 * The MIT License
 *
 * Copyright 2022 mieslingert.
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
package Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import utils.RegexScanner;

/**
 *
 * @author mieslingert
 */
public class TemplateFull {

    @JsonProperty("id")
    public Integer id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("content")
    public String content;
    @JsonProperty("parameters")
    public Map<String, String> parameters;

    public void initFromPath(Path f) throws IOException {
        this.parameters = new HashMap<>();

        String[] elems = f.getFileName().toString().split(" - ");
        this.id = Integer.parseInt(elems[0]);
        this.name = elems[1];

        BufferedReader br = Files.newBufferedReader(f);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        this.content = sb.toString();

        String regex = "\\$\\{[a-zA-Z0-9:]+\\}";
        RegexScanner regexScanner = new RegexScanner(sb.toString(), regex);
        while (regexScanner.hasNext()) {
            String parameter = regexScanner.next();
            // trim ${ and }
            parameter = parameter.replaceAll("\\$\\{", "");
            parameter = parameter.replaceAll("\\}", "");
            // split at first :
            String[] kv = parameter.split(":");
            if (kv.length == 1) {
                this.parameters.put(kv[0], "");
            } else {
                this.parameters.put(kv[0], kv[1]);
            }
        }
    }
}
