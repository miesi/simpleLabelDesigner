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
package de.mieslinger.simplelabeldesigner.servlets;

import Models.TemplateListElement;
import Models.TemplateUpload;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mieslinger.simplelabeldesigner.Main;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mieslingert
 *
 * GET templates -> List POST templates -> create new template
 *
 * operations on individual templates handled in template servlet
 */
public class ServletTemplates extends HttpServlet {

    private Logger logger = LoggerFactory.getLogger(ServletTemplates.class);

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long startTs = System.currentTimeMillis();
        PrintWriter out = response.getWriter();
        try {
            Path path = Paths.get(Main.getTemplatesFolder());
            DirectoryStream<Path> files = Files.newDirectoryStream(path, "[0-9]* - *");
            List<TemplateListElement> l = new LinkedList<>();
            for (Path f : files) {
                String[] elems = f.getFileName().toString().split(" - ");
                TemplateListElement t = new TemplateListElement(Integer.parseInt(elems[0]), elems[1]);
                l.add(t);
            }
            files.close();
            ObjectMapper mapper = new ObjectMapper();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(200);
            mapper.writeValue(out, l);
            long totalTime = System.currentTimeMillis() - startTs;
            out.println("Total processing time: " + totalTime + "ms");
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(500);
            long totalTime = System.currentTimeMillis() - startTs;
            out.println(e.toString());
            out.println("Total processing time: " + totalTime + "ms");
            e.printStackTrace();
        } finally {
            out.close();
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     *
     * Expected JSON { "name":"string", "content":"string" }
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long startTs = System.currentTimeMillis();
        PrintWriter out = response.getWriter();
        try {
            // read json from request
            ObjectMapper mapper = new ObjectMapper();
            TemplateUpload t = mapper.readValue(request.getReader(), TemplateUpload.class);

            // find a free id
            Path path = Paths.get(Main.getTemplatesFolder());
            DirectoryStream<Path> files = Files.newDirectoryStream(path, "[0-9]* - *");
            List<Integer> l = new LinkedList<>();
            for (Path f : files) {
                String[] elems = f.getFileName().toString().split(" - ");
                l.add(Integer.parseInt(elems[0]));
            }
            files.close();
            Collections.sort(l);
            Integer newid = 1;
            if (!l.isEmpty()) {
                newid = l.get(l.size() - 1) + 1;
            }

            // write file
            // TOOD: strip away dots, /, \ and other fancy stuff in name
            String newTemplate = String.format("%s/%d - %s", Main.getTemplatesFolder(), newid, t.name);
            BufferedWriter bw = Files.newBufferedWriter(Paths.get(newTemplate), StandardOpenOption.CREATE_NEW);
            bw.write(t.content);
            bw.close();
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(200);
            long totalTime = System.currentTimeMillis() - startTs;
            out.println("Success - Total processing time: " + totalTime + "ms");
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(500);
            long totalTime = System.currentTimeMillis() - startTs;
            out.println(e.toString());
            out.println("Total processing time: " + totalTime + "ms");
            logger.error("Request failed");
        } finally {
            out.close();
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "REST API for templates";
    }
}
