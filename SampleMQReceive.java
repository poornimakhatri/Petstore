import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQQueueConnection;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQQueueReceiver;
import com.ibm.mq.jms.MQQueueSession;
import com.ibm.msg.client.wmq.WMQConstants;

public class MQReceive {
  
 public static void main(String[] args)
 {
  try {
   MQQueueConnectionFactory cf = new MQQueueConnectionFactory();
   cf.setHostName("localhost");
   cf.setPort(1414);
    
   cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
   
   cf.setQueueManager("QM_GRIDSERVER");
   cf.setChannel("SYSTEM.ADMIN.SVRCONN");
    
   MQQueueConnection connection = (MQQueueConnection) cf.createQueueConnection();
   //MQQueueConnection connection = (MQQueueConnection) cf.createQueueConnection("username","password");
              
   MQQueueSession session = (MQQueueSession) connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

   MQQueue queue = (MQQueue) session.createQueue("queue:///MyTestQueue");
   
   MQQueueReceiver receiver = (MQQueueReceiver) session.createReceiver(queue);
 
   connection.start();
     
   TextMessage receivedMessage = (TextMessage) receiver.receive();
   System.out.println("Received message from Queue MyTestQueue: " + receivedMessage.getText());

   receiver.close();
   session.close();
   connection.close();
   System.out.println("Message Received OK.\n");
  }
  catch (JMSException jmsex) {
   System.out.println(jmsex);
   System.out.println("Message Receive Failure\n");
  }
  catch (Exception ex) {
   System.out.println(ex);
   System.out.println("Message Receive Failure\n");
  }
 }
}