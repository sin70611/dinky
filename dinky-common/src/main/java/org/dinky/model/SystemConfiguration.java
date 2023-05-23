/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.dinky.model;

import org.dinky.assertion.Asserts;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

/**
 * SystemConfiguration
 *
 * @since 2021/11/18
 */
public class SystemConfiguration {

    private static volatile SystemConfiguration systemConfiguration = new SystemConfiguration();

    public static SystemConfiguration getInstances() {
        return systemConfiguration;
    }

    private static final List<Configuration> CONFIGURATION_LIST =
            Lists.newArrayList(
                    systemConfiguration.useRestAPI,
                    systemConfiguration.sqlSeparator,
                    systemConfiguration.jobIdWait,
                    systemConfiguration.mavenSettings,
                    systemConfiguration.mavenRepository,
                    systemConfiguration.mavenRepositoryUser,
                    systemConfiguration.mavenRepositoryPassword);

    private Configuration useRestAPI =
            new Configuration(
                    "useRestAPI",
                    "使用 RestAPI",
                    ValueType.BOOLEAN,
                    true,
                    "在运维 Flink 任务时是否使用 RestAPI");
    private Configuration sqlSeparator =
            new Configuration(
                    "sqlSeparator", "FlinkSQL语句分割符", ValueType.STRING, ";\n", "Flink SQL 的语句分割符");
    private Configuration jobIdWait =
            new Configuration(
                    "jobIdWait",
                    "获取 Job ID 的最大等待时间（秒）",
                    ValueType.INT,
                    30,
                    "提交 Application 或 PerJob 任务时获取 Job ID 的最大等待时间（秒）");

    private Configuration mavenSettings =
            new Configuration(
                    "mavenSettings",
                    "Maven Settings 文件路径",
                    ValueType.STRING,
                    "",
                    "Maven Settings File Path");

    private Configuration mavenRepository =
            new Configuration(
                    "mavenRepository",
                    "Maven Central Repository",
                    ValueType.STRING,
                    "https://maven.aliyun.com/nexus/content/repositories/central",
                    "Maven private server address");

    private Configuration mavenRepositoryUser =
            new Configuration(
                    "mavenRepositoryUser",
                    "Maven Central Repository Auth User",
                    ValueType.STRING,
                    "",
                    "Maven private server authentication username");

    private Configuration mavenRepositoryPassword =
            new Configuration(
                    "mavenRepositoryPassword",
                    "Maven Central Repository Auth Password",
                    ValueType.STRING,
                    "",
                    "Maven private server authentication password");

    public void setConfiguration(JsonNode jsonNode) {
        CONFIGURATION_LIST.stream()
                .filter(t -> jsonNode.has(t.getName()))
                .forEach(
                        item -> {
                            final JsonNode value = jsonNode.get(item.getName());
                            switch (item.getType()) {
                                case BOOLEAN:
                                    item.setValue(value.asBoolean());
                                    break;
                                case INT:
                                    item.setValue(value.asInt());
                                    break;
                                default:
                                    item.setValue(value.asText());
                            }
                        });
    }

    public void addConfiguration(Map<String, Object> map) {
        for (Configuration item : CONFIGURATION_LIST) {
            final String name = item.getName();
            if (!map.containsKey(name)) {
                map.put(name, item.getValue());
                continue;
            }

            if (item.getType().equals(ValueType.BOOLEAN)) {
                map.put(name, Asserts.isEqualsIgnoreCase("true", map.get(name).toString()));
            }
        }
    }

    public boolean isUseRestAPI() {
        return (boolean) useRestAPI.getValue();
    }

    public void setUseRestAPI(boolean useRestAPI) {
        this.useRestAPI.setValue(useRestAPI);
    }

    public String getSqlSeparator() {
        return sqlSeparator.getValue().toString();
    }

    public void setSqlSeparator(String sqlSeparator) {
        this.sqlSeparator.setValue(sqlSeparator);
    }

    public int getJobIdWait() {
        return (int) jobIdWait.getValue();
    }

    public void setJobIdWait(Configuration jobIdWait) {
        this.jobIdWait.setValue(jobIdWait);
    }

    public String getMavenSettings() {
        return (String) mavenSettings.getValue();
    }

    public String getMavenRepository() {
        return (String) mavenRepository.getValue();
    }

    public String getMavenRepositoryUser() {
        return (String) mavenRepositoryUser.getValue();
    }

    public String getMavenRepositoryPassword() {
        return (String) mavenRepositoryPassword.getValue();
    }

    enum ValueType {
        STRING,
        INT,
        DOUBLE,
        FLOAT,
        BOOLEAN,
        DATE
    }

    public class Configuration {

        private final String label;
        private final Object defaultValue;
        private final String note;

        private String name;
        private ValueType type;
        private Object value;

        public Configuration(
                String name, String label, ValueType type, Object defaultValue, String note) {
            this.name = name;
            this.label = label;
            this.type = type;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
            this.note = note;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        public ValueType getType() {
            return type;
        }

        public String getName() {
            return name;
        }
    }
}