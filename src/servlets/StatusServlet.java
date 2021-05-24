package servlets;

import entity.ChatUser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

public class StatusServlet extends ChatServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("utf8");
        PrintWriter pw = response.getWriter();

        HttpSession session = request.getSession();
        ChatUser user = activeUsers.get((String) session.getAttribute("name"));

        long sessionTimeout = 0;
        if (user != null && user.getSessionId() == session.getId()) {
            sessionTimeout = 5 * 1000 - (Calendar.getInstance().getTimeInMillis() - user.getLastInteractionTime());
        }

        pw.println("<html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'/><meta http-equiv='refresh' content='5'></head>");
        pw.println("<body>");
        pw.print("<p>Мега-чат: <font color='blue'>");
        if (sessionTimeout < 0) {
            response.sendRedirect(response.encodeRedirectURL("/chat/timeoutRedirect.jsp"));
        } else if (sessionTimeout < 60 * 1000) {
            pw.print("Время сессии закончится меньше чем через минуту");
        } else if (sessionTimeout < 3 * 60 * 1000) {
            pw.print("Время сессии закончится меньше чем через 3 минуты");
        } else if (sessionTimeout < 5 * 60 * 1000) {
            pw.print("Время сессии закончится меньше чем через 5 минут");
        }
        pw.println("</font></p></body></html>");
    }
}