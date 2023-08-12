package loga.dev9.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss z";
    public static final String TIMEZONE = "timezone";
    public static final String LAST_TIMEZONE_COOKIE = "lastTimezone";

    private transient TemplateEngine engine;

    @Override
    public void init() {
        engine = new TemplateEngine();
        configureTemplateEngine();
    }

    private void configureTemplateEngine() {
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(Objects.requireNonNull(getClass()
                        .getClassLoader()
                        .getResource("templates"))
                .getPath());
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String currentTimezone = getCurrentTimezone(request, response);
        renderTimezonePage(request, response, currentTimezone);
    }

    private String getCurrentTimezone(HttpServletRequest request, HttpServletResponse response) {
        String requestedTimezone = request.getParameter(TIMEZONE);
        String lastTimezone = getLastTimezoneFromCookies(request.getCookies());

        String currentTimezone = "";
        if (requestedTimezone != null && !requestedTimezone.isEmpty()) {
            currentTimezone = parseDateToFormat(requestedTimezone);
            response.addCookie(new Cookie(LAST_TIMEZONE_COOKIE, requestedTimezone));
        } else if (!lastTimezone.isEmpty()) {
            currentTimezone = parseDateToFormat(lastTimezone);
        } else {
            currentTimezone = parseDateToFormat("UTC");
        }
        return currentTimezone;
    }

    private String getLastTimezoneFromCookies(Cookie[] cookies) {
        String lastTimezone = "";
        Cookie lastTimezoneCookie = findCookieByName(cookies, LAST_TIMEZONE_COOKIE);

        if (lastTimezoneCookie != null) {
            lastTimezone = parseDateToFormat(lastTimezoneCookie.getValue());
        }
        return lastTimezone;
    }

    private Cookie findCookieByName(Cookie[] cookies, String name) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    private void renderTimezonePage(HttpServletRequest request, HttpServletResponse response, String currentTimezone) throws IOException {
        Context context = new Context(request.getLocale());
        context.setVariable("currentTimezone", currentTimezone);

        response.setContentType("text/html; charset=UTF-8");
        String templateName = "test";

        try {
            engine.process(templateName, context, response.getWriter());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }

    private String parseDateToFormat(String zoneId) {
        Instant date = new Date().toInstant();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_PATTERN)
                .withZone(ZoneId.of(zoneId));
        return dateFormat.format(date);
    }

}
