# BAUER #

Bauer (a.k.a. stf4j - Simple Topic Facade for Java) serves as a simple facade or abstraction for various topic-based message-queues systems, such as Kafka, FFMQ, Java Message Service and MQTT. Bauer allows the end-user to plug in the desired message-queue framework at deployment time. Note that Bauer-enabling your library/application implies the addition of only a single mandatory dependency, namely bauer-api.jar.

Bauer has been written with esteem and respect for slf4j, the world famous Simple Logging Facade for Java.

### How to include Bauer in your project ###
```
#!xml
<dependency>
  <groupId>it.netgrid</groupId>
  <artifactId>bauer-api</artifactId>
  <version>0.0.2</version>
</dependency>
```

### Bauer send event sample ###

```
#!java
Topic<MyCustomPayload> myCustomTopic = TopicFactory.getTopic("/my/topic/path");
myCustomTopic.post(new MyCustomPayload("Hello World"));
```

### Bauer event handler sample ###

```
#!java
Topic<MyCustomPayload> myCustomTopic = TopicFactory.getTopic("/my/topic/path");
myCustomTopic.addHandler(new EventHandler<MyCustomPayload>() {

	@Override
	public Class<MyCustomPayload> getEventClass() {
		return MyCustomPayload.class;
	}

	@Override
	public String getName() {
		return "handler-identifier";
	}

	@Override
	public boolean handle(MyCustomPayload event) {
		System.out.println(event.getMyCustomProperty())
		return true;
	}
});
```

### Binding with a event-queue framework ad deployment time
As mentioned previously, Bauer supports various topic-based event-queues frameworks. Some of them still in development. The Bauer distribution ships with serveral jar files referred as "Bauer bindings", with each binding corresponding to a supported framework.

#### bauer-ffmq ####
Binding for [FFMQ 3.0.7](http://timewalker74.github.io/ffmq/), a JMS compatible ultra-light weight message queue system.

#### bauer-mqtt [DEVELOPMENT] ####
Binding for [MQTT](http://mqtt.org/), a M2M/Internet-of-Things connectivity protocol. It was designed as an extremely lightweight publish/subscribe messaging transport.

#### bauer-kafka [DEVELOPMENT] ####
Binding for [Apache Kafka](https://kafka.apache.org/), a distributed streaming platform. Kafka is run as a cluster on one or more servers. Ideal for real-time streaming data pipelines that reliably get data between systems or applications.

[Who was Bauer?](https://en.wikipedia.org/wiki/Felice_Bauer)