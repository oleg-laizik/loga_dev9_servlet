package loga.dev9.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    public static final String TIMEZONE = "timezone";

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss z";
    private static final String LAST_TIMEZONE_PARAM = "lastTimezone";
    private static final String UTC = "UTC";
    private transient TemplateEngine engine;

    @Override
    public void init() {
        engine = new TemplateEngine();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws IOException {
        String currentDateTime = "";

        String timezoneParam = httpServletRequest.getParameter(TIMEZONE);
        if (timezoneParam == null || timezoneParam.isEmpty()) {
            timezoneParam = getLastTimezoneFromCookiesOrUTC(httpServletRequest.getCookies());
        } else {
            httpServletResponse.addCookie(new Cookie(LAST_TIMEZONE_PARAM, timezoneParam));
        }
        currentDateTime = getCurrentDateTime(timezoneParam);

        Context context = new Context(
                httpServletRequest.getLocale(),
                Collections.singletonMap("currentDateTime", currentDateTime)
        );

        httpServletResponse.setContentType("text/html");
        PrintWriter writer = httpServletResponse.getWriter();
        engine.process("test", context, writer);
        writer.close();
    }

    private String getLastTimezoneFromCookiesOrUTC(Cookie[] cookies) {
        String cookieValue = getParamFromCookies(cookies);
        if (cookieValue == null) {
            return "UTC";
        }
        return cookieValue;
    }

    private String getParamFromCookies(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(e -> e.getName().equals(LAST_TIMEZONE_PARAM))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    public String getCurrentDateTime(String zoneId) {
        Instant now = Instant.now();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_PATTERN)
                .withZone(ZoneId.of(zoneId));
        return dateFormat.format(now);
    }
}