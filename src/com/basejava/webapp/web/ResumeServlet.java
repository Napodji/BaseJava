package com.basejava.webapp.web;

import com.basejava.webapp.model.Resume;
import com.basejava.webapp.storage.SqlStorage;
import com.basejava.webapp.util.Config;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ResumeServlet extends HttpServlet {
    private SqlStorage storage;

    @Override
    public void init() throws ServletException {
        Config config = Config.getInstance();
        storage = new SqlStorage(
                config.getDbUrl(),
                config.getDbUser(),
                config.getDbPassword()
        );
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

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        // Пока пусто
    }
}