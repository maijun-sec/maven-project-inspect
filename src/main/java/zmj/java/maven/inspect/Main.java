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
package zmj.java.maven.inspect;

import hudson.maven.MavenEmbedderException;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import zmj.java.maven.inspect.util.MavenProjectUtil;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws ProjectBuildingException, MavenEmbedderException {
        String pomFile = "D:\\workspace\\idea\\test\\test-abc\\pom.xml";
        String localRepo = "D:\\workspace\\maven_repo";
        String m2Home = "C:\\development\\apache-maven-3.6.1";
        File mavenHome = new File(m2Home);

        MavenProject project = MavenProjectUtil.getMavenProject(pomFile, localRepo, null);
        List<MavenProject> projects = MavenProjectUtil.getAllSubProjects(project, localRepo);
        System.out.println(projects.size());

        for (MavenProject project1 : projects) {
            System.out.println("===========> begin");
            System.out.println(project1.getGroupId() + " <==> " + project1.getArtifactId() + " <==> " + project1.getVersion());
            System.out.println("is a pom maven project: " + MavenProjectUtil.isParent(project1));

            if (!MavenProjectUtil.isParent(project1)) {
                System.out.println("====> source " + MavenProjectUtil.getSourcePaths(project1).stream().collect(Collectors.joining(",")));

                Build build = project1.getBuild();

                List<Dependency> dependencies = project1.getDependencies();
                for (Dependency dependency : dependencies) {
                    System.out.println("====> dependency");
                    System.out.println(dependency.getGroupId() + " <==> " + dependency.getArtifactId() + " <==> " + dependency.getVersion());
                }
            }
        }

        // PlexusContainer container = MavenEmbedderUtils.buildPlexusContainer(mavenHome, mavenRequest);
    }
}
