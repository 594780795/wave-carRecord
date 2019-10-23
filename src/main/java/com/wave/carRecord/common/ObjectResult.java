/**
 * wavewisdom.com Inc. Copyright (c) 2013-2018 All Rights Reserved.
 */
package com.wave.carRecord.common;

import java.io.Serializable;

/**
 * @author 李建平
 * @date 2018/07/16 返回数据类返回参数
 */
public class ObjectResult<T> implements Serializable{

    private static final long serialVersionUID = 1L;
    private Integer code;
	private String msg;
	private T data;
	
	private ObjectResult() {
      
    }
	

	private ObjectResult(CodeMsg cm) {
		if (cm == null) {
			return;
		}
		this.code = cm.getRetCode();
		this.msg = cm.getMessage();
	}

	private ObjectResult(T data) {
		this.code = CodeMsg.SUCCESS_CODE;
		this.msg = CodeMsg.SUCCESS_DESC;
		this.data = data;
	}

	private ObjectResult(CodeMsg cm, T data) {
		this.code = cm.getRetCode();
		this.msg = cm.getMessage();
		this.data = data;
	}
	
	 /**
     * 判断返回结果是否成功
     */
    public boolean success() {
        return CodeMsg.SUCCESS_CODE == code;
    }

	/**
	 * 成功时候的调用
	 */
	public static <T> ObjectResult<T> newSuccess(T data) {
		return new ObjectResult<T>(data);
	}

	/**
	 * 特殊成功时候的调用
	 */
	public static <T> ObjectResult<T> newSuccess(CodeMsg cm, T data) {
		return new ObjectResult<T>(cm, data);
	}

	/**
	 * 成功，不需要传入参数
	 */
	@SuppressWarnings("unchecked")
	public static <T> ObjectResult<T> newSuccess() {
		return (ObjectResult<T>) newSuccess("");
	}

	/**
	 * 失败时候的调用
	 */
	public static <T> ObjectResult<T> newFailure(CodeMsg cm) {
		return new ObjectResult<T>(cm);
	}

	/**
	 * 失败时候的调用,扩展消息参数
	 */
	public static <T> ObjectResult<T> newFailure(CodeMsg cm, String msg) {
		cm.setMessage(cm.getMessage() + "--" + msg);
		return new ObjectResult<T>(cm);
	}

	/**
	 * Getter method for property <tt>code</tt>.
	 * 
	 * @return property value of code
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * Setter method for property <tt>code</tt>.
	 * 
	 * @param code
	 *            value to be assigned to property code
	 */
	public void setCode(Integer code) {
		this.code = code;
	}

	/**
	 * Getter method for property <tt>msg</tt>.
	 * 
	 * @return property value of msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * Setter method for property <tt>msg</tt>.
	 * 
	 * @param msg
	 *            value to be assigned to property msg
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	/**
	 * Getter method for property <tt>data</tt>.
	 * 
	 * @return property value of data
	 */
	public T getData() {
		return data;
	}

	/**
	 * Setter method for property <tt>data</tt>.
	 * 
	 * @param data
	 *            value to be assigned to property data
	 */
	public void setData(T data) {
		this.data = data;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return "ObjectResult [code=" + code + ", msg=" + msg + ", data=" + data + "]";
	}

}
