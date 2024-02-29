# BAUER #

Bauer (a.k.a. stf4j - Simple Topic Facade for Java) serves as a simple facade or abstraction for various topic-based message-queues systems, such as Kafka, FFMQ, Java Message Service and MQTT. Bauer allows the end-user to plug in the desired message-queue framework at deployment time. Note that Bauer-enabling your library/application implies the addition of only a single mandatory dependency, namely bauer-api.jar.

Bauer has been written with esteem and respect for slf4j, the world famous Simple Logging Facade for Java.

### How to include Bauer in your project ###
```
#!xml
<dependency>
  <groupId>it.netgrid</groupId>
  <artifactId>bauer-api</artifactId>
  <version>0.0.13</version>
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
Binding for [FFMQ 4.0.14](http://timewalker74.github.io/ffmq/), a JMS compatible ultra-light weight message queue system.

#### bauer-mqtt [DEVELOPMENT] ####
Binding for [MQTT](http://mqtt.org/), a M2M/Internet-of-Things connectivity protocol. It was designed as an extremely lightweight publish/subscribe messaging transport.

#### bauer-kafka [DEVELOPMENT] ####
Binding for [Apache Kafka](https://kafka.apache.org/), a distributed streaming platform. Kafka is run as a cluster on one or more servers. Ideal for real-time streaming data pipelines that reliably get data between systems or applications.

To switch event-queue framework, just replace Bauer bindings on your class path. For example, to switch from FFMQ to Kafka, just replace bauer-ffmq with bauer-kafka.

Bauer does not rely on any special class loader machinery. In fact, each Bauer binding is hardwired at compile time to use one and only one specific event-queue framework. In your code, in addition to bauer-api, you simply drop one and only one binding of your choice onto the appropriate class path location. Do not place more than one binding on your class path.

### Concepts ###
Bauer relies upon three core concepts: events, topics and event handlers. 

#### Event ####
An event is a batch of data.

#### Topic ####
A topic is a stream of events of the same kind, sent at different times from different sources.

#### Event Handlers ####
Piece of code executed each time a new event occurs in the topic which holds the code

### ...one more thing! ###
By default, Bauer uses JSON for events de/serialization. We choose JSON interoperability over binary efficiency.


[Who was Bauer?](https://en.wikipedia.org/wiki/Felice_Bauer)

# Deploy #
you need access to https://oss.sonatype.org/

check your  ~/.m2/settings.xml

```
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>USERNAME</username>
      <password>PASSWORD</password>
    </server>
  </servers>
</settings>
```

then run in repo

```
mvn deploy
```
