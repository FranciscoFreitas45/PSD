-module(taskManager).
-export([start/0,lookup/1,sendOrder/1]).

start() ->
    register(?MODULE, spawn(fun() -> taskhandler(#{},0) end)).


taskhandler(MapNego,Noffer) ->
    receive
        {order,Msg,Pid} ->
            Orderi = maps:get(manufacturerOrder,Msg),
            Orderf = maps:put(id,Noffer,Orderi),
            Msgn = maps:put(manufacturerOrder,Orderf,Msg),
            Pid = spawn(fun() -> io:format("New order ~n",[]) end),
            taskhandler(maps:put(Noffer,Pid,MapNego),Noffer+1);
        {lookup,Id,Pid} ->
            PidResult = maps:get(Id,MapNego),
            Pid ! {lookup,PidResult},
            taskhandler(MapNego,Noffer)
    end.


sendOrder(Msg) ->
    ?MODULE ! {order,Msg,self()}.

lookup(Id) ->
    ?MODULE ! {lookup,Id,self()},
    reply().

reply()->
    receive
        {?MODULE,rep} -> rep
    end.