import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class FindMaxTemp {

	public static class TempMapper 
	     extends Mapper<Object,Text,Text,IntWritable>{
		
		public void map(Object key,Text value,Context context) 
				throws IOException, InterruptedException{
			
			String inputLine=value.toString();
			String Year=inputLine.substring(15, 19);
			char sign=inputLine.charAt(87);
			String temp=inputLine.substring(88, 92);
			IntWritable intTemp=new IntWritable();
			if(sign == '-')
				intTemp.set(-1*Integer.parseInt(temp));
			else
				intTemp.set(Integer.parseInt(temp));
			if(!temp.equals("9999")) {
				context.write(new Text(Year),intTemp);
			}
			
		}
	}
	
	public static class TempReducer 
		extends Reducer<Text,IntWritable,Text,IntWritable>{
		
		public void reduce(Text key,Iterable<IntWritable> values,Context context)
				throws IOException, InterruptedException{
			int max = Integer.MIN_VALUE;
			for(IntWritable value:values) {
				if(value.get() > max)
					max = value.get();
			}
			context.write(key, new IntWritable(max));
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "FindMaxTemp");
		
		job.setJarByClass(FindMaxTemp.class);
		
		job.setMapperClass(TempMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		job.setCombinerClass(TempReducer.class);
	    job.setReducerClass(TempReducer.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileSystem.get(conf).delete(new Path(args[1]),true);
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	    
	    System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}
