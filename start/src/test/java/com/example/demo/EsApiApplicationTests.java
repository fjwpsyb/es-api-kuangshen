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
 * @date 2021/8/16 8:50 ??????

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
     * ????????????
     * @author YuanBo.Shi
     * @date 2021/8/16 8:50 ??????
     */
    @Test
    public void testIndexCreate() throws IOException {
        // ??????????????????
        CreateIndexRequest request = new CreateIndexRequest(EsConstant.ES_INDEX);
        // ????????????
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println("response = " + response.isFragment());
    }

    /**
     * ????????????
     * @author YuanBo.Shi
     * @date 2021/8/16 9:00 ??????
     */
    @Test
    public void testQueryIndex() throws IOException {
        // ????????????????????????
        GetIndexRequest request = new GetIndexRequest(EsConstant.ES_INDEX);
        if (client.indices().exists(request, RequestOptions.DEFAULT)) {
            System.out.println("true = " + true);
        }
    }

    /**
     * ????????????
     * @author YuanBo.Shi
     * @date 2021/8/16 9:00 ??????
     */
    @Test
    public void testDeleteIndex() throws IOException {
        // ????????????????????????
        DeleteIndexRequest request = new DeleteIndexRequest(EsConstant.ES_INDEX);
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }

    /**
     * ??????????????????
     * @author YuanBo.Shi
     * @date 2021/8/16 9:07 ??????
     */
    @Test
    public void testAddStudent() throws IOException {
        // ????????????
        Student student = new Student("syb", 18);

        // ????????????
        IndexRequest request = new IndexRequest("syb_index");
        request.id("3")
                .timeout(TimeValue.timeValueMinutes(1));

        // ??????????????????
        request.source(JSON.toJSONString(student), XContentType.JSON);

        // ?????????????????????
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
        System.out.println(response.getResult());
    }

    /**
     * ????????????
     * @author YuanBo.Shi
     * @date 2021/8/16 9:19 ??????
     */
    @Test
    public void testGetStudent() throws IOException {
        GetRequest request = new GetRequest("syb_index", "1");
        // ?????????????????? _source ?????????MP queryWrapper select
        // request.fetchSourceContext(new FetchSourceContext(false));
        // request.storedFields("_none_");

        // ????????????????????????
        boolean exists = client.exists(request, RequestOptions.DEFAULT);
        System.out.println("exists = " + exists);

        //??????????????????
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        Map<String, Object> sourceAsMap = response.getSourceAsMap();
        System.out.println(sourceAsMap);
    }

    /**
     * ??????????????????
     * @author YuanBo.Shi
     * @date 2021/8/17 12:26 ??????
     */
    @Test
    public void testUpdateDocument() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("syb_index", "1");
        // ??????????????????
        updateRequest.timeout(TimeValue.timeValueMillis(1));

        Student student = new Student("?????????", 27);
        updateRequest.doc(JSON.toJSONString(student), XContentType.JSON);
        UpdateResponse response = client.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(response.getResult());
    }

    /**
     * ??????????????????
     * @author YuanBo.Shi
     * @date 2021/8/17 12:42 ??????
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
     * ??????????????????
     * @author YuanBo.Shi
     * @date 2021/8/17 12:42 ??????
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

        // ???????????????
        for (int i = 0; i < list.size(); i++) {
            bulkRequest.add(
                    new IndexRequest("syb_index")
                    // ??????id??? ???????????????id
                    .id("" + (i + 1))
                    .source(JSON.toJSONString(list.get(i)), XContentType.JSON)
            );
        }

        BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(response.hasFailures());
        System.out.println(response.status());
    }

    /**
     * ????????????
     * @author YuanBo.Shi
     * @date 2021/8/17 8:47 ??????
     */
    @Test
    public void testSearchDocument() throws IOException {
        // ??????????????????
        SearchRequest searchRequest = new SearchRequest("syb_index");

        // ??????????????????(?????????)
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // ????????????
        // TermsQueryBuilder ????????????
        TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("name", "syb0");

        // MatchAllQueryBuilder ????????????
        // MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        sourceBuilder.query(termsQueryBuilder)
                     .timeout(TimeValue.timeValueMillis(1));


        searchRequest.source(sourceBuilder);

        // ????????????
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(response.getHits().getTotalHits());
    }

}
