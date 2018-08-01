package ${package}.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import ${package}.base.exception.AppException;
import ${package}.base.exception.BizException;
import ${package}.base.result.Result;
import ${package}.base.result.ResultCode;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * 应用程序配置，包括配置拦截器，异常等；
 */
@Configuration
public class AppWebMvcConfigurer implements WebMvcConfigurer {
    Logger logger = LoggerFactory.getLogger(AppWebMvcConfigurer.class);

    @Value("${privateKey}")
    String privateKey;//密钥，在配置文件
    @Value("${maxTimeGap}")
    Integer maxTimeGap;//验证签名，最大的时间差，在配置文件

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //打印请求日志
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                logger.info(getIpAddress(request) + "-" + request.getMethod() + "[" + request.getRequestURI() + "]");
                return true;
            }
        });
//        //添加签名验证
//        registry.addInterceptor(new HandlerInterceptor() {
//            @Override
//            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//                if (!validateSign(request)) {
//                    Result result = new Result(ResultCode.SIGN_FAIL, "[(" + request.getMethod() + ")" + request.getRequestURI() + "]非法请求");
//                    responseResult(response, result);
//                    logger.warn(result.toString());
//                    return false;
//                }
//                return true;
//            }
//        });
    }

    /**
     * 一个简单的签名认证，规则：
     * 1. 将请求参数按ascii码排序
     * 2. 拼接为a=value&b=value...这样的字符串（不包含sign）
     * 3. 混合密钥（secret）进行md5获得签名，与请求的签名进行比较
     */
    private boolean validateSign(HttpServletRequest request) {
        String t = request.getHeader("t");//获得请求的时间戳
        if (StringUtils.isEmpty(t)) {
            return false;
        }
        long timestamp = Long.valueOf(t);
        if (Math.abs(System.currentTimeMillis() - timestamp) > maxTimeGap) {
            return false;
        }
        String requestSign = request.getHeader("sign");//获得请求签名，如sign=19e907700db7ad91318424a97c54ed57
        if (StringUtils.isEmpty(requestSign)) {
            return false;
        }
        List<String> keys = new ArrayList<String>(request.getParameterMap().keySet());
        Collections.sort(keys);//排序
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(key).append("=").append(request.getParameter(key)).append("&");//拼接字符串
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(privateKey);
        String sign = DigestUtils.md5DigestAsHex(sb.toString().getBytes());//混合密钥md5
        return requestSign.equals(sign);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //配置跨域请求
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .allowedMethods("*");
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add((request, response, handler, ex) -> {
            Result result;
            if (ex instanceof BizException) {
                //业务异常,不需要打印堆栈
                result = new Result(((BizException) ex).getResultCode(), ex.getMessage());
                responseResult(response, result);
                logger.warn(result.toString());
            } else if (ex instanceof AppException) {
                //应用异常
                result = new Result(((AppException) ex).getResultCode(), ex.getMessage());
                responseResult(response, result);
                logger.error(result.toString(), ex);
            } else if (ex instanceof NoHandlerFoundException || ex instanceof HttpRequestMethodNotSupportedException) {
                //不是服务器的异常,不需要打印堆栈
                result = new Result(ResultCode.NOT_FOUND, "接口[(" + request.getMethod() + ")" + request.getRequestURI() + "]不存在");
                responseResult(response, result);
                logger.warn(result.toString());
            } else if (ex instanceof BindException || ex instanceof MethodArgumentNotValidException) {
                //参数不合法
                List<ObjectError> errors;
                if (ex instanceof BindException) {
                    errors = ((BindException) ex).getAllErrors();
                } else {
                    errors = ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors();
                }
                if (!errors.isEmpty()) {
                    result = new Result(ResultCode.BIZ_FAIL, errors.get(0).getDefaultMessage());
                } else {
                    result = new Result(ResultCode.BIZ_FAIL, "数据验证错误");
                }
                responseResult(response, result);
                logger.warn(result.toString());
            } else if (ex instanceof ServletException) {
                result = new Result(ResultCode.INTERNAL_SERVER_ERROR, ex.getMessage());
                responseResult(response, result);
                logger.error(result.toString(), ex);
            } else {
                //其他错误
                String message = String.format("接口 [%s] 出现异常，异常摘要：%s", request.getRequestURI(), ex.getMessage());
                result = new Result(ResultCode.INTERNAL_SERVER_ERROR, message);
                responseResult(response, result);
                logger.error(result.toString(), ex);
            }
            return new ModelAndView();
        });
    }

    /**
     * 遇到错误，拦截以后输出响应到客户端
     */
    private void responseResult(HttpServletResponse response, Result result) {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setStatus(200);
        try {
            ObjectMapper mapper = new ObjectMapper();
            response.getWriter().write(mapper.writeValueAsString(result));
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * 获取客户端ip
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，那么取第一个ip为客户端ip
        if (ip != null && ip.indexOf(",") != -1) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
}
