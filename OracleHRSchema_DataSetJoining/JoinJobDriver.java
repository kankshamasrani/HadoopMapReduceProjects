package OaracleHRSchemaDataSetJoin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
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
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import JoinProject.JoinGenericWritable;
import JoinProject.MapSideJoin;
import JoinProject.ProductIdKey;
import JoinProject.MapSideJoin.JoinGroupingComparator;
import JoinProject.MapSideJoin.JoinRecuder;
import JoinProject.MapSideJoin.JoinSortingComparator;
import JoinProject.MapSideJoin.ProductMapper;
import JoinProject.MapSideJoin.SalesOrderDataMapper;

import org.apache.hadoop.mapreduce.Reducer;

public class JoinJobDriver {
	
	public static class SalaryMapper extends Mapper<Object,Text,DepartmentIdKey,GenericJoinWritable>{
		
		IntWritable departmentId=new IntWritable();
		IntWritable salary=new IntWritable();
		SalaryRecord record;
		DepartmentIdKey dKey;
		Text dName=new Text();
		Path[] localFiles;
		private BufferedReader brReader;
		private HashMap<Integer, String> DeptMap = new HashMap<Integer, String>();
		 
		 public void readDeptFile(Path localFile) throws NumberFormatException, IOException {
			 brReader = new BufferedReader(new FileReader(localFile.toString()));
	    	 String strLineRead = "";
	    	 while ((strLineRead = brReader.readLine()) != null) {
	    		 String recordFields[] = strLineRead.split("\\t");
	    		 int key = Integer.parseInt(recordFields[0]);
	             String dName = recordFields[1];
	             DeptMap.put(key, dName);
	    	 }			 
		 }
		 
		 //Read the Dept table from cache
	     public void setup(Context context) throws IOException{
	    	 localFiles =DistributedCache.getLocalCacheFiles(context.getConfiguration());
	    	 if(localFiles[0]!=null)
	    		 readDeptFile(localFiles[0]);
	     }
	     
		public void map(Object key,Text value,Context context) throws IOException, InterruptedException {
			String[] input=value.toString().split("\\t");
			if(input.length>=9)
				departmentId.set(Integer.parseInt(input[8]));
			else
				departmentId.set(0);
			if(input.length>=8)
				salary.set(Integer.parseInt(input[7]));
			else
				salary.set(0);
			
			String deptName = departmentId.get() > 0 ? DeptMap.get(departmentId.get()) : "DefaultDepartment"; 
			dName.set(deptName);
			dKey=new DepartmentIdKey(departmentId,dName);
			record=new SalaryRecord(salary);
			
			GenericJoinWritable genericRecord = new GenericJoinWritable(record);
			context.write(dKey,genericRecord);
		}
	}
	
	public static class SalaryReducer extends Reducer<DepartmentIdKey,GenericJoinWritable,NullWritable,Text>{
		
		Writable inputRecord;
		Text op=new Text();
		
		public void reduce(DepartmentIdKey key,Iterable<GenericJoinWritable> values,Context context) throws IOException, InterruptedException {
			StringBuilder output = new StringBuilder();
			output.append(key.departmentId.toString());
			output.append(" ");
			output.append(key.departmentName.toString());
			output.append(" ");
			int salary=0,counter=0,avg=0;
			for(GenericJoinWritable record:values) {
				inputRecord=record.get();
				SalaryRecord salaryRecord=(SalaryRecord)inputRecord;
				salary+=salaryRecord.salary.get();
				counter++;
			}
			avg=salary/counter;
			output.append(avg);
			op.set(output.toString());
			context.write(NullWritable.get(),op);
		}
	}
	
	public static class JoinGroupingComparator extends WritableComparator{
		//join criteria is ProductIdKey
		public JoinGroupingComparator() {
			super(DepartmentIdKey.class,true);
		}
		
		public int compare(WritableComparable a,WritableComparable b) {
			DepartmentIdKey first = (DepartmentIdKey) a;
			DepartmentIdKey second = (DepartmentIdKey) b;
     
	        return first.departmentId.compareTo(second.departmentId);
		}
	}
	
	public static class JoinSortingComparator extends WritableComparator {
	    public JoinSortingComparator()
	    {
	        super (DepartmentIdKey.class, true);
	    }
	                               
	    @Override
	    public int compare (WritableComparable a, WritableComparable b){
	    	DepartmentIdKey first = (DepartmentIdKey) a;
	    	DepartmentIdKey second = (DepartmentIdKey) b;
	                                 
	        return first.compareTo(second);
	    }
	}
	public static void main(String[] args) throws IOException, URISyntaxException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf,"MapSideJoin");
	    job.setJarByClass(JoinJobDriver.class);
	    
	    DistributedCache.addCacheFile(new URI(args[0]), job.getConfiguration());
	    
	    job.setInputFormatClass(TextInputFormat.class);
	    job.setOutputFormatClass(TextOutputFormat.class);
	                               
	    job.setMapOutputKeyClass(DepartmentIdKey.class);
	    job.setMapOutputValueClass(GenericJoinWritable.class);
	                               
	    job.setMapperClass(SalaryMapper.class);                         
	    job.setReducerClass(SalaryReducer.class);
	                         
	    job.setSortComparatorClass(JoinSortingComparator.class);
	    job.setGroupingComparatorClass(JoinGroupingComparator.class);
	                               
	    job.setOutputKeyClass(NullWritable.class);
	    job.setOutputValueClass(Text.class);
	    FileInputFormat.addInputPath(job, new Path(args[1]));
	    FileSystem.get(conf).delete(new Path(args[2]),true);                           
	    FileOutputFormat.setOutputPath(job, new Path(args[2]));
	    
	    System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}
