package JoinProject;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class ReduceSideJoin {
	//Mappers need to emitt differrent types but reducer has to be passed only one type hence the wrapper
    public static class SalesOrderDataMapper extends Mapper<Object,Text,ProductIdKey,JoinGenericWritable>{
    	IntWritable pId=new IntWritable();
    	public void map(Object key,Text value,Context context) throws IOException,InterruptedException{
    		String[] recordFields = value.toString().split("\\t");
            int productId = Integer.parseInt(recordFields[4]);
            int orderQty = Integer.parseInt(recordFields[3]);
            double lineTotal = Double.parseDouble(recordFields[8]);
            pId.set(productId);                                       
            ProductIdKey recordKey = new ProductIdKey(pId, ProductIdKey.DATA_RECORD);
            SalesOrderDataRecord record = new SalesOrderDataRecord(orderQty, lineTotal);
                                                   
            JoinGenericWritable genericRecord = new JoinGenericWritable(record);
            context.write(recordKey, genericRecord);
    	}
    }
	
    //Mappers need to emit differrent types but reducer has to be passed only one type hence the wrapper
	public static class ProductMapper extends Mapper<Object,Text,ProductIdKey,JoinGenericWritable>{

	     IntWritable pId=new IntWritable();
		 public void map(Object key,Text value,Context context) throws IOException,InterruptedException{
			 String[] recordFields = value.toString().split("\\t");
			 String pName=recordFields[1];
			 String pNumber=recordFields[2];
			 
			 pId.set(Integer.parseInt(recordFields[0]));
			 
			 ProductIdKey recordKey = new ProductIdKey(pId, ProductIdKey.PRODUCT_RECORD);
			 ProductRecord record=new ProductRecord(pName,pNumber);
			 
	         JoinGenericWritable genericRecord = new JoinGenericWritable(record);
	         context.write(recordKey, genericRecord);
		 }
	}
	public static class JoinGroupingComparator extends WritableComparator{
		//join criteria is ProductIdKey
		public JoinGroupingComparator() {
			super(ProductIdKey.class,true);
		}
		
		public int compare(WritableComparable a,WritableComparable b) {
			ProductIdKey first = (ProductIdKey) a;
	        ProductIdKey second = (ProductIdKey) b;
     
	        return first.ProductId.compareTo(second.ProductId);
		}
	}
	
	
	public static class JoinRecuder extends Reducer<ProductIdKey, JoinGenericWritable, NullWritable, Text>{
	    public void reduce(ProductIdKey key, Iterable<JoinGenericWritable> values, Context context) throws IOException, InterruptedException{
	        StringBuilder output = new StringBuilder();
	        int sumOrderQty = 0;
	        double sumLineTotal = 0.0;
	                                               
	        for (JoinGenericWritable v : values) {
	            Writable record = v.get();
	            if (key.recordType.equals(ProductIdKey.PRODUCT_RECORD)){
	                ProductRecord pRecord = (ProductRecord)record;
	                output.append(Integer.parseInt(key.ProductId.toString())).append(", ");
	                output.append(pRecord.productName.toString()).append(", ");
	                output.append(pRecord.productNumber.toString()).append(", ");
	            } else {
	                SalesOrderDataRecord record2 = (SalesOrderDataRecord)record;
	                sumOrderQty += Integer.parseInt(record2.orderQty.toString());
	                sumLineTotal += Double.parseDouble(record2.lineTotal.toString());
	            }
	        }
	        
	        if (sumOrderQty > 0) {
	            context.write(NullWritable.get(), new Text(output.toString() + sumOrderQty + ", " + sumLineTotal));
	        }
	    }
	}
	
	public static class JoinSortingComparator extends WritableComparator {
	    public JoinSortingComparator()
	    {
	        super (ProductIdKey.class, true);
	    }
	                               
	    @Override
	    public int compare (WritableComparable a, WritableComparable b){
	        ProductIdKey first = (ProductIdKey) a;
	        ProductIdKey second = (ProductIdKey) b;
	                                 
	        return first.compareTo(second);
	    }
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf,"ReduceSideJoin");
	    job.setJarByClass(ReduceSideJoin.class);
	                               
	    job.setInputFormatClass(TextInputFormat.class);
	    job.setOutputFormatClass(TextOutputFormat.class);
	                               
	    job.setMapOutputKeyClass(ProductIdKey.class);
	    job.setMapOutputValueClass(JoinGenericWritable.class);
	                               
	    MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, SalesOrderDataMapper.class);
	    MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, ProductMapper.class);
	                              
	    job.setReducerClass(JoinRecuder.class);
	                         
	    job.setSortComparatorClass(JoinSortingComparator.class);
	    job.setGroupingComparatorClass(JoinGroupingComparator.class);
	                               
	    job.setOutputKeyClass(NullWritable.class);
	    job.setOutputValueClass(Text.class);
	                               
	    FileOutputFormat.setOutputPath(job, new Path(args[2]));
	    
	    System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}
