import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "FirstServlet")
public class FirstServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer count = (Integer) session.getAttribute("count");
        if (count == null) {
            session.setAttribute("count", 2);
            count = 1;
        }
        else
            session.setAttribute("count", count + 1);

//        String name = request.getParameter("name");
//        String surname = request.getParameter("surname");
        PrintWriter pw = response.getWriter();

        pw.println("<html>");
        pw.println("Your count is " + count + ".");
//        pw.println("<h1>First servlet work check by " + name + " " + surname +".</h1>");
        pw.println("</html>");

//        response.sendRedirect("/FirstJsp.jsp");
//        RequestDispatcher dispatcher = request.getRequestDispatcher("/FirstJsp.jsp");
//        dispatcher.forward(request, response);
    }
}
