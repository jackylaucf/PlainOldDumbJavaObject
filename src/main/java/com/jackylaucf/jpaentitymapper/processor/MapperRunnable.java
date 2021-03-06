package com.jackylaucf.jpaentitymapper.processor;

import com.jackylaucf.jpaentitymapper.config.ApplicationConfig;
import com.jackylaucf.jpaentitymapper.config.BeanConfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MapperRunnable implements Runnable{

    private Connection connection;
    private String tableName;
    private String beanName;
    private ApplicationConfig config;

    MapperRunnable(Connection connection, String tableName, String beanName){
        this.connection = connection;
        this.tableName = tableName;
        this.beanName = beanName;
        this.config = ApplicationConfig.getConfig();
    }

    @Override
    public void run() {
        try {
            System.out.println("Processing table " + tableName);
            final List<String> columnNames = new ArrayList<>();
            final List<Integer> columnTypes = new ArrayList<>();
            ResultSet meta = connection.getMetaData().getColumns(null, config.getDbConnectionSchema(), tableName, "%");
            while(meta.next()){
                columnNames.add(meta.getString("COLUMN_NAME"));
                columnTypes.add(meta.getInt("DATA_TYPE"));
            }
            if(columnNames.isEmpty()){
                throw new Exception("Table " + tableName + " is not found");
            }else{
                for(BeanConfig beanConfig : config.getBeanConfig()){
                    final String outputPath = beanConfig.getAbsolutePath();
                    beanConfig.getType().getBeanWriter().write(outputPath, tableName, beanName, columnNames, columnTypes, beanConfig);
                }
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.print("Error encountered: ");
            System.err.println(e.toString());
        }
    }

}
