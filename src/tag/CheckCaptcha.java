package tag;

import entity.Captcha;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

public class CheckCaptcha extends SimpleTagSupport {
    private String captchaName;

    public void setCaptchaName(String captchaName){
        this.captchaName = captchaName;
    }

    public void doTag() throws JspException, IOException{
        String errorMessage = null;

        Captcha currentCaptcha = (Captcha) getJspContext().getAttribute("currentCaptcha", PageContext.APPLICATION_SCOPE);
        if (!currentCaptcha.getName().equals(captchaName)){
            errorMessage = "CAPTCHA введена неправильно";
        }

        RandomCaptcha();

        getJspContext().setAttribute("errorMessage", errorMessage, PageContext.SESSION_SCOPE);
    }

    private void RandomCaptcha(){
        LinkedList<Captcha> captchas = (LinkedList<Captcha>) getJspContext().getAttribute("captchas", PageContext.APPLICATION_SCOPE);

        Random random = new Random();
        int index = random.nextInt(captchas.size());

        getJspContext().setAttribute("currentCaptcha", captchas.get(index), PageContext.APPLICATION_SCOPE);
    }
}
