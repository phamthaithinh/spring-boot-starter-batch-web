package de.codecentric.batch.metrics;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import de.codecentric.batch.listener.LoggingListener;

public class BatchMetricsImplTest {

	private MetricWriter metricWriterMock;
	private BatchMetricsImpl batchMetrics;

	@Before
	public void beforeTest() {
		metricWriterMock = mock(MetricWriter.class);
		batchMetrics = new BatchMetricsImpl(metricWriterMock);
		MDC.put(LoggingListener.STEP_EXECUTION_IDENTIFIER, "jobname-1");
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void incrementBy1Transactional() throws Exception {
		// Given
		TransactionSynchronizationManager.initSynchronization();
		// When
		batchMetrics.increment("counter.test", 1L);
		TransactionSynchronizationUtils.triggerAfterCompletion(TransactionSynchronization.STATUS_COMMITTED);
		// Then
		TransactionSynchronizationManager.clearSynchronization();
		ArgumentCaptor<Delta> argumentCaptor = ArgumentCaptor.forClass(Delta.class);
		verify(metricWriterMock).increment(argumentCaptor.capture());
		assertThat(argumentCaptor.getValue().getName(), equalTo("counter.batch.jobname-1.counter.test"));
		assertThat((Long)argumentCaptor.getValue().getValue(), equalTo(1L));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void decrementBy1Transactional() throws Exception {
		// Given
		TransactionSynchronizationManager.initSynchronization();
		// When
		batchMetrics.decrement("counter.test", 1L);
		TransactionSynchronizationUtils.triggerAfterCompletion(TransactionSynchronization.STATUS_COMMITTED);
		// Then
		TransactionSynchronizationManager.clearSynchronization();
		ArgumentCaptor<Delta> argumentCaptor = ArgumentCaptor.forClass(Delta.class);
		verify(metricWriterMock).increment(argumentCaptor.capture());
		assertThat(argumentCaptor.getValue().getName(), equalTo("counter.batch.jobname-1.counter.test"));
		assertThat((Long)argumentCaptor.getValue().getValue(), equalTo(-1L));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void incrementBy1NonTransactional() throws Exception {
		// Given
		// No transaction
		// When
		batchMetrics.increment("counter.test", 1L);
		// Then
		ArgumentCaptor<Delta> argumentCaptor = ArgumentCaptor.forClass(Delta.class);
		verify(metricWriterMock).increment(argumentCaptor.capture());
		assertThat(argumentCaptor.getValue().getName(), equalTo("counter.batch.jobname-1.counter.test"));
		assertThat((Long)argumentCaptor.getValue().getValue(), equalTo(1L));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void submitTransactional() throws Exception {
		// Given
		TransactionSynchronizationManager.initSynchronization();
		// When
		batchMetrics.submit("counter.test", 1L);
		TransactionSynchronizationUtils.triggerAfterCompletion(TransactionSynchronization.STATUS_COMMITTED);
		// Then
		TransactionSynchronizationManager.clearSynchronization();
		ArgumentCaptor<Metric> argumentCaptor = ArgumentCaptor.forClass(Metric.class);
		verify(metricWriterMock).set(argumentCaptor.capture());
		assertThat(argumentCaptor.getValue().getName(), equalTo("gauge.batch.jobname-1.counter.test"));
		assertThat((Double)argumentCaptor.getValue().getValue(), equalTo(1.0));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void submitNonTransactional() throws Exception {
		// Given
		// No transaction
		// When
		batchMetrics.submit("counter.test", 1L);
		// Then
		ArgumentCaptor<Metric> argumentCaptor = ArgumentCaptor.forClass(Metric.class);
		verify(metricWriterMock).set(argumentCaptor.capture());
		assertThat(argumentCaptor.getValue().getName(), equalTo("gauge.batch.jobname-1.counter.test"));
		assertThat((Double)argumentCaptor.getValue().getValue(), equalTo(1.0));
	}
}
