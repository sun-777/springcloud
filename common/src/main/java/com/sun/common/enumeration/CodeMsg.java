package com.sun.common.enumeration;

import com.sun.common.util.StringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum CodeMsg implements CodeKeyEnum<CodeMsg, Short, String>{
	// 通用错误码
	UNKNOWN_ERROR((short) -1,"未知错误"),
	ERROR((short) 0, "错误"),
	SUCCESS((short) 1,"成功"),
	RESOURCE_NOT_FOUND((short) 1001,"没有找到相关资源"),
	PARAMETER_ERROR((short) 1002,"参数错误"),
	PARAMETER_MISSING((short) 1003,"缺失必要的参数"),
	SERVER_ERROR((short) 1010,"服务端异常"),
	
	REQUEST_ILLEGAL((short) 1021,"非法请求"),
	ACCESS_LIMIT_REACHED((short) 1022,"访问太频繁"),
	
	// 登陆模块错误码
	SESSION_NOT_EXISTS((short) 1051, "会话不存在"),
	SESSION_EXPIRED((short) 1052, "会话失效"),
	USERNAME_ILLEGAL((short) 1053,"无效用户名"),
	USERNAME_NOT_EXISTS((short) 1054,"用户名不存在"),
	PASSWORD_EMPTY((short) 1055,"登录密码不能为空"),
	PASSWORD_ERROR((short) 1056,"密码错误"),
	USER_NOT_LOGIN((short) 1057,"用户未登录"),
	USER_TOKEN_EXPIRED((short) 1058,"token失效"),
	
	// 
	MYSQL_EXCEPTION((short) 1100, "数据库异常"),
	REDIS_EXCEPTION((short) 1105, "Redis异常");
	
	private final Short code;
	private final String key;
	
	
    private final static Map<Short, CodeMsg> CODE_MAP;
    private final static Map<String, CodeMsg> KEY_MAP;
    
    static {
        CODE_MAP = Collections.unmodifiableMap(Arrays.stream(CodeMsg.values()).collect(Collectors.toMap(CodeMsg::code, Function.identity())));
        KEY_MAP = Collections.unmodifiableMap(Arrays.stream(CodeMsg.values()).collect(Collectors.toMap(k -> k.key().toLowerCase(), Function.identity())));
    }

	CodeMsg(Short code, String key) {
		this.code = code;
		this.key = key;
	}
	
	
    @Override
    public Short code() {
        return this.code;
    }

    @Override
    public Optional<CodeMsg> codeOf(Short c) {
        return Optional.ofNullable(null == c ? null : CODE_MAP.get(c));
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public Optional<CodeMsg> keyOf(String k) {
        if (StringUtil.isBlank(k)) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(KEY_MAP.get(k.toLowerCase()));
        }
    }
	
}
