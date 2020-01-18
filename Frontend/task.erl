-module(task).
-export([start/1]).


start(Msgn) ->
    {ok, Context} = erlzmq:context(1),
    {ok, SockPush} = erlzmq:socket(Context, [push, {active, false}]),
    {ok, SockPub} = erlzmq:socket(Context, [pub, {active, false}]),
    ok = erlzmq:connect(SockPush, "tcp://localhost:12347"),
    ok = erlzmq:connect(SockPub, "tcp://localhost:12345"),
    io:format("ZMQ connect "),
    erlzmq:send(SockPush, messages:encode_msg(Msgn)).



handleRequest(SockPush,SockPub,N) ->
    receive
        {offer,Msg,Pid} ->
            io:format("Msg Reg ~n",[]);
        {close,Pid} ->
            
    end.



