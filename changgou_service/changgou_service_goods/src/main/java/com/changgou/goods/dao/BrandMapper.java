package com.changgou.goods.dao;

import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface BrandMapper extends Mapper<Brand> {

    @Select("SELECT t2.`name` FROM tb_category_brand t1 " +
            "INNER JOIN tb_brand t2 ON t1.`brand_id` = t2.`id` " +
            "INNER JOIN tb_category t3 ON t3.`id` = t1.`category_id` " +
            "WHERE t3.`name` = #{category} ")
    List<String> findListByCategoryName(String category);
}
