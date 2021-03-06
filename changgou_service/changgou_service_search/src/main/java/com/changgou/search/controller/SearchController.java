package com.changgou.search.controller;

import com.changgou.entity.Page;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    //搜索页面 http://localhost:9009/search/list?keywords=手机&brand=三星&spec_颜色=粉色&
    //入参：Map
    //返回值 Map
    //由于页面是thymeleaf 完成的 属于服务器内页面渲染 跳转页面
    @GetMapping("/list")
    public String search(@RequestParam Map<String, String> searchMap, Model model) {
        Map<String, Object> searchResult = searchService.search(searchMap);
        model.addAttribute("searchMap", searchMap);
        model.addAttribute("result", searchResult);
        //拼装url
        StringBuilder url = new StringBuilder("/search/list");
        if (searchMap != null && searchMap.size() > 0) {
            //是由查询条件
            url.append("?");
            for (String paramKey : searchMap.keySet()) {
                if (!"sortRule".equals(paramKey) && !"sortField".equals(paramKey) && !"pageNum".equals(paramKey)) {
                    url.append(paramKey).append("=").append(searchMap.get(paramKey)).append("&");
                }
            }
            //http://localhost:9009/search/list?keywords=手机&spec_网络制式=4G&
            String urlString = url.toString();
            //去除路径上的最后一个&
            urlString = urlString.substring(0, urlString.length() - 1);
            model.addAttribute("url", urlString);
        } else {
            model.addAttribute("url", url);
        }

        Page<SkuInfo> page = new Page<SkuInfo>(Long.parseLong(searchResult.get("totalPages").toString()),
                Integer.parseInt(searchResult.get("pageNum").toString()),
                Page.pageSize);
        model.addAttribute("page", page);
        return "search";
    }

    //特殊符号处理
    private void handleSearchMap(Map<String, String> searchMap) {
        Set<Map.Entry<String, String>> entries = searchMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            if (entry.getKey().startsWith("spec_")) {
                searchMap.put(entry.getKey(), entry.getValue().replace("+", "%2B"));
            }
        }
    }
}
