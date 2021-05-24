package servlets;

import entity.ChatMessage;
import entity.ChatUser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

@WebServlet(name = "ChatServlet")
public class ChatServlet extends HttpServlet {
    protected HashMap<String, ChatUser> activeUsers;
    protected LinkedList<ChatMessage> messages;

    @Override
    public void init() throws ServletException {
        super.init();
        activeUsers = (HashMap<String, ChatUser>) getServletContext().getAttribute("activeUsers");
        messages = (LinkedList<ChatMessage>) getServletContext().getAttribute("messages");

        if (activeUsers == null) {
            activeUsers = new HashMap<String, ChatUser>();
            getServletContext().setAttribute("activeUsers", activeUsers);
        }

        if (messages == null) {
            messages = new LinkedList<ChatMessage>();
            getServletContext().setAttribute("messages", messages);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
