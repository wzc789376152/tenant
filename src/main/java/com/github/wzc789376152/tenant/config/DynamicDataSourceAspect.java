package com.github.wzc789376152.tenant.config;

import com.github.wzc789376152.tenant.context.DynamicDataSource;
import com.github.wzc789376152.tenant.context.DynamicDataSourceContextHolder;
import com.zaxxer.hikari.HikariDataSource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Order(1) // 请注意：这里order一定要小于tx:annotation-driven的order，即先执行DynamicDataSourceAspectAdvice切面，再执行事务切面，才能获取到最终的数据源
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class DynamicDataSourceAspect {
    @Autowired
    private DynamicDataSource dynamicDataSource;

    @Around("execution(* com.wzc789376152.*.controller.*.*(..))")
    public Object doAround(ProceedingJoinPoint jp) throws Throwable {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String tenantId = sra.getRequest().getParameter("tenantId");
        if (tenantId != null) {
            if (!DynamicDataSourceContextHolder.containDataSourceKey(tenantId)) {
                Map<Object, Object> dataSourceMap = new HashMap<>();
                HikariDataSource dataSource = new HikariDataSource();
                dataSource.setDriverClassName("com.mysql.jdbc.Driver");
                dataSource.setJdbcUrl("jdbc:mysql://www.yangmh.top:3306/tenant" + tenantId);
                dataSource.setUsername("root");
                dataSource.setPassword("wzc@789376152");
                dataSourceMap.put(tenantId, dataSource);
                //设置数据源
                dynamicDataSource.setDataSources(dataSourceMap);
                /**
                 * 必须执行此操作，才会重新初始化AbstractRoutingDataSource 中的 resolvedDataSources，也只有这样，动态切换才会起效
                 */
                dynamicDataSource.afterPropertiesSet();
            }
            DynamicDataSourceContextHolder.setDataSourceKey(tenantId);
        }

        Object result = jp.proceed();
        DynamicDataSourceContextHolder.clearDataSourceKey();
        return result;
    }
}
