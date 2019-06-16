package cj.netos.dealmaking.plugin.DealmakingEngine;

import java.util.HashMap;
import java.util.Map;

import cj.netos.dealmaking.plugin.DealmakingEngine.reactor.MyReactorControllerVavle;
import cj.studio.ecm.ServiceCollection;
import cj.studio.ecm.annotation.CjService;
import cj.studio.util.reactor.IPipeline;
import cj.studio.util.reactor.IPipelineCombination;
import cj.studio.util.reactor.IValve;

public class MyPipelineCombination implements IPipelineCombination {
	@Override
	public void combine(IPipeline pipeline) {
		Map<String, IValve> valves=new HashMap<String, IValve>();
		ServiceCollection<IValve> col=pipeline.site().getServices(IValve.class);
		for(IValve v:col) {
			CjService cj=v.getClass().getAnnotation(CjService.class);
			valves.put(cj.name(), v);
		}
		pipeline.append(new MyReactorControllerVavle(valves));

	}

}
