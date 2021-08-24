package com.example.demo.es.service;

import com.alibaba.fastjson.JSON;
import com.example.demo.demos.resultvo.ResultVo;
import com.example.demo.demos.resultvo.ResultVoUtil;
import com.example.demo.es.constant.EsConstant;
import com.example.demo.es.pojo.Content;
import com.example.demo.es.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author YuanBo.Shi
 * @date 2021年08月18日 9:07 下午
 */
@Service
public class ContentService {


    private RestHighLevelClient client;

    private HtmlParseUtil htmlParseUtil;

    @Autowired
    @Qualifier("htmlParseUtil")
    public void setHtmlParseUtil(HtmlParseUtil htmlParseUtil) {
        this.htmlParseUtil = htmlParseUtil;
    }

    @Autowired
    @Qualifier("restHighLevelClient")
    public void setClient(RestHighLevelClient client) {
        this.client = client;
    }

    /**
     * 解析页面数据，放入至es中
     * @author YuanBo.Shi
     * @date 2021/8/24 12:39 下午
     * @param keyword 关键词
     * @return java.lang.Boolean
     */
    public ResultVo parseContent(String keyword) {
        List<Content> list = htmlParseUtil.parseHtml(keyword);

        if (list.isEmpty()) {
            return ResultVoUtil.error(1000, "执行批量插入失败");
        }

        BulkRequest request = new BulkRequest();
        request.timeout(TimeValue.timeValueMinutes(2));
        for (Content content : list) {
            request.add(
                    new IndexRequest(EsConstant.ES_INDEX)
                            .source(JSON.toJSONString(content), XContentType.JSON));
        }

        try {
            BulkResponse responses = client.bulk(request, RequestOptions.DEFAULT);
            if (!responses.hasFailures()) {
                return ResultVoUtil.success();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResultVoUtil.error(10001, "未抓取到相关数据");
    }

    public List<Map<String, Object>> searchPage(String keyword, Integer pageNum, Integer pageSize) throws IOException {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        if (pageNum <= 1) {
            pageNum = 1;
        }

        // 创建搜索请求
        SearchRequest searchRequest = new SearchRequest(EsConstant.ES_INDEX);

        // 创建构造器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 分页
        sourceBuilder.from(pageNum);
        sourceBuilder.size(pageSize);

        // 设置超时
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 创建搜索条件
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQueryBuilder);

        // 设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        highlightBuilder.requireFieldMatch(false);
        sourceBuilder.highlighter(highlightBuilder);

        // 执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        // 解析结果
        if (!response.isFragment()) {
            for (SearchHit hit : response.getHits().getHits()) {
                // 解析高亮的字段
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField highlightField = highlightFields.get("title");
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                if (highlightField != null) {
                    Text[] fragments = highlightField.getFragments();
                    String n_title = "";
                    for (Text text : fragments) {
                        n_title += text;
                    }
                    sourceAsMap.put("title", n_title);
                }
                list.add(sourceAsMap);
            }
        }
        return list;
    }
}
