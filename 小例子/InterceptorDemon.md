## InterceptorDemon



```java
package com.demon;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorDemon {
    
    class MyInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws Exception {
            System.out.println("开始处理逻辑");
            return true;
        }

    }

    @Configuration
    class InterceptorConfig implements WebMvcConfigurer {

        @Override
	public void addInterceptors(InterceptorRegistry registry) {
	    System.out.println("注册拦截器");
	    registry.addInterceptor(getInterceptor()).addPathPatterns("/**");
	}

	@Bean
	public MyInterceptor getInterceptor() {
	    return new MyInterceptor();
	}
    }
}
```
