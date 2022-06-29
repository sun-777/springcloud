package com.sun.client.controller;

import com.sun.common.enumeration.CodeKeyEnum;
import com.sun.common.util.StringUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;

/**
 * @description:  获取请求对象或响应对象。
 * @author: Sun Xiaodong
 */
public interface BaseController {

    default HttpServletRequest request() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

    default HttpServletResponse response() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
    }

    /**
     * 获取Session
     * @param allowCreate  如果没有Session，是否创建一个HttpSession
     * @return
     */
    default HttpSession session(boolean allowCreate) {
        return request().getSession(allowCreate);
    }


    // 根据给定的enumString，查找枚举接口类CodeKeyEnum的子类的枚举对象
    @SuppressWarnings("unchecked")
    default <T extends Enum<T> & CodeKeyEnum<T, C, K>, C extends Number, K> T findEnum(final T[] enumValues, String enumString) {
        T enumObject = null;
        if (enumValues.length > 0 && !StringUtil.isBlank(enumString)) {
            enumString = StringUtil.strip(enumString);
            try {
                final C c = enumValues[0].code();
                if (Integer.class.isInstance(c)) {  // C是Integer类型
                    final Integer code = Integer.valueOf(enumString);
                    enumObject = (T) enumValues[0].codeOf((C) code).orElse(null);
                } else if (Short.class.isInstance(c)) {  // C是Short类型
                    final Short code = Short.valueOf(enumString);
                    enumObject = (T) enumValues[0].codeOf((C) code).orElse(null);
                } else {
                    throw new RuntimeException("No suitable class type matched");
                }
            } catch (NumberFormatException e) {
                // 如果catch到NumberFormatException异常，说明参数enumString不是数字；
                // 那么，假设enumString是枚举对象的name()值，通过比较枚举对象name()一一比较确认
                try {
                    for (T value : enumValues) {
                        if (value.name().equalsIgnoreCase(enumString)) {
                            enumObject = value;
                            break;
                        }
                    }
                } catch (IllegalArgumentException lae) {
                }
            }
        }
        return enumObject;
    }

}
