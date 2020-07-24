package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private ElasticsearchTemplate esTemplate;

    //设置每页查询数
    public final static Integer PAGE_SIZE = 20;

    @Override
    public Map search(Map<String, String> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();

        if (null != searchMap) {
            //0、组合查询对象
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            if (!StringUtils.isEmpty(searchMap.get("keywords"))) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("name", searchMap.get("keywords")).operator(Operator.AND));
            }
            //1、按照品牌进行过滤查询
            if (StringUtils.isNotEmpty(searchMap.get("brand"))) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
            }
            //2、按照规格进行过滤查询
            for (String key : searchMap.keySet()) {
                if (key.startsWith("spec_")) {
                    //spec_网络制式
                    boolQueryBuilder.filter(QueryBuilders.termQuery(("specMap." + key.substring(5) + ".keyword"), searchMap.get(key)));
                }
            }
            //3、按照价格进行区间过滤查询
            if (StringUtils.isNotEmpty(searchMap.get("price"))) {
                String[] prices = searchMap.get("price").split("-");
                // 0-500 500-1000
                if (prices.length == 2) {
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(prices[1]));
                }
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(prices[0]));
            }
            //4、搜索实现类
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

            //5:高亮
            HighlightBuilder.Field field = new HighlightBuilder
                    .Field("name")
                    .preTags("<span style='color:red'>")
                    .postTags("</span>");
            nativeSearchQueryBuilder.withHighlightFields(field);

            //6、按照品牌进行分组(聚合)查询
            String skuBrand = "skuBrand";
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuBrand).field("brandName"));

            //7、按照规格进行聚合查询
            String skuSpec = "skuSpec";
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuSpec).field("spec.keyword"));

            //8、 排序
            if (!StringUtils.isEmpty(searchMap.get("sortField"))) {
                if ("ASC".equals(searchMap.get("sortRule"))) {
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.ASC));
                } else {
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.DESC));
                }
            }

            //9、设置分页
            //第一个参数:当前页 是从0开始
            //第二个参数:每页显示多少条
            //开启分页查询
            String pageNum = searchMap.get("pageNum"); //当前页
            String pageSize = searchMap.get("pageSize"); //每页显示多少条
            if (StringUtils.isEmpty(pageNum)) {
                pageNum = "1";
            }
            if (StringUtils.isEmpty(pageSize)) {
                pageSize = "30";
            }
            nativeSearchQueryBuilder.withPageable(PageRequest.of(Integer.parseInt(pageNum) - 1, Integer.parseInt(pageSize)));

            //10、执行查询，返回结果对象
            AggregatedPage<SkuInfo> aggregatedPage = esTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class,
                    new SearchResultMapper() {
                        @Override
                        public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                            SearchHits hits = searchResponse.getHits();
                            ArrayList<T> list = new ArrayList<>();
                            if (null != hits) {
                                for (SearchHit hit : hits) {
                                    SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                                    if (highlightFields != null && highlightFields.size()>0){
                                        //替换数据
                                        skuInfo.setName(highlightFields.get("name").getFragments()[0].toString());
                                    }
                                    list.add((T) skuInfo);
                                }
                            }
                            return new AggregatedPageImpl<T>(list, pageable, hits.getTotalHits(), searchResponse.getAggregations());
                        }
                    });

            //11、总条数
            resultMap.put("total", aggregatedPage.getTotalElements());
            //12、总页数
            resultMap.put("totalPages", aggregatedPage.getTotalPages());
            //13、查询结果结合
            resultMap.put("rows", aggregatedPage.getContent());

            //14、封装品牌的分组结果
            StringTerms brandTerms = (StringTerms) aggregatedPage.getAggregation(skuBrand);
            List<String> brandList = brandTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("brandList", brandList);


            //15、封装规格分组结果
            StringTerms specTerms = (StringTerms) aggregatedPage.getAggregation(skuSpec);
            List<String> specList = specTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("specList", specList(specList));

            //16. 返回当前页
            resultMap.put("pageNum", pageNum);
            return resultMap;
        }
        return null;
    }

    //处理规格集合
    public Map<String, Set<String>> specList(List<String> specList) {
        Map<String, Set<String>> specMap = new HashMap<>();
        if (null != specList && specList.size() > 0) {
            for (String spec : specList) {
                Map<String, String> map = JSON.parseObject(spec, Map.class);
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    Set<String> specValues = specMap.get(key);
                    if (null == specValues) {
                        specValues = new HashSet<>();
                    }
                    specValues.add(value);
                    specMap.put(key, specValues);
                }
            }
        }
        return specMap;
    }

}
