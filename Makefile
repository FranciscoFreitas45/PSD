build: 
	cd Dependencias/gpb/ && make && cd ../../
	Dependencias/gpb/bin/protoc-erl -I. -maps -o Frontend/ messages.proto
	erlc -I Dependencias/gpb/include -o Frontend/ Frontend/messages.erl
	mkdir -p priv
	cp Dependencias/erlzmq2/priv/erlzmq_drv.so priv/erlzmq_drv.so
	cp Dependencias/erlzmq2/ebin/*.beam Frontend/
	#erlc -I Dependencias/zeromq/include -o Frontend/ Frontend/erlzmq.erl
	#erlc -I Dependencias/zeromq/include -o Frontend/ Frontend/erlzmq_nif.erl
	erlc -o Frontend/ Frontend/loginManager.erl Frontend/userpainel.erl Frontend/frontend.erl Frontend/task.erl Frontend/taskManager.erl Frontend/finalizer.erl

clean:
	$(RM) Frontend/*.beam
	$(RM) Frontend/messages.erl