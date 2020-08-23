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
package zmj.java.maven.inspect.constant;

/**
 * all constant variables in the program
 *
 * @author zhang maijun
 * @since 2020/08/23
 */
public class Constants {
    /**
     * project name, now used to print help message
     */
    public static final String PROJECT_NAME = "maven-project-inspect";

    /**
     * maven home name set in the system profile
     */
    public static final String SYSTEM_PARAM_MAVEN_HOME = "M2_HOME";

    /**
     * program option, used to print help message
     */
    public static final String OPTION_HELP = "help";

    /**
     * short option for help
     */
    public static final String OPTION_HELP_SHORT = "h";

    /**
     * program option, used to set maven home(as M2_HOME in the system profile)
     */
    public static final String OPTION_MAVEV_HOME = "maven-home";

    /**
     * short option for maven-home
     */
    public static final String OPTION_MAVEV_HOME_SHORT = "mh";

    /**
     * program option, used to set user settings file
     * all messages about maven will be got here, local repository, remote repository
     * (maybe proxy will need in the future)
     */
    public static final String OPTION_USER_SETTINGS = "user-settings-file";

    /**
     * short option for user-settings-file
     */
    public static final String OPTION_USER_SETTINGS_SHORT = "usf";

    /**
     * program option, used to set project pom.xml
     */
    public static final String OPTION_MAVEN_SCRIPT = "maven-script";

    /**
     * short option for maven-script
     */
    public static final String OPTION_MAVEN_SCRIPT_SHORT = "ms";
}
