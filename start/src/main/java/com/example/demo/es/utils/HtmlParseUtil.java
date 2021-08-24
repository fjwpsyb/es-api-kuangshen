package com.example.demo.es.utils;

import com.example.demo.es.pojo.Content;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YuanBo.Shi
 * @date 2021年08月18日 7:30 下午
 */
@Component
@Slf4j
public class HtmlParseUtil {

    public List<Content> parseHtml(String keyword) {
        List<Content> list = new ArrayList<>();
        String url = "https://search.jd.com/Search?keyword=" + keyword;
        Document document = null;
        try {
            document = Jsoup.parse(new URL(url), 3000);
            Element element = document.getElementById("J_goodsList");
            if (element != null) {
                Elements elements = element.getElementsByTag("li");
                for (Element el : elements) {
                    String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
                    String price = el.getElementsByClass("p-price").eq(0).text();
                    String title = el.getElementsByClass("p-name").eq(0).text();
                    String shopName = el.getElementsByClass("p-shop").eq(0).text();
                    Content content = Content.builder()
                            .title(title)
                            .img(img)
                            .price(price)
                            .shopName(shopName)
                            .build();
                    list.add(content);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
