package JoinProject;

import org.apache.hadoop.io.GenericWritable;
import org.apache.hadoop.io.Writable;

//This class can wrap the differrent type of classes emitted by Mapper 
//because Reducer expects exact type of instance else it will throw errors 
//like “Expected Writable, getting SalesOrderDataRecord”
public class JoinGenericWritable extends GenericWritable {
    
    private static Class<? extends Writable>[] CLASSES = null;

    //All of these types must implement Writable else exception
    static {
        CLASSES = (Class<? extends Writable>[]) new Class[] {
                SalesOrderDataRecord.class,
                ProductRecord.class
        };
    }
   
    public JoinGenericWritable() {}
   
    public JoinGenericWritable(Writable instance) {
        set(instance);
    }

    @Override
    protected Class<? extends Writable>[] getTypes() {
        return CLASSES;
    }
}