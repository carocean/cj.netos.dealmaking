package cj.netos.dealmaking.plugin.DealmakingEngine.reactor;

import java.util.Map;

import cj.studio.ecm.net.CircuitException;
import cj.studio.util.reactor.Event;
import cj.studio.util.reactor.IPipeline;
import cj.studio.util.reactor.IValve;
//调度并实现扣费
public class MyReactorControllerVavle implements IValve {
	Map<String, IValve> valves;
	public MyReactorControllerVavle(Map<String, IValve> valves) {
		this.valves=valves;
	}

	@Override
	public void flow(Event e, IPipeline pipeline) throws CircuitException {
		IValve valve=valves.get(e.getCmd());
		if(valve==null) {
			throw new CircuitException("404","不存在valve:"+e.getCmd());
		}
		valve.flow(e, pipeline);
	}

}
