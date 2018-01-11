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


public class HitsFromIP {
	
	public static class TokenizerMapper 
	      extends Mapper<Object,Text,Text,IntWritable>{
		
		public void map(Object key,Text value,Context context) 
				throws IOException, InterruptedException  {
			
			String[] line=value.toString().split(" ");
			if(line.length > 1) {
				Text word = new Text();
				word.set(line[0]);
				context.write(word,new IntWritable(1));
			}
		}
		
	}
	
	  public static class HitsFromIPReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
		  
		  public void reduce(Text key,Iterable<IntWritable> values,Context context)
				  throws IOException, InterruptedException{
			  int sum=0;
			  for(IntWritable value : values) {
				  sum+=value.get();
			  }
			  
			  context.write(key,new IntWritable(sum));
		  }
		  
	  }
	
	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();
	    Job job = Job.getInstance(conf, "HitsFromIP");
	    job.setJarByClass(HitsFromIP.class);
	    
	    job.setMapperClass(TokenizerMapper.class);
	    job.setMapOutputKeyClass(Text.class);
	    job.setMapOutputValueClass(IntWritable.class);
	    
	    job.setCombinerClass(HitsFromIPReducer.class);
	    job.setReducerClass(HitsFromIPReducer.class);
	    
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileSystem.get(conf).delete(new Path(args[1]),true);
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	    
	    System.exit(job.waitForCompletion(true) ? 0 : 1);
	  
	}
}
