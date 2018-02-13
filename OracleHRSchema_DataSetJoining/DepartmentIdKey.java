package OaracleHRSchemaDataSetJoin;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import JoinProject.ProductIdKey;

public class DepartmentIdKey implements WritableComparable<DepartmentIdKey> {

	public IntWritable departmentId;
	public Text departmentName;
	
	public DepartmentIdKey() {		
		departmentId=new IntWritable();
		departmentName=new Text();
	}
	public DepartmentIdKey(IntWritable productId,Text name) {
		this.departmentId=productId;
		this.departmentName=name;
	}
	@Override
	public void readFields(DataInput in) throws IOException {
		this.departmentId.readFields(in);
		this.departmentName.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		this.departmentId.write(out);
		this.departmentName.write(out);
	}

	@Override
	public int compareTo(DepartmentIdKey o) {
		if (this.departmentId.equals(o.departmentId )) {
	        return this.departmentName.compareTo(o.departmentName);
	    } else {
	        return this.departmentId.compareTo(o.departmentId);
	    }
	}
	
	public boolean equals(DepartmentIdKey o) {
		return this.departmentId.equals(o.departmentId) && this.departmentName.equals(o.departmentName );
	}
	//to be used by partitioner to determine which record will go to which reducer
	public int hashcode() {
		return this.departmentId.hashCode();
	}

}
