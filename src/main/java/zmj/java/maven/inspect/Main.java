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

import hudson.maven.MavenEmbedder;
import hudson.maven.MavenEmbedderException;
import hudson.maven.MavenRequest;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingResult;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import zmj.java.maven.inspect.bean.RemoteRepositoryMessageBean;
import zmj.java.maven.inspect.util.DependencyUtil;
import zmj.java.maven.inspect.util.MavenProjectUtil;

import java.io.File;
import java.util.List;

/**
 * entrance of this program
 *
 * @author zhang maijun
 * @since 2020/08/22
 */
public class Main {
    public static void main(String[] args) throws MavenEmbedderException, ProjectBuildingException,
            ArtifactDescriptorException {
        String pomFile = "D:\\workspace\\idea\\test\\test-abc\\pom.xml";
        String localRepo = "D:\\workspace\\maven_repo";
        File mavenHome = new File("C:\\development\\apache-maven-3.6.1");

        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setPom(pomFile);
        mavenRequest.setLocalRepositoryPath(localRepo);

        MavenEmbedder mavenEmbedder = new MavenEmbedder(mavenHome, mavenRequest);
        List<ProjectBuildingResult> projectBuildingResults = mavenEmbedder.buildProjects(new File(pomFile), true);

        System.out.println(projectBuildingResults.size());
        for (ProjectBuildingResult result : projectBuildingResults) {
            System.out.println("==========================> begin");
            MavenProject project = result.getProject();
            System.out.println(project.getGroupId());
            System.out.println(project.getArtifactId());
            System.out.println(project.getVersion());
            System.out.println(MavenProjectUtil.isParent(project));
            System.out.println(result.getProjectId());

            if (!MavenProjectUtil.isParent(project)) {
                List<Dependency> dependencies = project.getDependencies();
                for (Dependency dependency : dependencies) {
                    String projectId =
                            dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion();
                    System.out.println("====> dependency: " + projectId);
                    RemoteRepositoryMessageBean remoteRepositoryMessageBean = new RemoteRepositoryMessageBean(
                            "alimaven", "default", "http://maven.aliyun.com/nexus/content/groups/public/");
                    ArtifactDescriptorResult artifactDescriptorResult =
                            DependencyUtil.getDependencies(remoteRepositoryMessageBean, projectId, localRepo);
                    for (org.eclipse.aether.graph.Dependency dependency1 : artifactDescriptorResult.getDependencies()) {
                        System.out.println(dependency1);
                    }
                }
            }
        }
    }
}
