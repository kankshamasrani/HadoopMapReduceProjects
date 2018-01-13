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

public class StormLocationAnalyser {

	public static class StormLocationMapper 
	     extends Mapper<Object,Text,Text,IntWritable>{
		
		IntWritable ONE = new IntWritable(1);
		public void map(Object key,Text value,Context context) 
				throws IOException, InterruptedException{
			
			String[] input=value.toString().split(",");
			if(input.length >=7)
				context.write(new Text(input[6]),ONE);
		}
	}
	
	public static class StormLocationCombiner 
		extends Reducer<Text,IntWritable,Text,IntWritable>{
	
	public void reduce(Text key,Iterable<IntWritable> values,Context context)
			throws IOException, InterruptedException{
		int count = 0;
		for(IntWritable value:values) {
			count = count+value.get();
		}
		context.write(key, new IntWritable(count));
	}
}
	
	public static class StormLocationReducer 
		extends Reducer<Text,IntWritable,Text,IntWritable>{
		
		public void reduce(Text key,Iterable<IntWritable> values,Context context)
				throws IOException, InterruptedException{
			int count = 0;
			for(IntWritable value:values) {
				count = count+value.get();
			}
			context.write(key, new IntWritable(count));
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Storm Location Dec 2017");
		
		job.setJarByClass(StormLocationAnalyser.class);
		
		job.setMapperClass(StormLocationMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		job.setCombinerClass(StormLocationCombiner.class);
	    job.setReducerClass(StormLocationReducer.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileSystem.get(conf).delete(new Path(args[1]),true);
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	    
	    System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}

