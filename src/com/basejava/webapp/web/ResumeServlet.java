package com.basejava.webapp.web;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import com.basejava.webapp.model.Resume;
import com.basejava.webapp.storage.SqlStorage;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

public class ResumeServlet extends HttpServlet {
    private SqlStorage storage;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try (InputStream is = config.getServletContext()
                .getResourceAsStream("/resumes.properties")) {
            if (is == null) {
                throw new ServletException("Cannot find /config/resumes.properties in web app");
            }

            Properties props = new Properties();
            props.load(is);

            String dbUrl = props.getProperty("db.url");
            String dbUser = props.getProperty("db.user");
            String dbPassword = props.getProperty("db.password");

            if (dbUrl == null) {
                throw new ServletException("db.url is not set in resumes.properties");
            }

            storage = new SqlStorage(dbUrl, dbUser, dbPassword);
        } catch (IOException e) {
            throw new ServletException("Failed to load DB config from resumes.properties", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        // Пока пусто
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        List<Resume> resumes = storage.getAllSorted();
        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<head><title>Resumes</title></head>");
        out.println("<body>");
        out.println("<h1>Resumes</h1>");
        out.println("<table border='1'>");
        out.println("<tr><th>UUID</th><th>Full name</th></tr>");

        for (Resume r : resumes) {
            out.println("<tr>");
            out.println("<td>" + r.getUuid() + "</td>");
            out.println("<td>" + r.getFullName() + "</td>");
            out.println("</tr>");
        }

        out.println("</table>");
        out.println("</body>");
        out.println("</html>");
    }
}