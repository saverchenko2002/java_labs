package servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletConfig;
import entity.Ad;
import entity.AdList;
import entity.Captcha;
import entity.UserList;
import helper.AdListHelper;
import helper.UserListHelper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

@WebServlet(name = "StartupServlet")
public class StartupServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        UserList userList = UserListHelper.readUserList(getServletContext());

        captchaInit(config);

        getServletContext().setAttribute("users", userList);

        AdList adList = AdListHelper.readAdList(getServletContext());

        getServletContext().setAttribute("ads", adList);
        for (Ad ad: adList.getAds()) {
            ad.setAuthor(userList.findUser(ad.getAuthorId()));
            ad.setLastModified(ad.getLastModified());
        }
    }

    private void captchaInit(ServletConfig config) throws ServletException{
        LinkedList<Captcha> captchas = new LinkedList<Captcha>();
        captchas.add(new Captcha("CAPTCHA", "https://sun9-42.userapi.com/impg/LoaS0nLtp8BZfoRAGFgv-8r8YcqD1xOPDJyJXA/TdYevRgdORg.jpg?size=1300x488&quality=96&sign=4fb9606c49b5cd2feaf6e26db505003d&type=album"));
        captchas.add(new Captcha("qGphJD", "https://sun9-38.userapi.com/impg/Ovkzs0SWuXxxcRNXgWefeHxqxjngbicYzb6kRA/Oclj7gGBSU0.jpg?size=2378x1010&quality=96&sign=593c477c05a5e177977b3681b13250de&type=album"));
        captchas.add(new Captcha("smwm", "https://sun9-22.userapi.com/impg/rsUR7wgwmXfA83NE8uyfFSZRXBYMTHOjAtyCpg/tL8-DGaBB20.jpg?size=290x80&quality=96&sign=b54bfe4c83be0d510a974aed7433b961&type=album"));

        Random random = new Random();
        int index = random.nextInt(captchas.size());

        getServletContext().setAttribute("captchas", captchas);
        getServletContext().setAttribute("currentCaptcha", captchas.get(index));
    }
}
