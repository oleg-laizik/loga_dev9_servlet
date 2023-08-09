package loga.dev9.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {

    private TemplateEngine templateEngine;

    @Override
    public void init() {
        templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");

        String lastTimezone = "";
        String timezoneParam = request.getParameter("timezone");

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("lastTimezone".equals(cookie.getName())) {
                    lastTimezone = cookie.getValue();
                    break;
                }
            }
        }

        ZoneId timezone = parseTimezone(timezoneParam, lastTimezone);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        LocalDateTime currentTime;

        if (timezone != null) {
            currentTime = LocalDateTime.now(timezone);
            response.addCookie(new Cookie("lastTimezone", timezone.getId()));
        } else {
            currentTime = LocalDateTime.now();
        }

        String formattedTime = currentTime.format(formatter);

        Context context = new Context();
        context.setVariable("currentTime", formattedTime);
        templateEngine.process("test", context, response.getWriter());
    }

    private ZoneId parseTimezone(String timezone, String lastTimezone) {
        if (timezone != null) {
            return ZoneId.of(timezone);
        } else if (lastTimezone != null && !lastTimezone.isEmpty()) {
            return ZoneId.of(lastTimezone);
        }
        return null;
    }
}