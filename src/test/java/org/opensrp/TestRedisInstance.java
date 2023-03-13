package org.opensrp;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class TestRedisInstance {
	private static final String DOCKER_IMAGE_NAME = "redis:7-alpine";
	protected static final int DOCKER_EXPOSE_PORT = 6379;

	protected static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse(DOCKER_IMAGE_NAME))
			.withExposedPorts(DOCKER_EXPOSE_PORT);

	static {
		redisContainer.start();
	}
}
