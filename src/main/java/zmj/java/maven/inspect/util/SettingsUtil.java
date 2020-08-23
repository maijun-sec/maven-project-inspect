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
package zmj.java.maven.inspect.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import zmj.java.maven.inspect.exception.MavenProjectInspectException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * get maven settings message
 *
 * @author zhang maijun
 * @since 2020/08/23
 */
@Slf4j
public class SettingsUtil {
    /**
     * build maven settings
     *
     * @param settingsFile user settings file
     * @return maven settings message
     * @throws MavenProjectInspectException
     */
    public static Settings build(String settingsFile) throws MavenProjectInspectException {
        try {
            return new SettingsXpp3Reader().read(new FileReader(new File(settingsFile)));
        } catch (IOException e) {
            log.error("read maven settings failed, maybe not exist.");
            throw new MavenProjectInspectException("get user setting error");
        } catch (XmlPullParserException e) {
            log.error("read maven setting failed, maybe user settings file format error");
            log.error(e.getMessage());
            throw new MavenProjectInspectException("get user setting error");
        }
    }

    /**
     * default mirror, if not set, will use this mirror
     *
     * @return default mirror
     */
    public static Mirror defaultMirror() {
        Mirror mirror = new Mirror();
        mirror.setId("alimaven");
        mirror.setMirrorOf("default");
        mirror.setUrl("http://maven.aliyun.com/nexus/content/groups/public/");
        return mirror;
    }
}
