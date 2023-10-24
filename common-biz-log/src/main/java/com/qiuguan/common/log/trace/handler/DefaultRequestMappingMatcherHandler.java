package com.qiuguan.common.log.trace.handler;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qiuguan.common.log.trace.bean.PermissionBean;
import com.qiuguan.common.log.trace.constants.LogConstants;
import com.qiuguan.common.log.trace.logbiz.dao.ServicesLogSkipUrlsMapper;
import com.qiuguan.common.log.trace.logbiz.entity.ServicesLogSkipUrls;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fu yuan hui
 * @date 2023-08-01 18:19:57 Tuesday
 */
@Slf4j
@Component
public class DefaultRequestMappingMatcherHandler implements RequestMappingMatcherHandler {

    private final StringRedisTemplate stringRedisTemplate;

    private final ServicesLogSkipUrlsMapper servicesLogSkipUrlsMapper;
    private final Map<String, PermissionBean> permissionMap = Maps.newConcurrentMap();

    private final Map<String, PermissionBean> patternPermissionMap = Maps.newConcurrentMap();

    private final Set<String> urls = Sets.newConcurrentHashSet();

    private final Set<String> patternUrls = Sets.newConcurrentHashSet();

    private final Map<String, Integer> singletonUrlMultiUses = Maps.newConcurrentMap();

    private final Set<String> configurableSkipUrls = Sets.newConcurrentHashSet();

    //需要忽略的注解就放到这里，暂时发现了@InitBinder, 不要用注解Class,因为可能会是代理
    private final Set<String> ignoreAnnotations = ImmutableSet.of(
            InitBinder.class.getCanonicalName(), ExceptionHandler.class.getCanonicalName());

    public DefaultRequestMappingMatcherHandler(StringRedisTemplate stringRedisTemplate,
                                               ServicesLogSkipUrlsMapper servicesLogSkipUrlsMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.servicesLogSkipUrlsMapper = servicesLogSkipUrlsMapper;
    }

    @Override
    public boolean match(String url) {
        if (this.urls.contains(url)) {
            return true;
        }

        //匹配restful格式的url
        for (String patternUrl : this.patternUrls) {
            Pattern compile = Pattern.compile(patternUrl);
            Matcher matcher = compile.matcher(url);
            if (matcher.matches()) {
               return true;
            }
        }

        return false;
    }

    /**
     * 每个模块启动都会扫描common-log 包，并去创建包路径下的对象，并且每个模块启动创建的对象都不一样，相当于每个模块都有自己的容器
     * 所以这里要确保每个模块都能够访问到自己的Map对象。
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        List<String> allPermissions = this.stringRedisTemplate.opsForList().range(LogConstants.LOG_PERMISSION_KEY, 0, -1);
        Optional.ofNullable(allPermissions).orElseGet(ArrayList::new).forEach(p -> {
            PermissionBean bean = JSON.parseObject(p, PermissionBean.class);
            if (bean != null && !StringUtils.isBlank(bean.getMappingUrl())) {
                //普通url
                if (!bean.getMappingUrl().contains(RESTFUL_PATTERN_PREFIX) && !bean.getMappingUrl().contains(RESTFUL_PATTERN_SUFFIX)) {
                    this.permissionMap.put(bean.getMappingUrl(), bean);
                    this.singletonUrlMultiUses.compute(bean.getMappingUrl(), (k, v) -> v == null ? 0 : (v + 1));
                    this.urls.add(bean.getMappingUrl());
                } else {
                    resolveRestfulUrl(bean.getMappingUrl(), bean);
                }
            }
        });

        initSkipUrls();


        log.info("common模块构建hr_permissions缓存成功个数: {}, restful pattern url list: {}", permissionMap.size(), this.patternUrls);
    }

    private void initSkipUrls() {
        Set<String> skipUrls = ImmutableSet.of("/login", "/logout", "/token",
                "/token/logout", "/token/{token}", "toDo", "haveMessage", "/user/getUserPageSet",
                "/user/getUserSigns", "/user/getUserPageSetForSetting", "/user/getUserList");
        this.configurableSkipUrls.addAll(skipUrls);

        List<ServicesLogSkipUrls> servicesLogSkipUrls = this.servicesLogSkipUrlsMapper.selectList(Wrappers.lambdaQuery());
        servicesLogSkipUrls.forEach(x -> configurableSkipUrls.add(x.getUrl()));
    }


    @Override
    public boolean skip(String url) {
        boolean contains = configurableSkipUrls.contains(url);
        if (!contains) {
            for (String configurableSkipUrl : configurableSkipUrls) {
                if (configurableSkipUrl.contains(RESTFUL_PATTERN_PREFIX) && configurableSkipUrl.contains(RESTFUL_PATTERN_SUFFIX)) {
                    String restfulUrl = configurableSkipUrl.replaceAll("\\{[^/]+\\}", "[^/]+");
                    Pattern compile = Pattern.compile(restfulUrl);
                    Matcher matcher = compile.matcher(url);
                    if (matcher.matches()) {
                        contains = true;
                    }
                }
            }
        }

        return contains;
    }

    @Override
    public PermissionBean mapping(String url) {
        PermissionBean permissionBean = this.permissionMap.get(url);
        //restful url 还需要匹配
        if (permissionBean == null) {
            for (String patternUrl : this.patternUrls) {
                Pattern compile = Pattern.compile(patternUrl);
                Matcher matcher = compile.matcher(url);
                if (matcher.matches()) {
                    return this.patternPermissionMap.get(patternUrl);
                }
            }
        }

        return permissionBean;
    }

    @Override
    public boolean singleUrlMappingMultiUse(String url) {
        //restful 风格的url, 应该没有重用的
        Integer count = this.singletonUrlMultiUses.get(url);
        return !Objects.isNull(count) && count > 0;
    }

    @Override
    public boolean ignoreAnnotations(Method method) {
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            if (this.ignoreAnnotations.contains(annotation.annotationType().getCanonicalName())) {
                return true;
            }
        }

        return false;
    }

    private void resolveRestfulUrl(String mappingUrl, PermissionBean permissionBean) {
        if (mappingUrl.contains(RESTFUL_PATTERN_PREFIX) && mappingUrl.contains(RESTFUL_PATTERN_SUFFIX)) {
            String restfulUrl = mappingUrl.replaceAll("\\{[^/]+\\}", "[^/]+");
            restfulUrl = PATTERN_BEGIN + restfulUrl + PATTERN_END;
            this.patternUrls.add(restfulUrl);
            this.patternPermissionMap.put(restfulUrl, permissionBean);
        }
    }
}
