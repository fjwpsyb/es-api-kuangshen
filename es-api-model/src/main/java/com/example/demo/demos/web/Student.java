package com.example.demo.demos.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author YuanBo.Shi
 * @date 2021年08月16日 9:06 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class Student {
    private String name;
    private Integer age;
}
