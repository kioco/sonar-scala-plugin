package com.stratio.sonar.jacoco;

import com.google.common.collect.Maps;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import java.util.Map;

public class ScalaExecutionDataVisitor implements ISessionInfoVisitor, IExecutionDataVisitor {
	
	private final Map<String, ExecutionDataStore> sessions = Maps.newHashMap();
	private ExecutionDataStore executionDataStore;
	private ExecutionDataStore merged = new ExecutionDataStore();
	
	@Override
	public void visitSessionInfo(SessionInfo info) {
		String sessionId = info.getId();
		executionDataStore = sessions.get(sessionId);
		if (executionDataStore == null) {
			executionDataStore = new ExecutionDataStore();
			sessions.put(sessionId, executionDataStore);
		}
	}
	
	@Override
	public void visitClassExecution(ExecutionData data) {
		executionDataStore.put(data);
		merged.put(defensiveCopy(data));
	}
	
	public Map<String, ExecutionDataStore> getSessions() {
		return sessions;
	}
	
	public ExecutionDataStore getMerged() {
		return merged;
	}
	
	private static ExecutionData defensiveCopy(ExecutionData data) {
		boolean[] src = data.getProbes();
		boolean[] dest = new boolean[src.length];
		System.arraycopy(src, 0, dest, 0, src.length);
		return new ExecutionData(data.getId(), data.getName(), dest);
	}
}
