package OaracleHRSchemaDataSetJoin;

import org.apache.hadoop.io.GenericWritable;
import org.apache.hadoop.io.Writable;


//This class can wrap the differrent type of classes emitted by Mapper 
//because Reducer expects exact type of instance else it will throw errors 
//like “Expected Writable, getting ProductRecord”
public class GenericJoinWritable extends GenericWritable {
	
	private static Class<? extends Writable>[] CLASSES = null;
	
	static{
		CLASSES = (Class<? extends Writable>[]) new Class[] {
                SalaryRecord.class
        };
	}
	
    public GenericJoinWritable() {}
    
    public GenericJoinWritable(Writable instance) {
        set(instance);
    }
	
	@Override
	protected Class<? extends Writable>[] getTypes() {
		// TODO Auto-generated method stub
		return CLASSES;
	}

}
