package ${package}.base.result;

/**
 * 返回结果数据结构
 */
public class Result<T> {
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
