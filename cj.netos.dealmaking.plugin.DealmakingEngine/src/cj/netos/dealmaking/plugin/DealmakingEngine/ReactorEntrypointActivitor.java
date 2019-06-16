package cj.netos.dealmaking.plugin.DealmakingEngine;

import cj.studio.ecm.IEntryPointActivator;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.ServiceCollection;
import cj.studio.ecm.context.IElement;
import cj.studio.util.reactor.DefaultReactor;
import cj.studio.util.reactor.IReactor;
import cj.studio.util.reactor.IServiceProvider;
import cj.studio.util.reactor.Reactor;
import cj.ultimate.util.StringUtil;

public class ReactorEntrypointActivitor implements IEntryPointActivator {
	@Override
	public void activate(IServiceSite site, IElement args) {
		String nthread = site.getProperty("reactor.workThreadCount");
		String qsize = site.getProperty("reactor.queueSize");
		int threads = StringUtil.isEmpty(nthread) ? 10 : Integer.valueOf(nthread);
		int size = StringUtil.isEmpty(qsize) ? 5000 : Integer.valueOf(qsize);
		IReactor reactor = Reactor.open(DefaultReactor.class, threads, size, new MyPipelineCombination(),
				new ExtendServiceProvider(site));
		site.addService("$.reactor", reactor);
	}

	@Override
	public void inactivate(IServiceSite site) {
		IReactor reactor = (IReactor) site.getService("$.reactor");
		if (reactor != null) {
			reactor.close();
		}
	}

	class ExtendServiceProvider implements IServiceProvider {
		IServiceSite site;

		public ExtendServiceProvider(IServiceSite site) {
			this.site = site;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getService(String name) {
			return (T) site.getService(name);
		}

		@Override
		public <T> ServiceCollection<T> getServices(Class<T> clazz) {
			return (ServiceCollection<T>) site.getServices((Class<T>) clazz);
		}
	}
}
