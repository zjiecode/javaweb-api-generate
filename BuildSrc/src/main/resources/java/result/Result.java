package ${package}.base.result;

/**
 * 返回结果数据结构
 */
public class Result<T> {
    //列举常见的错误类型
    public static final Result SUCCESS = new Result(ResultCode.SUCCESS, "处理成功");
    public static final Result BIZ_FAIL = new Result(ResultCode.BIZ_FAIL, "请求失败");
    public static final Result UNAUTHORIZED = new Result(ResultCode.UNAUTHORIZED, "认证失败，请重新登陆");
    public static final Result SIGN_FAIL = new Result(ResultCode.SIGN_FAIL, "接口签名错误");
    public static final Result NOT_FOUND = new Result(ResultCode.NOT_FOUND, "请求资源不存在");
    public static final Result INTERNAL_SERVER_ERROR = new Result(ResultCode.INTERNAL_SERVER_ERROR, "服务器内部错误");

    private Integer code;
    private String msg;
    private T data;

    public Result(ResultCode code, String msg, T data) {
        this.code = code.getCode();
        this.msg = msg;
        this.data = data;
    }

    public Result(ResultCode code, String msg) {
        this.code = code.getCode();
        this.msg = msg;
    }

    public static <T> Result getSuccess(T data) {
        return new Result(ResultCode.SUCCESS, "处理成功", data);
    }

    public static Result getBizFail(String msg) {
        return new Result(ResultCode.BIZ_FAIL, msg);
    }

    public Integer getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "[" + getCode() + "]" + getMsg();
    }
}
