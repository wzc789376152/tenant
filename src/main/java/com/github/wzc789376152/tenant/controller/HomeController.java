package com.github.wzc789376152.tenant.controller;

import com.github.wzc789376152.tenant.context.DynamicDataSourceContextHolder;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
public class HomeController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @RequestMapping("/register")
    public boolean index(String name) {
        String sql = "insert into tenant(name) values(?)";
        jdbcTemplate.update(sql, name);
        return true;
    }

    @RequestMapping("init")
    public boolean initData(Integer id) {
        try {
            String sql = "create database tenant" + id;
            jdbcTemplate.execute(sql);

            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setJdbcUrl("jdbc:mysql://www.yangmh.top:3306/tenant" + id);
            dataSource.setUsername("root");
            dataSource.setPassword("wzc@789376152");
            //切换数据源
            DynamicDataSourceContextHolder.setDataSourceKey(id.toString());
            File file = ResourceUtils.getFile("classpath:user.sql");
            Connection conn = dataSource.getConnection();
            FileSystemResource rc = new FileSystemResource(file.getPath());
            //EncodedResource er = new EncodedResource(rc, "GBK");
            ScriptUtils.executeSqlScript(conn, rc);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    @RequestMapping("testInsert")
    public boolean testInsert(Integer id) {
        String sql = "insert into user(id) values(?)";
        jdbcTemplate.update(sql, id);
        return true;
    }

}
