import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class TopRatingVideo {

	public static class TopRatingVideoMapper extends Mapper<Object,Text,Text,FloatWritable>{
		
		FloatWritable floatRating=new FloatWritable();
		Text text=new Text();
		
		public void map(Object key,Text value,Context context)
		  throws IOException,InterruptedException{
			
			String[] input=value.toString().split("\\t");
			if(input.length>8) {
				text.set(input[0]);
				floatRating.set(Float.parseFloat(input[6]));
			}
				context.write(text,floatRating);
			
		}
	}
	
	public static class TopRatingVideoReducer extends Reducer<Text,FloatWritable,Text,FloatWritable>{
		
		FloatWritable floatRating=new FloatWritable();
		
		public void reduce(Text key,Iterable<FloatWritable> values,Context context)
		   throws IOException,InterruptedException{
			
			int count=0;
			float sum=0;
			for(FloatWritable value: values) {
				count++;
				sum+=value.get();
			}
			
			//to get average
			sum=sum/count;
			floatRating.set(sum);
			context.write(key,floatRating);
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "YouTubeDataAnalyser");
		
		job.setJarByClass(TopRatingVideo.class);
		
		job.setMapperClass(TopRatingVideoMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(FloatWritable.class);
		
	    job.setReducerClass(TopRatingVideoReducer.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(FloatWritable.class);
	    
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileSystem.get(conf).delete(new Path(args[1]),true);
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	    
	    System.exit(job.waitForCompletion(true) ? 0 : 1);

	}
}

