package servlets;

import entity.ChatUser;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

public class LoginServlet extends ChatServlet {
    private int sessionTimeout = 600;

    @Override
    public void init() throws ServletException {
        super.init();
        String value = getServletConfig().getInitParameter("SESSION_TIMEOUT");
        if (value != null) {
            sessionTimeout = Integer.parseInt(value);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        String name = (String) session.getAttribute("name");
        String errorMessage = (String) session.getAttribute("error");

        String previousSessionId = null;

        if (name == null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("sessionId")) {
                    previousSessionId = cookie.getValue();
                    break;
                }
            }
            if (previousSessionId != null) {
                for (ChatUser user : activeUsers.values()) {
                    if (user.getSessionId().equals(previousSessionId)) {
                        name = user.getName();
                        user.setSessionId(request.getSession().getId());
                    }
                }
            }
        }

        if (name != null && !"".equals(name)) {
            errorMessage = processLogonAttempt(name, request, response);
        }

        response.setCharacterEncoding("utf8");
        PrintWriter pw = response.getWriter();
        pw.println("<html><head><title>Мега-чат!</title>" +
                "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/></head>");

        if (errorMessage != null) {
            pw.println("<p><font color='red'>" + errorMessage + "</font></p>");
        }

        pw.println("<form action='/chat/' method='post'>" +
                "Введите имя: <input type='text' name='name' value=''><input type='submit' value='Войти в чат'>");
        pw.println("</form></body></html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        request.setCharacterEncoding("UTF-8");

        String name = (String) request.getParameter("name");
        String errorMessage = null;

        if (name == null || "".equals(name)) {
            errorMessage = "Имя пользователя не должно быть пустым!";
        } else {
            errorMessage = processLogonAttempt(name, request, response);
        }
        if (errorMessage != null) {
            session.setAttribute("name", null);
            session.setAttribute("error", errorMessage);
            response.sendRedirect(response.encodeRedirectURL("/login"));
        }
    }

    String processLogonAttempt(String name, HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String sessionId = session.getId();
        ChatUser user = activeUsers.get(name);
        if (user == null) {
            user = new ChatUser(name, sessionId, Calendar.getInstance().getTimeInMillis());
            synchronized (activeUsers) {
                activeUsers.put(user.getName(), user);
            }
        }
        if (user.getSessionId().equals(sessionId) || (user.getLastInteractionTime() < Calendar.getInstance().getTimeInMillis() - sessionTimeout * 1000)) {
            session.setAttribute("name", name);
            user.setLastInteractionTime(Calendar.getInstance().getTimeInMillis());
            Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
            sessionIdCookie.setMaxAge(60 * 60 * 24);
            response.addCookie(sessionIdCookie);
            response.sendRedirect(response.encodeRedirectURL("/chat/view.jsp"));
            return null;
        } else {
            return "Извините, но имя <strong>" + name + "</strong> уже кем-то занято. Пожалуйста выберите другое имя!";
        }
    }
}

