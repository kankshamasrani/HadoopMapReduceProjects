package OaracleHRSchemaDataSetJoin;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class SalaryRecord implements Writable {
    
	IntWritable salary;
	
	public SalaryRecord() {
		salary=salary=new IntWritable();
	}
	
	public SalaryRecord(IntWritable sal) {
		salary=sal;
	}
	@Override
	public void readFields(DataInput in) throws IOException {
		this.salary.readFields(in);		
	}

	@Override
	public void write(DataOutput out) throws IOException {
		this.salary.write(out);
		
	}

}
