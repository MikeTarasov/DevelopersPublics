package main.com.skillbox.ru.developerspublics.config;


import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;


@Configuration
public class Initializer implements WebApplicationInitializer
{
    // Указываем имя нашему Servlet Dispatcher для мапинга
    private static final String DISPATCHER_SERVLET_NAME = "dispatcher";


    @Override
    @SneakyThrows
    public void onStartup(ServletContext servletContext) {
        //создаем контекст
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();

        // Регистрируем в контексте конфигурационный класс
        context.register(WebMVCConfig.class);

        // добавляем в контекст слушателя с нашей конфигурацией
        servletContext.addListener(new ContextLoaderListener(context));
        context.setServletContext(servletContext);

        // настраиваем маппинг Dispatcher Servlet-а
        ServletRegistration.Dynamic servlet = servletContext.addServlet(DISPATCHER_SERVLET_NAME,
                new DispatcherServlet(context));
        servlet.addMapping("/");
        servlet.setLoadOnStartup(1);
    }
}