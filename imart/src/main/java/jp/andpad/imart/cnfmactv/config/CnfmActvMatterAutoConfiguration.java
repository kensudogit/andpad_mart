package jp.andpad.imart.cnfmactv.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import jp.andpad.imart.cnfmactv.IntraMartCnfmActvMatterProperties;

@AutoConfiguration
@EnableConfigurationProperties(IntraMartCnfmActvMatterProperties.class)
@ComponentScan(basePackages = "jp.andpad.imart.cnfmactv")
public class CnfmActvMatterAutoConfiguration {}
