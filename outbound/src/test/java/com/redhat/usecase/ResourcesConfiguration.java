package com.redhat.usecase;

//@TestConfiguration
public class ResourcesConfiguration {
//	static int amqpPort = AvailablePortFinder.getNextAvailable();
//    
//    static EmbeddedActiveMQ server = new EmbeddedActiveMQ();
//    
//    @Autowired
//	private CamelContext camelContext;
//	
//	@Bean
//	@Primary
//	public AMQPConnectionDetails securedAmqpConnection() throws Exception {
//		Configuration config = new ConfigurationImpl();
//        AddressSettings addressSettings = new AddressSettings();
//        // Disable auto create address to make sure that topic name is correct without prefix
//        addressSettings.setAutoCreateAddresses(false);
//        config.addAcceptorConfiguration("amqp", "tcp://0.0.0.0:" + amqpPort 
//                                        + "?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=AMQP;useEpoll=true;amqpCredits=1000;amqpMinCredits=300");
//        config.setPersistenceEnabled(false);
//        config.addAddressesSetting("#", addressSettings);
//        config.setSecurityEnabled(false);
//        
//        // Set explicit topic name
//        CoreAddressConfiguration pingTopicConfig = new CoreAddressConfiguration();
//        pingTopicConfig.setName("topic.ping");
//        pingTopicConfig.addRoutingType(RoutingType.MULTICAST);
//        
//        config.addAddressConfiguration(pingTopicConfig);
//        
//        server.setConfiguration(config);
//        server.start();
//        System.out.println("AMQ_PORT="+amqpPort);
//        return discoverAMQP(camelContext);
//	}
}
