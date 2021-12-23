package com.github.eros.async.servlet;

import com.github.eros.async.facade.AsyncService;
import com.github.eros.async.listener.AppAsyncListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/22 11:30
 */
//@WebServlet(urlPatterns = "/async/*",asyncSupported = true)
public class AsyncFetchServlet extends HttpServlet {

    private static final long serialVersionUID = -5774675630765062066L;

    private static final Logger logger = LoggerFactory.getLogger(AsyncFetchServlet.class);

    private WebApplicationContext applicationContext;
    private AsyncService asyncService;
    private AppAsyncListener appAsyncListener;

    @Override
    public void init() throws ServletException {
        super.init();
        ServletContext context = this.getServletContext();
        this.applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);
        this.asyncService = this.applicationContext.getBean(AsyncService.class);
        this.appAsyncListener = new AppAsyncListener();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.startAsync();
        final AsyncContext asyncContext = req.getAsyncContext();
        asyncContext.addListener(this.appAsyncListener);
        asyncService.asyncFetch(asyncContext);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}