package loga.dev9.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Set;

@WebFilter(value = "/time")
public class TimezoneValidateFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request.getParameter("timezone") != null) {
            String timezone = request.getParameter("timezone");
            if (isValidTimezone(timezone)) {
                response.addCookie(new Cookie("lastTimezone", timezone));
                chain.doFilter(request, response);
            } else {
                response.setStatus(400);
                response.setContentType("text/plain; charset=utf-8");
                response.getWriter().write("Invalid timezone");
                response.getWriter().close();
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isValidTimezone(String timezone) {
        return ZoneId.getAvailableZoneIds().contains(timezone);
    }
}