package cj.netos.dealmaking.plugin.DealmakingEngine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cj.netos.dealmaking.plugin.DealmakingEngine.bs.IDealmakingQueueTask;
import cj.studio.ecm.IEntryPointActivator;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.context.IElement;

public class DealmakingEngineEntrypointActivitor implements IEntryPointActivator {
	
	ExecutorService exe;
	private Future<?> future;

	@Override
	public void activate(IServiceSite site, IElement args) {
		exe = Executors.newSingleThreadExecutor();
		IDealmakingQueueTask task = new DealmakingQueueTask(site);
		this.future=exe.submit(task);
	}

	@Override
	public void inactivate(IServiceSite site) {
		future.cancel(true);
		exe.shutdown();
	}

}
