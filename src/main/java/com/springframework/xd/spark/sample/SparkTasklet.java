/**
 * 
 */
package com.springframework.xd.spark.sample;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import scala.Tuple2;

import com.google.common.collect.Lists;

public class SparkTasklet implements Tasklet {
	
	private static final Pattern SPACE = Pattern.compile(" ");

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		    SparkConf sparkConf = new SparkConf().setAppName("JavaKafkaWordCount").setMaster("spark://localhost:7077");
		    // Create the context with a 1 second batch size
		    JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(2000));

		    int numThreads = 1;
		    Map<String, Integer> topicMap = new HashMap<String, Integer>();
		    String[] topics = new String[] { "test1", "demo" };
		    for (String topic: topics) {
		      topicMap.put(topic, numThreads);
		    }

		    JavaPairReceiverInputDStream<String, String> messages =
		            KafkaUtils.createStream(jssc, "localhost:2181", "group", topicMap);

		    JavaDStream<String> lines = messages.map(new Function<Tuple2<String, String>, String>() {
		      @Override
		      public String call(Tuple2<String, String> tuple2) {
		        return tuple2._2();
		      }
		    });

		    JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
		      @Override
		      public Iterable<String> call(String x) {
		        return Lists.newArrayList(SPACE.split(x));
		      }
		    });

		    JavaPairDStream<String, Integer> wordCounts = words.mapToPair(
		      new PairFunction<String, String, Integer>() {
		        @Override
		        public Tuple2<String, Integer> call(String s) {
		          return new Tuple2<String, Integer>(s, 1);
		        }
		      }).reduceByKey(new Function2<Integer, Integer, Integer>() {
		        @Override
		        public Integer call(Integer i1, Integer i2) {
		          return i1 + i2;
		        }
		      });

		    wordCounts.print();
		    jssc.start();
		    jssc.awaitTermination();
		return RepeatStatus.FINISHED;
	}
}
