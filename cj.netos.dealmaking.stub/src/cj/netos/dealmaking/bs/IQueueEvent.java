package cj.netos.dealmaking.bs;

public interface IQueueEvent {
	void onevent(String action,Object...args);
}
