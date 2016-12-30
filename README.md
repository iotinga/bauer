# BAUER #

Bauer (a.k.a. stf4j - Simple Topic Facade for Java) serves as a simple facade or abstraction for various message-queues systems, such as Kafka, FFMQ, Java Message Service and MQTT. Bauer allows the end-user to plug in the desired message-queue framework at deployment time. Note that Bauer-enabling your library/application implies the addition of only a single mandatory dependency, namely bauer-api.jar.

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

### What is this repository for? ###

* Quick summary
* Version
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###

* Summary of set up
* Configuration
* Dependencies
* Database configuration
* How to run tests
* Deployment instructions

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact

[Who was Bauer?](https://en.wikipedia.org/wiki/Felice_Bauer)