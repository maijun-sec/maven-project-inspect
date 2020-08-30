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
package zmj.java.maven.inspect.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * if a module depends on other modules in the project,
 * will not try to get the dependency from local repository,
 * use the module compile target instead.
 *
 * @author zhang maijun
 * @since 2020/08/30
 */
public class ModuleTargetCache {
    private static Map<String, String> projectIdAndTargetPathMap = new HashMap<>();

    /**
     * insert message
     *
     * @param projectId  used to identify a maven module
     * @param targetPath module target path
     */
    public static void insert(String projectId, String targetPath) {
        projectIdAndTargetPathMap.put(projectId, targetPath);
    }

    /**
     * get target path by projectId
     *
     * @param projectId used to identify a maven module
     * @return module target path
     */
    public static String getTargetByProjectId(String projectId) {
        return projectIdAndTargetPathMap.get(projectId);
    }

    /**
     * check if the module identified by projectId is a module of this project
     *
     * @param projectId used to identify a maven module
     * @return is a module?
     */
    public static boolean isModule(String projectId) {
        return projectIdAndTargetPathMap.keySet().contains(projectId);
    }
}
