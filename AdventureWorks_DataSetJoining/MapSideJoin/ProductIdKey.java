package JoinProject;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;

public class ProductIdKey implements WritableComparable<ProductIdKey>{
    public IntWritable ProductId;
    public IntWritable recordType;
    
    public static final IntWritable PRODUCT_RECORD = new IntWritable(0);
    public static final IntWritable DATA_RECORD = new IntWritable(1);
    
    public ProductIdKey() {
    	ProductId=new IntWritable();
    	recordType=new IntWritable();
    }
    
    public ProductIdKey(IntWritable ProductId,IntWritable recordType) {
    	this.ProductId=ProductId;
    	this.recordType=recordType;
    }
    
	@Override
	public void readFields(DataInput in) throws IOException {
		this.ProductId.readFields(in);
		this.recordType.readFields(in);
		
	}

	@Override
	public void write(DataOutput out) throws IOException {
		this.ProductId.write(out);
		this.recordType.write(out);
		
	}
	
	//to be used by default Sorting and grouping comparator
	//grouping comparator...groups the keys and send its values wrapped up in Iterable to reducer
	//Sorting comparator ...determines the order
	@Override
	public int compareTo(ProductIdKey o) {
		if (this.ProductId.equals(o.ProductId )) {
	        return this.recordType.compareTo(o.recordType);
	    } else {
	        return this.ProductId.compareTo(o.ProductId);
	    }
	}
	
	public boolean equals(ProductIdKey o) {
		return this.ProductId.equals(o.ProductId) && this.recordType.equals(o.recordType );
	}
	//to be used by partitioner to determine which record will go to which reducer
	public int hashcode() {
		return this.ProductId.hashCode();
	}

}
