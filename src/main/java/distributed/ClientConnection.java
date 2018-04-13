/**
 * Created by
 * Marios Prokopakis(3150141)
 * Stratos Xenouleas(3150130)
 * Foivos Kouroutsalidis(3080250)
 * Dimitris Staratzis(3150166)
 */
package distributed;

import org.apache.commons.math3.linear.RealMatrix;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.*;


class ArrayIndexComparator implements Comparator<Integer>
{
    private final Double[] array;

    /**
     * This is the constructor of the ArrayIndexComparator class
     *
     * @param array This is the array that we use to sort the indexes accordingly
     */
    public ArrayIndexComparator(Double[] array)
    {
        this.array = array;
    }

    /**
     * This method creates the index array
     */
    public Integer[] createIndexArray()
    {
        Integer[] indexes = new Integer[array.length];
        for (int i = 0; i < array.length; i++)
        {
            indexes[i] = i;
        }
        return indexes;
    }

    @Override
    /**
     * This method sorts the index array based on the comparator
     */
    public int compare(Integer index1, Integer index2)
    {
        return array[index2].compareTo(array[index1]);
    }
}

public class ClientConnection extends Thread
{
    private Socket client;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private RealMatrix predictions;


    /**
     * This is the constructor of the ClientConnections class
     *
     * @param connection  the socket between the client and Master
     * @param predictions this is the array which was created after training
     */
    public ClientConnection(Socket connection, RealMatrix predictions)
    {
        this.client = connection;
        try
        {
            this.predictions = predictions;
            in = new ObjectInputStream(client.getInputStream());
            out = new ObjectOutputStream(client.getOutputStream());
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * This method returns the row which contains the predictions for a specific user
     *
     * @param id The user
     */
    public synchronized double[] getUserPredictionWithId(int id)
    {
        return predictions.getRow(id);
    }


    @Override
    /**
     * This method starts the thread
     */
    public void run()
    {
        try
        {
            String a = (String) in.readObject();

            String[] tokens = a.split(";");
            int id = Integer.parseInt(tokens[1]);
            int topK = Integer.parseInt(tokens[0]);
            System.out.println("Message from client to Master: " + id + " " + topK);
            double[] b = getUserPredictionWithId(id);
            Double[] c = new Double[b.length];
            for (int i = 0; i < b.length; i++)
            {
                c[i] = new Double(b[i]);
            }

            ArrayIndexComparator comparator = new ArrayIndexComparator(c);
            Integer[] indexes = comparator.createIndexArray();
            Arrays.sort(indexes, comparator);
            Integer[] topKIndexes = new Integer[topK];
            for (int i = 0; i < topK; i++)
            {
                topKIndexes[i] = indexes[i];
            }
            out.writeObject(topKIndexes);
            out.flush();

        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (ClassNotFoundException cnfe)
        {

        } finally
        {
            close();
        }
    }

    /**
     * This method is used to close all streams
     * and connections that are related to this manager.
     */
    public void close()
    {
        try
        {
            in.close();
            out.close();
            client.close();
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
