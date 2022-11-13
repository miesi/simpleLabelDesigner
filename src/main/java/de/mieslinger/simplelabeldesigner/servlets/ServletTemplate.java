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

import Models.TemplateFull;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mieslinger.simplelabeldesigner.Main;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * GET templates/[id] -> individual template POST DELETE templates/[id] ->
 * delete individual template PATCH templates/[id] not planed for implementation
 * PUT templates/[id] not planed for implementation
 *
 * GET LIST and POST CREATE in templates
 */
public class ServletTemplate extends HttpServlet {

    private Logger logger = LoggerFactory.getLogger(ServletTemplate.class);

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
            // get id from url
            Integer templateId = Integer.parseInt(request.getPathInfo().substring(1));

            Path path = Paths.get(Main.getTemplatesFolder());
            DirectoryStream<Path> files = Files.newDirectoryStream(path, "" + templateId + " - *");
            int i = 0;
            TemplateFull t = null;
            for (Path f : files) {
                i++;
                t = new TemplateFull();
                t.initFromPath(f);
            }
            files.close();
            if (i == 0) {
                throw new Exception("No Template with id: " + templateId);
            }
            if (i > 1) {
                throw new Exception("More than one Template for id: " + templateId);
            }

            ObjectMapper mapper = new ObjectMapper();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(200);
            mapper.writeValue(out, t);
            long totalTime = System.currentTimeMillis() - startTs;
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
     * Handles the HTTP <code>DELETE</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        try {
            long startTs = System.currentTimeMillis();

            long totalTime = System.currentTimeMillis() - startTs;
        } catch (Exception e) {
            out.println(e.toString());
            e.printStackTrace();
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
        return "REST API for individual templates";
    }
}
