/**
 * wavewisdom.com Inc. Copyright (c) 2013-2018 All Rights Reserved.
 */
package com.wave.carRecord.common;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author jyc
 * @date 2018/09/18
 */
@Component
@ConfigurationProperties(prefix = "valid")
@Data
public class ValidCodeConfig {
    // 验证码
    private String code;
}
