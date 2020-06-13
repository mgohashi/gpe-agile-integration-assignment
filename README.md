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

4. After configured run the broker and the following components in the following order using `mvn clean spring-boot:run` command:
  
   - `inbound`
   - `xlate`
   - `services/integration-test-server`
   - `outbound`

5. Test the communication using the following command. Make sure you run the following command in the main folder:

   ---
   **NOTE**

   In order to use the `http` command you need to download and install the [httpie](https://httpie.org).

   ---

   ```shell
   http -v POST localhost:8080/camel/match @./inbound/src/test/resources/sample-person.xml 'Accept':'text/xml'
   ```
