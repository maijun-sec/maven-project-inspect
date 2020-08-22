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

import hudson.maven.MavenEmbedder;
import hudson.maven.MavenEmbedderException;
import hudson.maven.MavenRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * operations about MavenProject
 *
 * @author zhang maijun
 * @since 2020/08/22
 */
public class MavenProjectUtil {
    /**
     * check if a MavenProject is a parent module.
     *
     * @param project MavenProject
     * @return if a MavenProject is a parent module
     */
    public static boolean isParent(MavenProject project) {
        return "pom".equals(project.getPackaging());
    }

    /**
     * insert all maven modules to a list, contains the sub module of this project
     *
     * @param project   MavenProject
     * @param localRepo the local maven repository
     * @return List of all maven modules
     */
    public static List<MavenProject> getAllSubProjects(MavenProject project, String localRepo) {
        List<MavenProject> projects = new ArrayList<>();
        addAllSubProjectsToList(projects, project, localRepo);
        return projects;
    }

    private static void addAllSubProjectsToList(List<MavenProject> projects, MavenProject project, String localRepo) {
        projects.add(project);

        if (!isParent(project)) {
            return;
        }

        List<MavenProject> subProjects = getSubProjects(project, localRepo);
        if (CollectionUtils.isNotEmpty(subProjects)) {
            for (MavenProject subProject : subProjects) {
                addAllSubProjectsToList(projects, subProject, localRepo);
            }
        }
    }

    /**
     * get all sub modules of current MavenProject
     *
     * @param project   MavenProject
     * @param localRepo the local maven repository
     * @return all sub modules of current MavenProject
     */
    public static List<MavenProject> getSubProjects(MavenProject project, String localRepo) {
        List<MavenProject> projects = new ArrayList<>();

        File baseDir = project.getBasedir();
        List<String> modules = project.getModules();
        for (String module : modules) {
            File subDir = new File(baseDir, module);
            File subPom = new File(subDir, "pom.xml");
            if (subPom.exists() && subPom.isFile()) {
                try {
                    String pomPath = subPom.getCanonicalPath();
                    projects.add(getMavenProject(pomPath, localRepo, project));
                } catch (IOException e) {
                    // TODO warn
                } catch (ProjectBuildingException e) {
                    // TODO warn
                } catch (MavenEmbedderException e) {
                    // TODO warn
                }
            } else {
                // TODO warn
            }
        }

        return projects;
    }

    /**
     * get MavenProject from pom file and local maven repository
     *
     * @param pomFile   pom file
     * @param localRepo the local maven repository
     * @param parent    parent module of current maven project(can be null)
     * @return MavenProject
     * @throws MavenEmbedderException
     * @throws ProjectBuildingException
     */
    public static MavenProject getMavenProject(String pomFile, String localRepo, MavenProject parent) throws MavenEmbedderException, ProjectBuildingException {
        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setPom(pomFile);
        mavenRequest.setLocalRepositoryPath(localRepo);

        MavenEmbedder mavenEmbedder = new MavenEmbedder(Thread.currentThread().getContextClassLoader(), mavenRequest);

        MavenProject project = mavenEmbedder.readProject(new File(pomFile));

        if (parent != null) {
            project.setParent(parent);
        }

        return project;
    }
}
