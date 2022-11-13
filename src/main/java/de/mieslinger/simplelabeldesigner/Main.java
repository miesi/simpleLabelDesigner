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
package de.mieslinger.simplelabeldesigner;

import com.sampullara.cli.Argument;
import de.mieslinger.simplelabeldesigner.servlets.ServletRender;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.mieslinger.simplelabeldesigner.servlets.ServletRoot;
import de.mieslinger.simplelabeldesigner.servlets.ServletTemplate;
import de.mieslinger.simplelabeldesigner.servlets.ServletTemplates;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author mieslingert
 */
public class Main {

    @Argument(alias = "tf", description = "templates folder (default /srv/http/templatedesigner)")
    private static String strTemplatesFolder = "/srv/http/templatedesigner";

    @Argument(alias = "hp", description = "http port (default 8989)")
    private static String strHttpPort = "8989";
    private static int numHttpPort;

    private static Logger logger = LoggerFactory.getLogger(Main.class);
    private static Server jetty;

    public static void main(String[] args) {

        validateOutputDir(strTemplatesFolder);
        numHttpPort = Integer.parseInt(strHttpPort);
        startJetty();
    }

    private static void startJetty() {
        try {
            jetty = new Server(numHttpPort);

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");

            jetty.setHandler(context);

            context.addServlet(ServletRoot.class, "/");
            //context.addServlet(ServletStatus.class, "/status");
            //context.addServlet(ServletStatistics.class, "/statistics");
            context.addServlet(ServletTemplates.class, "/templates"); // POST, GET List go here
            context.addServlet(ServletTemplate.class, "/templates/*"); // GET, DELETE of individual templates go here
            context.addServlet(ServletRender.class, "/render");
            jetty.start();

        } catch (Exception e) {
            logger.warn("Jetty not started: {}", e.toString());
        }
    }

    public static boolean validateOutputDir(String strPath) {
        Path path = Paths.get(strPath);
        if (!Files.exists(path)) {
            logger.error("Output directory '" + strPath + "' does not exist.");
            System.exit(1);
        }
        if (!Files.isDirectory(path)) {
            logger.error("Output directory '" + strPath + "' is not a directory");
            System.exit(1);
        }
        if (!Files.isWritable(path)) {
            logger.error("Output directory '" + strPath + "' is not writeable");
            System.exit(1);
        }
        if (!Files.isExecutable(path)) {
            logger.error("Output directory '" + strPath + "' is not executable");
            System.exit(1);
        }
        logger.info("Output directory '" + strPath + "' is usuable");
        return true;
    }

    public static String getTemplatesFolder() {
        return strTemplatesFolder;
    }
}
