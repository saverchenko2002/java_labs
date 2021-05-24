package servlets;

import entity.ChatUser;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

public class LogoutServlet extends ChatServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String name = (String) session.getAttribute("name");

        if (name != null) {
            ChatUser user = activeUsers.get(name);

            if (user.getSessionId().equals((String) session.getId())) {
                synchronized (activeUsers) {
                    activeUsers.remove(name);
                }

                session.setAttribute("name", null);
                response.addCookie(new Cookie("sessionId", null));
                response.sendRedirect(response.encodeRedirectURL("/chat/"));
            } else {
                response.sendRedirect(response.encodeRedirectURL("/chat/view.jsp"));
            }
        } else {
            response.sendRedirect(response.encodeRedirectURL("/chat/login"));
        }
    }

}


