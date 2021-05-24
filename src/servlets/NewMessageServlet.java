package servlets;

import entity.ChatMessage;
import entity.ChatUser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Calendar;

public class NewMessageServlet extends ChatServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf8");
        HttpSession session = request.getSession();
        String message = (String) request.getParameter("message");

        if (message != null && !"".equals(message)) {
            ChatUser author = activeUsers.get((String) session.getAttribute("name"));
            author.setLastInteractionTime(Calendar.getInstance().getTimeInMillis());
            synchronized (messages) {
                messages.addFirst(new ChatMessage(message, author, Calendar.getInstance().getTimeInMillis()));
            }
        }
        response.sendRedirect("/chat/composeMessage.jsp");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
