package com.courigistics.courigisticsbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * Email configuration for asynchronous email sending using virtual threads
 *
 * Virtual threads (Java 21+) are lightweight threads that can handle thousands
 * of concurrent operations without the overhead of platform threads.
 *
 * Benefits:
 * - Simple configuration (no complex thread pool tuning)
 * - Handles high email volume efficiently
 * - Can be replaced with RabbitMQ later without changing service code
 * - Better than platform threads for I/O-bound tasks (email sending)
 *
 * @see com.courigistics.courigisticsbackend.services.email.EmailServiceImpl for usage
 */

@EnableAsync
@Slf4j
@Configuration
public class EmailConfig implements AsyncConfigurer{

    /**
     * Creates a virtual thread executor for email operations
     *
     * Virtual threads are:
     * - Cheap to create (millions can exist)
     * - Automatically managed by JVM
     * - Perfect for I/O operations like email sending
     *
     * Each email send operation gets its own virtual thread, allowing
     * thousands of emails to be sent concurrently without blocking.
     */
    @Bean(name = "emailTaskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        log.info("Initializing virtual thread executor for email operations");

        return (Executor) Thread.ofVirtual()
                .name("email-vt-", 0) // Virtual thread naming: email-vt-0, email-vt-1, etc
                .factory();

    }

    /**
     * Creates a template resolver for email templates
     *
     * This tells Thymeleaf where and how to find email templates.
     * Templates are located in: src/main/resources/templates/emails/
     *
     * Configuration:
     * - Prefix: "templates/emails/" - Template location
     * - Suffix: ".html" - Template file extension
     * - Mode: HTML - Parse as HTML5
     * - Encoding: UTF-8 - Character encoding
     * - Cacheable: false (dev) / true (prod) - Cache for performance
     *
     * @return Template resolver configured for email templates
     */
    @Bean
    @Description("Thymeleaf template resolver for email HTML templates")
    public ITemplateResolver emailTemplateResolver(){
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

        // Template location and format to follow
        templateResolver.setPrefix("templates/emails/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");

        // character encoding
        templateResolver.setCharacterEncoding("UTF-8");

        // Caching information
        // in development: false (templates reload on change)
        // in production: true (better for performance)
        templateResolver.setCacheable(false);

        // Cache TTL in milliseconds (if caching enabled)
        templateResolver.setCacheTTLMs(3600000L); // 1 hour cache limits
        log.info("Configured email template resolver: prefix=templates/emails/, suffix = .html");
        return templateResolver;
    }

    /**
     * Creates a Thymeleaf template engine for processing email templates
     *
     * The template engine uses the emailTemplateResolver to:
     * 1. Locate email templates
     * 2. Process Thymeleaf expressions (${variable})
     * 3. Generate HTML email content
     *
     * Example usage in EmailServiceImpl:
     * <pre>
     * Context context = new Context();
     * context.setVariable("firstName", "John");
     * String html = emailTemplateEngine.process("courier-verification", context);
     * </pre>
     *
     * This will:
     * 1. Load: templates/emails/courier-verification.html
     * 2. Replace: ${firstName} with "John"
     * 3. Return: Final HTML string
     *
     * @return Configured Spring template engine for emails
     */
    @Bean
    @Description("Thymeleaf template engine for processing email templates")
    public SpringTemplateEngine emailTemplateEngine(){
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(emailTemplateResolver());

        log.info("Configure email template engine with custom resolver");

        return templateEngine;
    }

    /**
     * Global exception handler for async email operations
     *
     * Catches exceptions thrown by async email methods and logs them.
     * Prevents silent failures in background threads.
     *
     * Common scenarios:
     * - SMTP connection failures
     * - Template rendering errors (missing variables, syntax errors)
     * - Invalid email addresses
     * - Network timeouts
     *
     * Exception details logged:
     * - Method name that failed
     * - Virtual thread name
     * - Exception message and stack trace
     * - Recipient email (if available)
     *
     * @return Exception handler for async operations
     */
    @Nullable
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable ex, Method method, Object... params) {
                log.error("=== Async Email Error ===");
                log.error("Method: {}", method.getName());
                log.error("Thread: {}", Thread.currentThread().getName());
                log.error("Exception: {}", ex.getMessage(), ex);

                // Log email recipient if available (first param is usually 'to' address)
                if (params.length > 0 && params[0] instanceof String) {
                    log.error("Failed to send email to: {}", params[0]);
                }

                /** TODO: In production, consider:
                 * 1. Retry logic (exponential backoff)
                 * 2. Dead letter queue for failed emails
                 * 3. Alert admins on critical email failures
                 * 4. Store failed email in database for later retry
                 * 5. Publish failure event for monitoring
                 */
            }
        };
    }
}

