package com.basejava.webapp.web;

import com.basejava.webapp.storage.SqlStorage;
import com.basejava.webapp.util.Config;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("resumes", storage.getAllSorted());
        request.getRequestDispatcher("/WEB-INF/jsp/list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Пока пусто
    }
}