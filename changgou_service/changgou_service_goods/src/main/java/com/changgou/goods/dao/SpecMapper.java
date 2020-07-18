package com.changgou.goods.dao;

import com.changgou.goods.pojo.Spec;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SpecMapper extends Mapper<Spec> {

    @Select("SELECT t1.name,t1.options FROM tb_spec t1 " +
            "INNER JOIN tb_template t2 ON t1.`template_id` = t2.`id`" +
            " INNER JOIN tb_category t3 ON t2.`id` = t3.`template_id`" +
            "WHERE t3.`name` = #{category} ")
    List<Map> findListByCategoryName(String category);
}
