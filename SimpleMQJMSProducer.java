import com.ibm.mq.MQException;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import java.io.IOException;


public class SimpleMQJMSProducer
{
    public void sendMsg(String msg)
    {
        MQQueueManager queueManager = null;
        MQMessage mqMessage = null;
        MQPutMessageOptions myPMO = null;

        try
        {

            // creating queue manager
            queueManager = new MQQueueManager("QMGR1");

            // creating queue`

            MQQueue queue = queueManager.accessQueue("MQ.REQUEST", CMQC.MQOO_OUTPUT);

            // configuring message encoding, character set, and format

            mqMessage = new MQMessage();
            mqMessage.encoding = new Integer("546");
            mqMessage.characterSet = new Integer("1208");
            mqMessage.format = "MQSTR";

            // sending message

            mqMessage.writeString(msg);
            myPMO = new MQPutMessageOptions();
            queue.put(mqMessage, myPMO);
            
            // Closing Queue after putting message
            queue.close();

        } catch (MQException | NumberFormatException | IOException je)
        {
            je.printStackTrace(System.err);
        } finally
        {
            if (queueManager != null) try
            {
                // Disconnecting queue manager
                queueManager.disconnect();
            } catch (MQException ex) {
                ex.printStackTrace(System.err);
            } 
        }
    }

    public static void main(String[] args)
    {
        new SimpleJMSProducer().sendMsg("Hello world!!!");
    }
}