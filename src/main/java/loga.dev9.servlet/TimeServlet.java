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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss z";
    public static final String TIMEZONE = "timezone";
    private transient TemplateEngine engine;

    @Override
    public void init() {
        engine = new TemplateEngine();

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
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws IOException {
        String lastTimezone = "";
        String currentTimezone = "";
        httpServletResponse.setContentType("text/html");

        String requestedTimezone = httpServletRequest.getParameter(TIMEZONE);
        if (requestedTimezone == null || requestedTimezone.isEmpty()) {
            Cookie[] cookies = httpServletRequest.getCookies();
            if (cookies != null && cookies.length > 0) {
                lastTimezone = getLastTimezoneFromCookies(lastTimezone, cookies);
            } else {
                currentTimezone = parseDateToFormat("UTC");
            }
        } else {
            httpServletResponse.addCookie(new Cookie("lastTimezone", requestedTimezone));
            currentTimezone = parseDateToFormat(requestedTimezone);
        }

        Context context = new Context(
                httpServletRequest.getLocale(),
                Map.of("lastTimezone", lastTimezone, "currentTimezone", currentTimezone)
        );

        engine.process("test", context, httpServletResponse.getWriter());
        httpServletResponse.getWriter().close();
    }

    private String getLastTimezoneFromCookies(String lastTimezone, Cookie[] cookies) {
        String cookie = getCookie(cookies);
        if (!cookie.isEmpty()) {
            lastTimezone = parseDateToFormat(cookie);
        }
        return lastTimezone;
    }

    private String getCookie(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .findFirst()
                .map(Cookie::getValue)
                .orElse("");
    }

    public String parseDateToFormat(String zoneId) {
        Instant date = new Date().toInstant();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_PATTERN)
                .withZone(ZoneId.of(zoneId));
        return dateFormat.format(date);
    }
}