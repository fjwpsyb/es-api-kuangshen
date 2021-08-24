package com.example.demo.es.controller;

import com.example.demo.demos.resultvo.ResultVo;
import com.example.demo.demos.resultvo.ResultVoUtil;
import com.example.demo.es.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * @author YuanBo.Shi
 * @date 2021年08月18日 9:07 下午
 */
@RestController
public class ContentController {

    /**
     * 解析jd页面数据并存入es
     * @author YuanBo.Shi
     * @date 2021/8/24 12:55 下午
     * @param keyword 关键词
     * @return com.example.demo.demos.resultvo.ResultVo
     */
    @GetMapping("parse/{keyword}")
    public ResultVo parse(@PathVariable("keyword") String keyword) {
        ResultVo vo = contentService.parseContent(keyword);
        if (vo.getCode() != 200) {
            return vo;
        }

        return ResultVoUtil.success();
    }

    /**
     * 搜索数据
     * search/1/10/java
     * @author YuanBo.Shi
     * @date 2021/8/24 12:55 下午
     * @param pageNum 当前页数
     * @param pageSize 每页显示条数
     * @param keyword 关键词
     * @return com.example.demo.demos.resultvo.ResultVo
     */
    @GetMapping("search/{pageNum}/{pageSize}/{keyword}")
    public ResultVo search(@PathVariable("pageNum") Integer pageNum,
                           @PathVariable("pageSize") Integer pageSize,
                           @PathVariable("keyword") String keyword) {
        if (pageNum <= 1) {
            pageNum = 1;
        }

        if (pageSize <= 1) {
            pageSize = 1;
        }

        try {
            return ResultVoUtil.success(contentService.searchPage(keyword, pageNum, pageSize));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResultVoUtil.error(500, "error");
    }

    @GetMapping({"/", "index"})
    public ModelAndView index() {
        return new ModelAndView("index");
    }



    private ContentService contentService;

    @Autowired
    @Qualifier("contentService")
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}
