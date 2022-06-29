package com.sun.common.util;

import com.sun.common.enumeration.CodeMsg;

import java.io.Serializable;

import static com.sun.common.util.Assertions.isTrueArgument;
import static com.sun.common.util.Assertions.notNull;


public class Result<T> implements Serializable{

    private static final long serialVersionUID = 1133213058406185006L;

    /*
    private final Integer code;
    private final String message;
    // 返回给浏览器的数据
    private final T data;
    */
    private Integer code;
    private String message;
    // 返回给浏览器的数据
    private T data;

    public Result() {}

    private Result(final Builder<T> builder) {
        this.code = Integer.valueOf(builder.code);
        this.message = builder.message;
        this.data = builder.data;
    }
    
    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    /*
    public boolean isSuccess() {
        final int successCode = CodeMsg.SUCCESS.code().intValue();
        return Integer.valueOf(successCode).equals(this.code);
    }*/

    /**
     * 返回执行结果
     * @return  true: 表示执行成功；false: 表示执行失败。
     */
    public boolean result() {
        // 只能以SUCCESS code判断
        final int successCode = CodeMsg.SUCCESS.code();
        return successCode == this.code;
    }

    /**
     * 设置错误代码信息，不为1（只有1表示正确，其他表示错误）
     * @param codeMsg
     * @return
     */
    public static <T> Result<T> error(CodeMsg codeMsg) {
        return Result.<T>builder().error(codeMsg).build();
    }


    public static <T> Result<T> error(){
        return Result.<T>builder().error().build();
    }


    public static <T> Result<T> error(String message){
        return Result.<T>builder().error(null, message).build();
    }


    public static <T> Result<T> error(T data){
        return Result.<T>builder().error(data).build();
    }

    public static <T> Result<T> error(T data, Throwable e){
        return Result.<T>builder().error(data, e).build();
    }

    public static <T> Result<T> error(T data, String message){
        return Result.<T>builder().error(data, message).build();
    }


    public static <T> Result<T> success(){
        return Result.<T>builder().success(null, null).build();
    }


    public static <T> Result<T> success(T data){
        return Result.<T>builder().success(data, null).build();
    }


    public static <T> Result<T> success(T data, String message){
        return Result.<T>builder().success(data, message).build();
    }


    @Override
    public String toString() {

        return new StringBuffer().append(this.getClass().getSimpleName())
                                 .append(" [code=").append(code)
                                 .append(", message=").append(message)
                                 .append(", data=").append(null == data ? "" : data.toString())
                                 .append("]")
                                 .toString();
    }


    public static <T> Builder<T> builder() {
        return new Builder<>();
    }


    public static final class Builder<T> {
        private Integer code;
        private String message;
        // 返回给浏览器的数据
        private T data;

        private Builder() {
        }


        Builder<T> setCode(Integer code) {
            this.code = code;
            return this;
        }

        Builder<T> setMessage(String message) {
            this.message = message;
            return this;
        }


        Builder<T> setData(T data) {
            this.data = data;
            return this;
        }


        private Builder<T> success(T data, String message) {
            this.code = Integer.valueOf(CodeMsg.SUCCESS.code());
            this.message = StringUtil.isBlank(message) ? CodeMsg.SUCCESS.key() : message;
            this.data = data;
            return this;
        }


        private Builder<T> error() {
            this.data = null;
            this.code = Integer.valueOf(CodeMsg.ERROR.code());
            this.message = CodeMsg.ERROR.key();
            return this;
        }

        private Builder<T> error(T data) {
            this.data = data;
            this.code = Integer.valueOf(CodeMsg.ERROR.code());
            this.message = CodeMsg.ERROR.key();
            return this;
        }

        private Builder<T> error(T data, String message) {
            this.data = data;
            this.code = Integer.valueOf(CodeMsg.ERROR.code());
            this.message = StringUtil.isBlank(message) ? CodeMsg.ERROR.key() : message;
            return this;
        }

        private Builder<T> error(T data, Throwable throwable) {
            this.data = data;
            this.code = Integer.valueOf(CodeMsg.ERROR.code());
            if (null != throwable) {
                this.message = throwable.getClass().getName() + ":" + throwable.getMessage();
            } else {
                this.message = CodeMsg.ERROR.key();
            }
            return this;
        }


        private Builder<T> error(final CodeMsg codeMsg) {
            notNull("codeMsg", codeMsg);
            final Integer code = Integer.valueOf(codeMsg.code());
            isTrueArgument("not equal to 1", !CodeMsg.SUCCESS.code().equals(code));
            this.data = null;
            this.code = code;
            this.message = codeMsg.key();
            return this;
        }


        public Result<T> build() {
            return new Result<>(this);
        }

    }
}
