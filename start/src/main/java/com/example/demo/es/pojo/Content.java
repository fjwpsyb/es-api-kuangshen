package com.example.demo.es.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author YuanBo.Shi
 * @date 2021年08月18日 7:51 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Content {
    /**
     * 商品标题
     * @date 2021/8/24 8:15 下午
     */
    private String title;

    /**
     * 商品价格
     * @date 2021/8/24 8:15 下午
     */
    private String price;

    /**
     * 商品图片
     * @date 2021/8/24 8:15 下午
     */
    private String img;

    /**
     * 店铺名称
     * @date 2021/8/24 8:15 下午
     */
    private String shopName;

    /**
     * 评价
     * @date 2021/8/24 8:15 下午
     */
    private String commit;
}
