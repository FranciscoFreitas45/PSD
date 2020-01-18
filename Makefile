build: 
	cd Dependencias/gpb/ && make && cd ../../
	Dependencias/gpb/bin/protoc-erl -I. -maps -o Frontend/ messages.proto
	erlc -I Dependencias/gpb/include -o Frontend/ Frontend/messages.erl
	erlc -I Dependencias/zeromq/include -o Frontend/ Frontend/erlzmq.erl
	erlc -I Dependencias/zeromq/include -o Frontend/ Frontend/erlzmq_nif.erl
	erlc -o Frontend/ Frontend/loginManager.erl Frontend/userpainel.erl Frontend/frontend.erl Frontend/task.erl Frontend/taskManager.erl

clean:
	$(RM) Frontend/*.beam
	$(RM) Frontend/messages.erl