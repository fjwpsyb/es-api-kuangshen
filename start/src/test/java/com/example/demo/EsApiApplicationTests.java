package com.example.demo;

import com.alibaba.fastjson.JSON;
import com.example.demo.demos.web.Student;
import com.example.demo.es.constant.EsConstant;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.net.Authenticator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * elasticSearch API
 * @author YuanBo.Shi
 * @date 2021/8/16 8:50 下午

 */
@SpringBootTest
class EsApiApplicationTests {

    private RestHighLevelClient client;

    @Autowired
    @Qualifier("restHighLevelClient")
    public void setClient(RestHighLevelClient client) {
        this.client = client;
    }

    /**
     * 索引创建
     * @author YuanBo.Shi
     * @date 2021/8/16 8:50 下午
     */
    @Test
    public void testIndexCreate() throws IOException {
        // 创建索引请求
        CreateIndexRequest request = new CreateIndexRequest(EsConstant.ES_INDEX);
        // 执行请求
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println("response = " + response.isFragment());
    }

    /**
     * 获取索引
     * @author YuanBo.Shi
     * @date 2021/8/16 9:00 下午
     */
    @Test
    public void testQueryIndex() throws IOException {
        // 创建获取索引请求
        GetIndexRequest request = new GetIndexRequest(EsConstant.ES_INDEX);
        if (client.indices().exists(request, RequestOptions.DEFAULT)) {
            System.out.println("true = " + true);
        }
    }

    /**
     * 删除索引
     * @author YuanBo.Shi
     * @date 2021/8/16 9:00 下午
     */
    @Test
    public void testDeleteIndex() throws IOException {
        // 创建删除索引请求
        DeleteIndexRequest request = new DeleteIndexRequest(EsConstant.ES_INDEX);
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }

    /**
     * 测试添加文档
     * @author YuanBo.Shi
     * @date 2021/8/16 9:07 下午
     */
    @Test
    public void testAddStudent() throws IOException {
        // 创建对象
        Student student = new Student("syb", 18);

        // 创建请求
        IndexRequest request = new IndexRequest("syb_index");
        request.id("3")
                .timeout(TimeValue.timeValueMinutes(1));

        // 数据放入请求
        request.source(JSON.toJSONString(student), XContentType.JSON);

        // 客户端发送请求
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
        System.out.println(response.getResult());
    }

    /**
     * 获取文档
     * @author YuanBo.Shi
     * @date 2021/8/16 9:19 下午
     */
    @Test
    public void testGetStudent() throws IOException {
        GetRequest request = new GetRequest("syb_index", "1");
        // 不获取上下文 _source 类似与MP queryWrapper select
        // request.fetchSourceContext(new FetchSourceContext(false));
        // request.storedFields("_none_");

        // 判断文档是否存在
        boolean exists = client.exists(request, RequestOptions.DEFAULT);
        System.out.println("exists = " + exists);

        //获取文档信息
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        Map<String, Object> sourceAsMap = response.getSourceAsMap();
        System.out.println(sourceAsMap);
    }

    /**
     * 更新文档信息
     * @author YuanBo.Shi
     * @date 2021/8/17 12:26 下午
     */
    @Test
    public void testUpdateDocument() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("syb_index", "1");
        // 设置超时时间
        updateRequest.timeout(TimeValue.timeValueMillis(1));

        Student student = new Student("石远波", 27);
        updateRequest.doc(JSON.toJSONString(student), XContentType.JSON);
        UpdateResponse response = client.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(response.getResult());
    }

    /**
     * 删除文档信息
     * @author YuanBo.Shi
     * @date 2021/8/17 12:42 下午
     */
    @Test
    public void testDeleteDocument() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("syb_index", "1");
        deleteRequest.timeout(TimeValue.timeValueMillis(1));

        DeleteResponse response = client.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(response.getResult());
        System.out.println(response.status());
    }

    /**
     * 批量插入数据
     * @author YuanBo.Shi
     * @date 2021/8/17 12:42 下午
     */
    @Test
    public void testBulkRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout(TimeValue.timeValueMillis(1));

        List<Student> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Student student = new Student("syb" + i, i);
            list.add(student);
        }

        // 批处理请求
        for (int i = 0; i < list.size(); i++) {
            bulkRequest.add(
                    new IndexRequest("syb_index")
                    // 不写id的 则使用默认id
                    .id("" + (i + 1))
                    .source(JSON.toJSONString(list.get(i)), XContentType.JSON)
            );
        }

        BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(response.hasFailures());
        System.out.println(response.status());
    }

    /**
     * 查询文档
     * @author YuanBo.Shi
     * @date 2021/8/17 8:47 下午
     */
    @Test
    public void testSearchDocument() throws IOException {
        // 创建查询请求
        SearchRequest searchRequest = new SearchRequest("syb_index");

        // 构建搜索条件(构造器)
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 查询条件
        // TermsQueryBuilder 精准查询
        TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("name", "syb0");

        // MatchAllQueryBuilder 匹配所有
        // MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        sourceBuilder.query(termsQueryBuilder)
                     .timeout(TimeValue.timeValueMillis(1));


        searchRequest.source(sourceBuilder);

        // 执行搜索
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(response.getHits().getTotalHits());
    }

}
