import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class MostViewedPerCategory {

	public static class MostViewedPerCategoryMapper extends Mapper<Object,Text,Text,Text>{
		
		Text outputkey=new Text();
		Text outputValue=new Text();
		
		public void map(Object key,Text value,Context context)
		  throws IOException,InterruptedException{
			
			String[] input=value.toString().split("\\t");
			if(input.length>8) {
				outputkey.set(input[3]);
				outputValue.set(input[0]+"@"+input[5]);
				context.write(outputkey,outputValue);
			}
			
		}
	}
	
	public static class MostViewedPerCategoryPartitioner extends Partitioner<Text,Text>{

		@Override
		public int getPartition(Text key, Text value, int numReduceTasks) {
			String category=key.toString();
			if(numReduceTasks==0) return 0;
			if(category.equals(" UNA")) return 1;
			if(category.equals("Autos & Vehicles")) return 2;
			if(category.equals("Comedy")) return 3;
			if(category.equals("Education")) return 4;
			if(category.equals("Entertainment")) return 5;
			if(category.equals("Film & Animation")) return 6;
			if(category.equals("Howto & Style")) return 7;
			if(category.equals("Music")) return 8;
			if(category.equals("News & Politics")) return 9;
			if(category.equals("Nonprofits & Activism")) return 10;
			if(category.equals("People & Blogs")) return 11;
			if(category.equals("Pets & Animals")) return 12;
			if(category.equals("Science & Technology")) return 13;
			if(category.equals("Sports")) return 14;
			if(category.equals("Travel & Events")) return 15;
			return 0;
		}
		
	}
	
	public static class MostViewedPerCategoryReducer extends Reducer<Text,Text,Text,Text>{
			
		public void reduce(Text key,Iterable<Text> values,Context context)
		   throws IOException,InterruptedException{
			
			Text TopViewedId=new Text();
			int max=Integer.MIN_VALUE;
			for(Text value: values) {
				String[] buff=value.toString().split("@");
				if(buff.length>=2 && Integer.parseInt(buff[1])>max) {
					TopViewedId.set(buff[0]);
					max=Integer.parseInt(buff[1]);
				}
			}
			
			context.write(key,TopViewedId);
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "YouTubeDataAnalyser");
		
		job.setJarByClass(MostViewedPerCategory.class);
		
		job.setMapperClass(MostViewedPerCategoryMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		
		job.setPartitionerClass(MostViewedPerCategoryPartitioner.class);
		job.setNumReduceTasks(16);
		
	    job.setReducerClass(MostViewedPerCategoryReducer.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(Text.class);
	    
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileSystem.get(conf).delete(new Path(args[1]),true);
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	    
	    System.exit(job.waitForCompletion(true) ? 0 : 1);

	}
}


