/**
 * 
 */
package com.springframework.xd.batch.sample;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * @author Ilayaperumal Gopinathan
 * 
 */

public class HelloSpringXDTasklet implements Tasklet {

	private StepExecution execution = null;

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		System.out.println("Hi from HellSpringXDTasklet");
		return RepeatStatus.FINISHED;
	}
}
