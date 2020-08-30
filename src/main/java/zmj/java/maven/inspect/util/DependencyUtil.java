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

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import zmj.java.maven.inspect.bean.RemoteRepositoryMessageBean;

import java.util.Collections;

/**
 * get transitive dependency information of the exact dependency
 * reference: https://stackoverflow.com/questions/39638138/find-all-direct-dependencies-of-an-artifact-on-maven-central
 *
 * @author zhang maijun
 * @since 2020/08/22
 */
public class DependencyUtil {
    /**
     * get transitive dependency information of the exact dependency
     *
     * @param remoteRepositoryMessageBean remote maven repository message
     * @param projectId                   groupId:artifactId:version project id
     * @param localRepo                   local maven repository
     * @return transitive dependency
     * @throws ArtifactDescriptorException
     */
    public static DependencyResult getDependencies(RemoteRepositoryMessageBean remoteRepositoryMessageBean,
                                                   String projectId, String localRepo, String scope) throws DependencyResolutionException {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        RepositorySystem system = newRepositorySystem(locator);
        RepositorySystemSession session = newSession(system, localRepo);

        RemoteRepository central = new RemoteRepository.Builder(remoteRepositoryMessageBean.getId(),
                remoteRepositoryMessageBean.getType(), remoteRepositoryMessageBean.getUrl()).build();

        Artifact artifact = new DefaultArtifact(projectId);
        // ArtifactDescriptorRequest request = new ArtifactDescriptorRequest(artifact, Arrays.asList(central), null);
        // ArtifactDescriptorResult result = system.readArtifactDescriptor(session, request);

        // resolved artifact, used to get the path of artifact in the local repository
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, scope));
        collectRequest.setRepositories(Collections.singletonList(central));
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);
        DependencyResult result = system.resolveDependencies(session, dependencyRequest);

        return result;
    }

    private static RepositorySystem newRepositorySystem(DefaultServiceLocator locator) {
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    private static RepositorySystemSession newSession(RepositorySystem system, String localRepo) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepository = new LocalRepository(localRepo);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepository));
        return session;
    }
}
