/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package zmj.java.maven.inspect.bean;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * java file compile options
 *
 * @author zhang maijun
 * @since 2020/08/22
 */
@Data
@ToString
public class JavaOptionBean {
    /**
     * jdk version of this project
     */
    private String source;

    /**
     * encoding of this project, default is "UTF-8"
     */
    private String encoding;

    /**
     * all source paths of this project,
     * mainly we will get sourceDirectory under build tag
     * if there is org.codehaus.mojo/build-helper-maven-plugin plugin, there will be multiple source paths.
     */
    private List<String> sourcePaths;

    /**
     * test source path
     */
    private String testSourcePaths;

    /**
     * directory that the source file will be compiled to
     */
    private String outputPath;

    /**
     * directory that the test source file will be compiled to
     */
    private String testOutputPath;

    /**
     * dependencies of this project
     */
    private List<DependencyBean> dependencyBeans;
}
