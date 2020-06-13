# Assignment Agile Integration

## Installation process

1. Install AMQ Broker

   Download and install *AMQ Broker* with a user name `admin` and a password `password` in the `PATH` of your preference.

2. Create the following queues and addresses:

   ```shell
   artemis queue create --name [queue-name] --address [address-name] --preserve-on-no-consumers --durable --anycast --auto-create-address
   ```

   - `q.empi.deim.in`: This queue will receive the initial XML once it is delivered to the REST endpoint of the `inbound` component.
   - `q.empi.transform.dlq`: If any error occurs in the xlate component it will send the original message to this queue.
   - `q.empi.nextgate.out`: This queue receives the message that is transformed by the `xlate` component and is also the input queue for the `outbound` component.
   - `q.empi.nextgate.dlq`: If anything goes wrong when the `outbound` component tries to call the `integration-test-server` then the original message will be sent to this queue.

3. Clone the project at:

   ```shell
   git clone https://github.com/mgohashi/gpe-agile-integration-assignment
   ```

   - Go into the `parent` folder and run the following command:

     ```shell
     mvn clean install
     ```

   - Go into the `artifacts` folder and run the following command:

     ```shell
     mvn clean install
     ```

4. After configured run the broker and the following components in the following order using `mvn clean spring-boot:run` command:
  
   ---
   **NOTE**

   The components bellow will use the port: `amq.port=5672` and the host: `amq.host=localhost`.

   ---

   - `inbound`: this component will listen in the port `8080` in the path `/camel/match`. To access the service access [http://localhost:8080/camel/match](http://localhost:8080/camel/match)
   - `xlate`: this component is totally message based
   - `services/integration-test-server`: this component will listen in the port `9080` in the path `/service/PersonEJBService/PersonEJB`. To access the WSDL file access the following URL: [http://localhost:9080/service/PersonEJBService/PersonEJB?wsdl](http://localhost:9080/service/PersonEJBService/PersonEJB?wsdl)
   - `outbound`: This component is totally message based and it integrates with the `integration-test-server` using SOAP protocol.

5. Test the communication using the following command. Make sure you run the following command in the main folder:

   ---
   **NOTE**

   In order to use the `http` command you need to download and install the [httpie](https://httpie.org).

   ---

   ```shell
   http -v POST localhost:8080/camel/match @./inbound/src/test/resources/sample-person.xml 'Accept':'text/xml'
   ```
