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
package zmj.java.maven.inspect.handler;

import hudson.maven.MavenEmbedder;
import hudson.maven.MavenEmbedderException;
import hudson.maven.MavenRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Settings;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import zmj.java.maven.inspect.bean.DependencyBean;
import zmj.java.maven.inspect.bean.JavaOptionBean;
import zmj.java.maven.inspect.bean.RemoteRepositoryMessageBean;
import zmj.java.maven.inspect.constant.Constants;
import zmj.java.maven.inspect.exception.MavenProjectInspectException;
import zmj.java.maven.inspect.util.DependencyUtil;
import zmj.java.maven.inspect.util.MavenProjectUtil;
import zmj.java.maven.inspect.util.SettingsUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * handle arguments, and get the java compile options
 *
 * @author zhang maijun
 * @since 2020/08/23
 */
@Slf4j
public class JavaProjectOptionHandler {
    public static void handle(Map<String, String> arguments) throws MavenProjectInspectException {
        // maven pom file
        String pomFile = arguments.get(Constants.OPTION_MAVEN_SCRIPT);

        // maven home
        File mavenHome = new File(arguments.get(Constants.OPTION_MAVEV_HOME));

        // maven settings message
        Settings settings = SettingsUtil.build(arguments.get(Constants.OPTION_USER_SETTINGS));

        // local repository
        String localRepo = settings.getLocalRepository();

        // remote maven repository, used to handle dependency
        List<Mirror> mirrors = settings.getMirrors();
        Mirror mirror = SettingsUtil.defaultMirror();
        if (CollectionUtils.isNotEmpty(mirrors)) {
            mirror = mirrors.get(0);
        }

        List<ProjectBuildingResult> projectBuildingResults = getProjectBuildingResults(pomFile, mavenHome, localRepo);

        List<JavaOptionBean> javaOptionBeans = handleProjectBuildingResults(localRepo, projectBuildingResults, mirror);

        javaOptionBeans.forEach(System.out::println);
    }

    private static List<JavaOptionBean> handleProjectBuildingResults(String localRepo,
                                                                     List<ProjectBuildingResult> projectBuildingResults, Mirror mirror) {
        List<JavaOptionBean> javaOptionBeans = new ArrayList<>();

        log.info("there are {} maven modules to be handled, including pom module", projectBuildingResults.size());
        for (ProjectBuildingResult result : projectBuildingResults) {
            MavenProject project = result.getProject();
            String projectId = result.getProjectId();

            if (MavenProjectUtil.isParent(project)) {
                log.info("maven module {} is a parent module, there is no source code here, will skip.", projectId);
                continue;
            }

            JavaOptionBean javaOptionBean = constructJavaOptionBean(localRepo, project, projectId, mirror);
            javaOptionBeans.add(javaOptionBean);
        }

        return javaOptionBeans;
    }

    private static JavaOptionBean constructJavaOptionBean(String localRepo, MavenProject project, String projectId,
                                                          Mirror mirror) {
        log.info("handling maven module {}", projectId);
        JavaOptionBean optionBean = new JavaOptionBean();

        // set project id
        optionBean.setProjectId(projectId);

        // set source file path
        optionBean.setSourcePaths(MavenProjectUtil.getSourcePaths(project));

        Build build = project.getBuild();
        // set test source file path
        optionBean.setTestSourcePath(build.getTestSourceDirectory());

        // set source output directory
        optionBean.setOutputPath(build.getOutputDirectory());

        // set test output directory
        optionBean.setTestOutputPath(build.getTestOutputDirectory());

        // TODO get source java version(for java, not very necessary, high version is compatible with low version)
        optionBean.setSource("1.8");

        // TODO get encoding of the project, now we set UTF-8 for all
        optionBean.setEncoding("UTF-8");

        // set dependencies
        Set<DependencyBean> dependencyBeans = new HashSet<>();

        List<Dependency> dependencies = project.getDependencies();
        for (Dependency dependency : dependencies) {
            String scope = dependency.getScope();
            String dependencyId =
                    dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion();
            RemoteRepositoryMessageBean remoteRepositoryMessageBean = new RemoteRepositoryMessageBean(mirror.getId(),
                    mirror.getMirrorOf(), mirror.getUrl());
            try {
                DependencyResult dependencyResult =
                        DependencyUtil.getDependencies(remoteRepositoryMessageBean,
                                dependencyId, localRepo, scope);
                final List<ArtifactResult> artifactResults = dependencyResult.getArtifactResults();
                for (final ArtifactResult artifactResult : artifactResults) {
                    DependencyBean bean = getDependencyBean(artifactResult, scope);
                    dependencyBeans.add(bean);
                }
            } catch (DependencyResolutionException e) {
                log.warn("exception occur when handle dependency of {}, need further check!", projectId);
            }
        }
        optionBean.setDependencyBeans(dependencyBeans);

        return optionBean;
    }

    private static DependencyBean getDependencyBean(ArtifactResult artifactResult, String scope) {
        DependencyBean bean = new DependencyBean();
        Artifact artifact = artifactResult.getArtifact();
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();
        bean.setGroupId(groupId);
        bean.setArtifactId(artifactId);
        bean.setVersion(version);
        bean.setScope(scope);
        try {
            if (artifact.getFile() != null) {
                bean.setFilePath(artifact.getFile().getCanonicalPath());
            }
        } catch (IOException e) {
            log.warn("can't get local file path for dependency {}:{}:{}, error message: {}", groupId, artifactId,
                    version, e.getMessage());
        }

        return bean;
    }

    private static List<ProjectBuildingResult> getProjectBuildingResults(String pomFile, File mavenHome,
                                                                         String localRepo) throws MavenProjectInspectException {
        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setPom(pomFile);
        mavenRequest.setLocalRepositoryPath(localRepo);

        try {
            MavenEmbedder mavenEmbedder = new MavenEmbedder(mavenHome, mavenRequest);
            return mavenEmbedder.buildProjects(new File(pomFile), true);
        } catch (MavenEmbedderException | ProjectBuildingException e) {
            log.error("construct MavenProject from pom.xml error, error message: {}", e.getMessage());
            throw new MavenProjectInspectException("construct MavenProject error");
        }

    }
}
