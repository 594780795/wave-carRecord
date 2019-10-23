package com.wave.carRecord.common;

/**
 * 公共错误码 999开头
 * 
 * @author 李建平
 * @date 2018/08/22
 */
public class CodeMsg {

	private Integer retCode;
	private String message;

	// 成功
	public static final Integer SUCCESS_CODE = 0;
	public static final String SUCCESS_DESC = "success";

	// 失败
	public static final Integer FAILED_CODE = -1;

	// 按照模块定义CodeMsg
	// 通用异常
	public static CodeMsg SUCCESS = new CodeMsg(0, SUCCESS_DESC);
	public static CodeMsg LOGINSUCCESS = new CodeMsg(0, "登录成功");
	public static CodeMsg SERVER_EXCEPTION = new CodeMsg(999001, "系统繁忙，请稍后再试");
	public static CodeMsg ERROR_VALID = new CodeMsg(999010, "密码错误");

	public CodeMsg(int retCode, String message) {
		this.retCode = retCode;
		this.message = message;
	}

	public int getRetCode() {
		return retCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "CodeMsg [retCode=" + retCode + ", message=" + message + "]";
	}
}
