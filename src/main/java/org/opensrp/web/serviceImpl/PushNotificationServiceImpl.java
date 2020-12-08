package org.opensrp.web.serviceImpl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensrp.web.pojo.MqttMessagePayload;
import org.opensrp.web.service.PushNotificationService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;

@Service
public class PushNotificationServiceImpl implements PushNotificationService, Consumer<Mqtt5Publish>, MqttClientConnectedListener, MqttClientDisconnectedListener, InitializingBean {
	
	private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
	
	private Mqtt5AsyncClient mqtt5AsyncClient = null;
	
	@Value("#{opensrp['emqx.server.host']}")
	private String serverHost;
	
	@Value("#{opensrp['emqx.server.identifier']}")
	private String serverIdentifier;
	
	@Value("#{opensrp['emqx.server.port']}")
	private int serverPort;
	
	@Override
	public void setUp() {
		Mqtt5ClientBuilder clientBuilder = Mqtt5Client.builder().identifier(serverIdentifier).serverHost(serverHost)
		        .serverPort(serverPort);
		
		clientBuilder.automaticReconnect().initialDelay(3, TimeUnit.SECONDS).maxDelay(10, TimeUnit.SECONDS)
		        .applyAutomaticReconnect();
		
		clientBuilder.addConnectedListener(this);
		clientBuilder.addDisconnectedListener(this);
		Mqtt5AsyncClient client = clientBuilder.buildAsync();
		
		client.publishes(MqttGlobalPublishFilter.ALL, this);
		setMqtt5AsyncClient(client);
		client.connectWith()
				.cleanStart(false)
				.sessionExpiryInterval(3600)
				.send()
		        .whenCompleteAsync((mqtt5ConnAck, throwable) -> {
			        ///subscribeByTopicAndQos("#", 2);
			        logger.log(Level.INFO, mqtt5ConnAck.toString());
		        }).exceptionally(throwable -> {
			        throwable.printStackTrace();
			        logger.log(Level.SEVERE, "connect", throwable);
			        return null;
		        });
	}
	
	@Override
	public void publish(MqttMessagePayload mqttMessagePayload) {
		if (mqtt5AsyncClient != null && mqtt5AsyncClient.getState().isConnected()) {
			CompletableFuture<Mqtt5PublishResult> mqtt5PublishResultCompletableFuture = mqtt5AsyncClient.publishWith()
			        .topic(mqttMessagePayload.getTopic()).payload(mqttMessagePayload.getMessage().getBytes())
			        .qos(mqttMessagePayload.getQos())
			        .userProperties(Mqtt5UserProperties.builder()
			                .add("client-id", mqtt5AsyncClient.getConfig().getClientIdentifier().toString())
			                .add("type", "cls").build())
			        .noMessageExpiry().contentType("application/text").send();
			mqtt5PublishResultCompletableFuture.whenComplete(new BiConsumer<Mqtt5PublishResult, Throwable>() {
				
				@Override
				public void accept(Mqtt5PublishResult mqtt5PublishResult, Throwable throwable) {
					logger.log(Level.SEVERE, "accept publish", throwable);
					logger.log(Level.INFO, "accept publish" + mqtt5PublishResult.toString());
				}
			}).exceptionally(new Function<Throwable, Mqtt5PublishResult>() {
				
				@Override
				public Mqtt5PublishResult apply(Throwable throwable) {
					throwable.printStackTrace();
					logger.log(Level.SEVERE, "apply publish", throwable);
					return null;
				}
			});
		}
	}

	public void setMqtt5AsyncClient(Mqtt5AsyncClient mqtt5AsyncClient) {
		this.mqtt5AsyncClient = mqtt5AsyncClient;
	}
	
	@Override
	public void disconnect() {
		if (mqtt5AsyncClient != null) {
			CompletableFuture<Void> completableFuture = mqtt5AsyncClient.disconnectWith().reasonString("nah cant tell")
			        .send();
			completableFuture.whenComplete(new BiConsumer<Void, Throwable>() {
				
				@Override
				public void accept(Void aVoid, Throwable throwable) {
					logger.log(Level.SEVERE, "accept disconnect", throwable);
				}
			}).exceptionally(new Function<Throwable, Void>() {
				
				@Override
				public Void apply(Throwable throwable) {
					throwable.printStackTrace();
					logger.log(Level.SEVERE, "apply disconnect", throwable);
					return null;
				}
			});
		}
	}
	
	@Override
	public void subscribeByTopicAndQos(String topic, int qos) {
		if (mqtt5AsyncClient != null) {
			CompletableFuture<Mqtt5SubAck> mqttSubAck = mqtt5AsyncClient.subscribeWith().topicFilter(topic)
			        .qos(MqttQos.fromCode(qos)).send();
			mqttSubAck.whenCompleteAsync(new BiConsumer<Mqtt5SubAck, Throwable>() {
				
				@Override
				public void accept(Mqtt5SubAck mqtt5SubAck, Throwable throwable) {
					System.out.println(mqtt5SubAck);
				}
			}).exceptionally(new Function<Throwable, Mqtt5SubAck>() {
				
				@Override
				public Mqtt5SubAck apply(Throwable throwable) {
					throwable.printStackTrace();
					logger.log(Level.INFO, "apply subscribe", throwable);
					return null;
				}
			});
		}
	}
	
	@Override
	public void accept(Mqtt5Publish mqtt5Publish) {
//		System.out.println(new String(mqtt5Publish.getPayloadAsBytes()));
//		System.out.println(mqtt5Publish.getPayload());
		logger.log(Level.INFO, "accept ->" + mqtt5Publish);
	}
	
	@Override
	public Consumer<Mqtt5Publish> andThen(Consumer<? super Mqtt5Publish> consumer) {
		logger.log(Level.FINEST, "andThen ->" + consumer.toString());
		return null;
	}
	
	@Override
	public void onConnected(MqttClientConnectedContext context) {
		logger.log(Level.INFO, "onConnected ->" + context.toString());
	}
	
	@Override
	public void onDisconnected(MqttClientDisconnectedContext context) {
		logger.log(Level.SEVERE, new Gson().toJson(context.getCause()));
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		setUp();
	}
}
