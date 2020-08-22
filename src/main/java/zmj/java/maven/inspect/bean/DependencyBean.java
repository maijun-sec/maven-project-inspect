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

/**
 * maven dependency message
 *
 * @author zhang maijun
 * @since 2020/08/22
 */
@Data
@ToString
public class DependencyBean {
    /**
     * group id of the dependency
     */
    private String groupId;

    /**
     * artifact id of the dependency
     */
    private String artifactId;

    /**
     * version of the dependency
     */
    private String version;

    /**
     * file path of the dependency in the local maven repository
     */
    private String filePath;
}
