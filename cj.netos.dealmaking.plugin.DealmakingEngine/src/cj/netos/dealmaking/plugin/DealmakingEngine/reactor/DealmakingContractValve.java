package cj.netos.dealmaking.plugin.DealmakingEngine.reactor;

import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.studio.util.reactor.Event;
import cj.studio.util.reactor.IPipeline;
import cj.studio.util.reactor.IValve;
import cj.ultimate.gson2.com.google.gson.Gson;

@CjService(name = "dealmakingContract")
public class DealmakingContractValve implements IValve{
	//扣手续费：按交易价从买扣除。使之买的帑银量变少，之后将卖方的帑银给买方。
	@Override
	public void flow(Event e, IPipeline pipeline) throws CircuitException {
		System.out.println(new Gson().toJson(e));
	}

}
