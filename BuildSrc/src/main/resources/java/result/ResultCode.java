package ${package}.base.result;

/**
 * 返回编码,参考http语义
 */
public enum ResultCode {
    SUCCESS(200),//成功
    BIZ_FAIL(400),//业务异常错误
    UNAUTHORIZED(401),//未认证
    SIGN_FAIL(402),//签名错误
    NOT_FOUND(404),//接口不存在
    INTERNAL_SERVER_ERROR(500);//服务器内部错误

    private final int code;

    ResultCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}